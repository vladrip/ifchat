package com.vladrip.ifchat.ui.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.vladrip.ifchat.R
import com.vladrip.ifchat.data.network.model.UserChatMemberDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

sealed class UiState(open var status: Status = Status.SUCCESS) {
    fun isSuccess(): Boolean {
        return status == Status.SUCCESS
    }

    data class ChatItem(
        override var status: Status = Status.SUCCESS,
        val chatId: Long,
        val chatType: com.vladrip.ifchat.data.entity.Chat.ChatType,
        val name: String,
        val lastMsgContent: String? = null,
        val lastMsgSentAt: String? = null
    ) : UiState()

    data class Chat(
        override var status: Status = Status.SUCCESS,
        val name: String = "",
        val shortInfo: String? = null,
        val userChatMember: UserChatMemberDto? = null,
        val otherPersonUid: String? = null,
    ) : UiState() {
        @Composable
        fun shortInfo(): String? {
            return if (isSuccess()) shortInfo else status.defaultMsg()
        }
    }

    data class ChatInfo(
        val description: String? = null,
    ) : UiState()

    data class Person(
        override var status: Status = Status.SUCCESS,
        val displayName: String = "",
        val uid: String? = null,
        val phoneNumber: String? = null,
        val tag: String? = null,
        val bio: String? = null,
        val lastOnline: String = "",
    ) : UiState() {
        @Composable
        fun displayName(): String {
            return if (isSuccess()) displayName else status.defaultMsg()
        }
    }

    data class Batch<T : UiState>(
        override var status: Status = Status.SUCCESS,
        val content: List<T> = listOf(),
    ) : UiState()
}

enum class Status(private val msgId: Int) {
    SUCCESS(-1),
    LOADING(R.string.loading),
    NETWORK_ERROR(R.string.waiting_for_network),
    ERROR(R.string.unknown_error);

    @Composable
    fun defaultMsg(capitalize: Boolean = false): String {
        val msg = stringResource(msgId)
        return if (capitalize) msg.replaceFirstChar { it.uppercase() } else msg
    }
}

fun <T : UiState> Flow<T>.updateStatus() = flow {
    var prevData: T? = null
    collect { data ->
        if (data.status != Status.SUCCESS && prevData != null) prevData!!.status = data.status
        else prevData = data
        emit(prevData!!)
    }
}
