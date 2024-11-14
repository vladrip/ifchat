package com.vladrip.ifchat.data.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.haroldadmin.cnradapter.NetworkResponse
import com.vladrip.ifchat.data.entity.Chat
import com.vladrip.ifchat.data.entity.ChatListItem
import com.vladrip.ifchat.data.local.LocalDatabase
import com.vladrip.ifchat.data.network.IFChatService
import com.vladrip.ifchat.exception.ServerException
import com.vladrip.ifchat.exception.WaitingForNetworkException

@OptIn(ExperimentalPagingApi::class)
class ChatListRemoteMediator(
    private val api: IFChatService,
    private val localDb: LocalDatabase,
) : RemoteMediator<Int, ChatListItem>() {
    private val chatListDao = localDb.chatListDao()
    private val chatDao = localDb.chatDao()

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ChatListItem>,
    ): MediatorResult {
        val nextPage: Int = when (loadType) {
            LoadType.REFRESH -> 0
            LoadType.PREPEND -> return MediatorResult.Success(true)
            LoadType.APPEND -> state.pages.size
        }

        return when (val response = api.getChatList(nextPage)) {
            is NetworkResponse.Success -> {
                val chatList = response.body.content
                localDb.withTransaction {
                    if (loadType == LoadType.REFRESH)
                        chatListDao.clear()
                    chatListDao.insertAll(chatList)
                    chatDao.insertAllFromChatList(chatList.map {
                        Chat(
                            id = it.chatId, type = it.chatType,
                            name = if (it.chatType == Chat.ChatType.PRIVATE) null else it.chatName
                        )
                    })
                }
                MediatorResult.Success(response.body.last)
            }

            is NetworkResponse.NetworkError -> MediatorResult.Error(WaitingForNetworkException())
            is NetworkResponse.ServerError -> MediatorResult.Error(
                ServerException("${response.body?.status}: ${response.body?.error}")
            )

            is NetworkResponse.UnknownError -> MediatorResult.Error(response.error)
        }
    }
}