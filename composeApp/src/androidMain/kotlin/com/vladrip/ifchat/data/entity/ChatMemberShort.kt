package com.vladrip.ifchat.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity
class ChatMemberShort(
    @PrimaryKey val id: Long,
    val chatId: Long,
    val personUid: String,
    val displayName: String,
    val lastOnline: LocalDateTime,
)