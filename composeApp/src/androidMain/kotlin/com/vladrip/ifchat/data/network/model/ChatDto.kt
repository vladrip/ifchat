package com.vladrip.ifchat.data.network.model

import com.vladrip.ifchat.data.entity.Chat.ChatType
import com.vladrip.ifchat.data.entity.Person

data class ChatDto(
    val id: Long,
    val type: ChatType,
    val otherPerson: Person?,
    val userChatMember: UserChatMemberDto,
    val name: String,
    val description: String,
    val memberCount: Int,
)