package com.vladrip.ifchat.ui.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.vladrip.ifchat.data.repository.ChatRepository
import com.vladrip.ifchat.ui.shared.UiState
import com.vladrip.ifchat.ui.utils.FormatHelper
import kotlinx.coroutines.flow.map
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ChatListViewModel(
    chatRepository: ChatRepository,
) : ViewModel() {
    val chatList = chatRepository.getChatList().map { pagingData ->
        pagingData.map { chatItem ->
            UiState.ChatItem(
                chatId = chatItem.chatId,
                chatType = chatItem.chatType,
                name = chatItem.chatName,
                lastMsgContent = chatItem.lastMsgContent,
                lastMsgSentAt = chatItem.lastMsgSentAt?.let { FormatHelper.formatLastSent(it) })
        }
    }.cachedIn(viewModelScope)
}
