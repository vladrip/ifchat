package com.vladrip.ifchat.ui.chat.info

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.vladrip.ifchat.data.Data
import com.vladrip.ifchat.data.repository.ChatRepository
import com.vladrip.ifchat.ui.shared.UiState
import kotlinx.coroutines.flow.map
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ChatInfoViewModel(
    savedStateHandle: SavedStateHandle,
    chatRepository: ChatRepository,
) : ViewModel() {
    val chatId: Long = savedStateHandle["chatId"]!!
    val members = chatRepository.getMembers(chatId).cachedIn(viewModelScope)
    val chatInfo = chatRepository.getChat(chatId).map {
        UiState.ChatInfo(if (it is Data.Success) it.payload.description else null)
    }
}