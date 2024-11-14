package com.vladrip.ifchat.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.room.withTransaction
import com.haroldadmin.cnradapter.NetworkResponse
import com.haroldadmin.cnradapter.executeWithRetry
import com.vladrip.ifchat.data.entity.Message
import com.vladrip.ifchat.data.local.LocalDatabase
import com.vladrip.ifchat.data.mediator.MessageRemoteMediator
import com.vladrip.ifchat.data.network.IFChatService
import com.vladrip.ifchat.data.network.MESSAGE_NETWORK_PAGE_SIZE
import org.koin.core.annotation.Single

@Single
class MessageRepository(
    private val api: IFChatService,
    private val localDb: LocalDatabase,
) {
    private val messageDao = localDb.messageDao()
    private val chatListDao = localDb.chatListDao()

    @OptIn(ExperimentalPagingApi::class)
    fun getMessagesByChatId(chatId: Long) = Pager(
        config = PagingConfig(MESSAGE_NETWORK_PAGE_SIZE),
        remoteMediator = MessageRemoteMediator(api, localDb, chatId)
    ) {
        messageDao.getAll(chatId)
    }.flow

    suspend fun save(message: Message, createLocalTemp: Boolean) {
        var tempId: Long = 0
        if (createLocalTemp) {
            localDb.withTransaction {
                tempId = messageDao.getMaxId() + 19
                messageDao.insert(message.copy(id = tempId))
            }
        }

        val response = executeWithRetry(
            times = Int.MAX_VALUE,
            initialDelay = 500
        ) { api.saveMessage(message) }

        if (response is NetworkResponse.Success)
            response.headers["Location"]
                ?.substringAfterLast("/")
                ?.toLongOrNull()
                ?.also { realId ->
                    localDb.withTransaction {
                        if (createLocalTemp) messageDao.delete(tempId)
                        messageDao.insert(message.copy(id = realId, status = Message.Status.SENT))
                    }
                }
    }

    suspend fun delete(id: Long) {
        var isMessageLocal = false
        localDb.withTransaction {
            val message = messageDao.get(id)
            if (message.status == Message.Status.SENDING) {
                isMessageLocal = true
                messageDao.delete(id)
                return@withTransaction
            }
            messageDao.insert(message.copy(status = Message.Status.DELETING))
        }
        if (isMessageLocal) return

        val response = executeWithRetry(
            times = Int.MAX_VALUE,
            initialDelay = 500,
        ) { api.deleteMessage(id) }

        if (response is NetworkResponse.Success)
            messageDao.delete(id)
    }

    suspend fun saveMessageLocally(message: Message) {
        messageDao.insert(message)
        chatListDao
            .updateLastMsg(message.chatId, message.id, message.content, message.sentAt)
    }
}