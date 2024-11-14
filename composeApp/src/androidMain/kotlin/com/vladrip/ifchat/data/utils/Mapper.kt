package com.vladrip.ifchat.data.utils

import com.vladrip.ifchat.data.entity.Chat
import com.vladrip.ifchat.data.network.model.ChatDto

object Mapper {
    fun toChat(chatDto: ChatDto): Chat =
        Chat(
            id = chatDto.id,
            otherPersonUid = chatDto.otherPerson?.uid,
            userChatMember = chatDto.userChatMember,
            type = chatDto.type,
            name = chatDto.name,
            description = chatDto.description,
            memberCount = chatDto.memberCount,
        )
}