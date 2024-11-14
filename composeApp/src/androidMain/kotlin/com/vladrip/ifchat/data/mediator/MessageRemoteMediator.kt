package com.vladrip.ifchat.data.mediator

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.vladrip.ifchat.data.entity.Message
import com.vladrip.ifchat.data.local.LocalDatabase
import com.vladrip.ifchat.data.network.IFChatService
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class MessageRemoteMediator(
    private val api: IFChatService,
    private val localDb: LocalDatabase,
    private val chatId: Long,
) : RemoteMediator<Int, Message>() {
    private val messageDao = localDb.messageDao()

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    //note: message list is reversed, so state, append and prepend are reversed as well
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Message>,
    ): MediatorResult {
        val loadKey: Long = when (loadType) {
            LoadType.REFRESH -> {
                val anchorMsgId =
                    state.anchorPosition?.let { state.closestItemToPosition(it)?.id }
                        ?: localDb.chatListDao().getLastMsgId(chatId)
                        ?: 0
                anchorMsgId
            }

            LoadType.PREPEND -> {
                val newestSeenMsg = state.firstItemOrNull()
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                newestSeenMsg.id
            }

            LoadType.APPEND -> {
                val oldestSeenMsg = state.lastItemOrNull()
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                oldestSeenMsg.id
            }
        }

        try {
            val isRefresh = loadType == LoadType.REFRESH
            val messages = api.getMessages(
                chatId,
                afterId = if (loadType == LoadType.PREPEND || isRefresh) loadKey else 0,
                beforeId = if (loadType == LoadType.APPEND || isRefresh) loadKey else 0
            )

            if (isRefresh) messageDao.clear(chatId)
            messageDao.insertAll(messages)
            return MediatorResult.Success(endOfPaginationReached = messages.isEmpty())
        } catch (e: IOException) {
            Log.e("MESSAGE_REMOTE_MEDIATOR", "IO: $e")
            return MediatorResult.Error(e)
        } catch (e: HttpException) {
            Log.e("MESSAGE_REMOTE_MEDIATOR", "Http: $e")
            return MediatorResult.Error(e)
        }
    }
}