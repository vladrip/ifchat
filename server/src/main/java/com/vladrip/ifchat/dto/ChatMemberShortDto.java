package com.vladrip.ifchat.dto;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class ChatMemberShortDto {
    Long id;
    Long chatId;
    String firstName;
    String lastName;
    LocalDateTime onlineAt;
}
