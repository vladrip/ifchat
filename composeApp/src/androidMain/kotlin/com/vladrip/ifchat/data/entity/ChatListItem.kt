package com.vladrip.ifchat.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity
data class ChatListItem(
    @PrimaryKey val chatId: Long,
    val chatName: String,
    val chatType: Chat.ChatType,
    val lastMsgId: Long? = null,
    val lastMsgContent: String? = null,
    val lastMsgSentAt: LocalDateTime? = null,
    val isMuted: Boolean = false,
) {
    override fun toString(): String {
        return "chatId=$chatId"
    }
}