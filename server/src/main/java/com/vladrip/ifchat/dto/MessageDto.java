package com.vladrip.ifchat.dto;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class MessageDto {
    Long id;
    Long chatId;
    LocalDateTime sentAt;
    PersonShortDto sender;
    String content;
}
