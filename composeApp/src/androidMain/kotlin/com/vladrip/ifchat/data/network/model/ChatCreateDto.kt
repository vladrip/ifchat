package com.vladrip.ifchat.data.network.model

import com.vladrip.ifchat.data.entity.Chat

data class ChatCreateDto(
    val name: String,
    val type: Chat.ChatType,
    val members: List<String>?,
    val otherPersonUid: String?,
)