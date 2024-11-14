package com.vladrip.ifchat.ui.shared


import com.vladrip.ifchat.data.entity.Chat.ChatType

sealed class Graph(val route: String) {
    data object Contacts : Graph("contacts")
    data object SavedMessages : Graph("saved-messages")

    data object Settings : Graph("settings")

    data object ChatList : Graph("chat-list")
    data object CreateChat : Graph("create-chat")

    data object Chat : Graph("chat/{chatId}?chatType={chatType}") {
        fun routeWithArgs(chatId: Long, chatType: ChatType): String {
            return "chat/${chatId}?chatType=${chatType}"
        }

        data object Messages: Graph("messages")
        data object Info : Graph("info")
    }

    data object PersonInfo : Graph("person/{uid}") {
        fun routeWithArgs(personUid: String): String {
            return "person/${personUid}"
        }
    }
}