package com.vladrip.ifchat.dto;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class PersonDto {
    String uid;
    String tag;
    String firstName;
    String lastName;
    LocalDateTime onlineAt;
}
