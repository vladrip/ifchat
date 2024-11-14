package com.vladrip.ifchat.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.room.withTransaction
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.haroldadmin.cnradapter.NetworkResponse
import com.haroldadmin.cnradapter.executeWithRetry
import com.vladrip.ifchat.data.Data
import com.vladrip.ifchat.data.entity.Chat
import com.vladrip.ifchat.data.local.LocalDatabase
import com.vladrip.ifchat.data.mediator.ChatListRemoteMediator
import com.vladrip.ifchat.data.mediator.ChatMembersRemoteMediator
import com.vladrip.ifchat.data.network.CHAT_LIST_NETWORK_PAGE_SIZE
import com.vladrip.ifchat.data.network.CHAT_MEMBERS_PAGE_SIZE
import com.vladrip.ifchat.data.network.IFChatService
import com.vladrip.ifchat.data.network.model.BooleanWrapper
import com.vladrip.ifchat.data.network.model.ChatCreateDto
import com.vladrip.ifchat.data.utils.Mapper
import com.vladrip.ifchat.data.utils.handleLocalRequest
import com.vladrip.ifchat.data.utils.handleNetworkRequest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import org.koin.core.annotation.Single

@Single
class ChatRepository(
    private val api: IFChatService,
    private val localDb: LocalDatabase,
) {
    private val chatDao = localDb.chatDao()
    private val chatListDao = localDb.chatListDao()
    private val personDao = localDb.personDao()
    private val chatMemberShortDao = localDb.chatMemberShortDao()

    @OptIn(ExperimentalPagingApi::class)
    fun getChatList() = Pager(
        config = PagingConfig(CHAT_LIST_NETWORK_PAGE_SIZE),
        remoteMediator = ChatListRemoteMediator(api, localDb)
    ) {
        chatListDao.getOrderByLatestMsg()
    }.flow

    fun getChat(id: Long) = merge(
        handleLocalRequest { chatDao.get(id) }.map {
            if (it is Data.Success) {
                var localChat = it.payload
                if (localChat.type == Chat.ChatType.PRIVATE && localChat.otherPersonUid != null) {
                    val otherPerson =
                        handleLocalRequest { personDao.get(localChat.otherPersonUid!!) }.firstOrNull()
                    if (otherPerson is Data.Success) {
                        localChat = localChat.copy(name = otherPerson.payload?.displayName)
                    }
                }

                Data.Success(localChat)
            } else it
        },

        handleNetworkRequest { api.getChat(id) }.map { data ->
            when (data) {
                is Data.Success -> {
                    val chatDto = data.payload
                    val chat = Mapper.toChat(chatDto)
                    localDb.withTransaction {
                        if (chat.type == Chat.ChatType.PRIVATE && chatDto.otherPerson != null)
                            personDao.insert(chatDto.otherPerson)
                        chatDao.insert(chat)
                    }
                    Data.Success(chat)
                }

                is Data.NotSuccess -> data
            }
        }
    )

    fun getPrivateChat(otherPersonUid: String) = merge(
        handleLocalRequest { chatDao.getByOtherPersonUid(otherPersonUid) },

        handleNetworkRequest { api.getPrivateChat(otherPersonUid) }.map { data ->
            when (data) {
                is Data.Success -> {
                    val chatDto = data.payload
                    val chat = Mapper.toChat(chatDto)
                    chatDao.insert(chat)
                    Data.Success(chat)
                }

                is Data.NotSuccess -> data
            }
        }
    )

    @OptIn(ExperimentalPagingApi::class)
    fun getMembers(id: Long) = Pager(
        config = PagingConfig(CHAT_MEMBERS_PAGE_SIZE),
        remoteMediator = ChatMembersRemoteMediator(api, localDb, id),
    ) {
        chatMemberShortDao.getOrderByMostRecentOnline(id)
    }.flow

    suspend fun muteChat(chatId: Long, value: Boolean) {
        val response = executeWithRetry(
            times = Int.MAX_VALUE,
            initialDelay = 500,
        ) { api.setIsChatMuted(Firebase.auth.uid!!, chatId, BooleanWrapper(value)) }

        if (response is NetworkResponse.Success) {
            localDb.withTransaction {
                chatDao.updateUserChatMember(chatId, response.body)
                chatListDao.updateIsMuted(chatId, response.body.isChatMuted)
            }
        } else {
            //TODO: snackbar
        }
    }

    suspend fun createChat(
        type: Chat.ChatType,
        name: String? = null,
        members: List<String>? = null,
        otherPersonUid: String? = null,
    ): Chat? {
        val body = ChatCreateDto(name ?: "", type, members, otherPersonUid)
        val response = api.createChat(body)

        return if (response is NetworkResponse.Success) {
            val chat = Mapper.toChat(response.body)
            localDb.withTransaction {
                chatDao.insert(chat)
                chatListDao.insert(
                    _root_ide_package_.com.vladrip.ifchat.data.entity.ChatListItem(
                        chat.id,
                        chat.name ?: "",
                        chat.type
                    )
                )
            }
            chat
        } else {
            //TODO: snackbar
            null
        }
    }
}
