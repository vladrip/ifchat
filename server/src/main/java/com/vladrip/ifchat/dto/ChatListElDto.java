package com.vladrip.ifchat.dto;

import com.vladrip.ifchat.entity.Chat;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class ChatListElDto {
    Long chatId;
    String chatName;
    Chat.ChatType chatType;
    Long lastMsgId;
    String lastMsgContent;
    LocalDateTime lastMsgSentAt;
    boolean isChatMuted;

    public static ChatListElDto of(ChatListElDto chatListElDto, String chatName) {
        return new ChatListElDto(chatListElDto.chatId, chatName, chatListElDto.chatType, chatListElDto.lastMsgId,
                chatListElDto.lastMsgContent, chatListElDto.lastMsgSentAt, chatListElDto.isChatMuted);
    }
}