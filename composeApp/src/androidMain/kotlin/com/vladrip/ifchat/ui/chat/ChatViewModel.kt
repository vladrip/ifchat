package com.vladrip.ifchat.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vladrip.ifchat.R
import com.vladrip.ifchat.data.Data
import com.vladrip.ifchat.data.entity.Chat.ChatType
import com.vladrip.ifchat.data.entity.Message
import com.vladrip.ifchat.data.repository.ChatRepository
import com.vladrip.ifchat.data.repository.MessageRepository
import com.vladrip.ifchat.data.repository.PersonRepository
import com.vladrip.ifchat.ui.shared.UiState
import com.vladrip.ifchat.ui.shared.updateStatus
import com.vladrip.ifchat.ui.utils.FormatHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.android.annotation.KoinViewModel
import java.time.LocalDateTime

@SuppressLint("StaticFieldLeak") //lint doesn't know APP context is injected and thinks there is a leak
@KoinViewModel
class ChatViewModel(
    private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val personRepository: PersonRepository,
) : ViewModel() {
    val chatId: Long = savedStateHandle["chatId"]!!
    val chatType: ChatType = savedStateHandle["chatType"]!! //TODO: remove?

    val chat = chatRepository.getChat(chatId).map {
        if (it is Data.Success) {
            val chat = it.payload
            if (chat.type == ChatType.PRIVATE) {
                val otherPerson = chat.otherPersonUid?.let {
                    val data = personRepository.getPerson(chat.otherPersonUid)
                        .first { data -> data is Data.Success }
                    (data as Data.Success).payload
                }
                UiState.Chat(
                    name = otherPerson?.displayName ?: "",
                    shortInfo = FormatHelper.lastOnline(otherPerson?.lastOnline, context),
                    userChatMember = chat.userChatMember,
                    otherPersonUid = otherPerson?.uid,
                )
            } else {
                UiState.Chat(
                    name = chat.name ?: "",
                    shortInfo = context.getString(
                        R.string.group_members_count,
                        chat.memberCount
                    ),
                    userChatMember = chat.userChatMember,
                )
            }
        } else UiState.Chat(it.uiStatus())
    }.updateStatus()

    val messages = messageRepository.getMessagesByChatId(chatId)
        .map { pagingData -> pagingData.map { UiModel.MessageItem(it) } }
        .map {
            it.insertSeparators { after, before ->
                if (after == null) return@insertSeparators null
                else if (before == null || after.message.sentAt.dayOfYear != before.message.sentAt.dayOfYear)
                    UiModel.DateSeparator(FormatHelper.formatDateSeparator(after.message.sentAt))
                else null
            }
        }.cachedIn(viewModelScope)

    suspend fun sendMessage(text: String) {
        val uid = Firebase.auth.uid!!
        val message = Message(
            chatId = chatId,
            sentAt = LocalDateTime.now(),
            sender = Message.Sender(uid = uid),
            content = text,
            status = Message.Status.SENDING,
        )
        messageRepository.save(message, true)
    }

    suspend fun deleteMessage(id: Long) = messageRepository.delete(id)

    suspend fun muteChat(value: Boolean) = chatRepository.muteChat(chatId, value)
}

sealed class UiModel {
    data class MessageItem(val message: Message) : UiModel()
    data class DateSeparator(val formattedDate: String) : UiModel()
}
