package com.vladrip.ifchat.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vladrip.ifchat.data.network.model.UserChatMemberDto

@Entity
data class Chat(
    @PrimaryKey val id: Long,
    val type: ChatType,
    val otherPersonUid: String? = null,
    val userChatMember: UserChatMemberDto? = null,
    val name: String? = "",
    val description: String? = "",
    val memberCount: Int = 2,
) {
    enum class ChatType {
        PRIVATE,
        GROUP,
    }
}
