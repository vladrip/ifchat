package com.vladrip.ifchat.controller;

import com.vladrip.ifchat.dto.BooleanWrapperDto;
import com.vladrip.ifchat.dto.PersonDto;
import com.vladrip.ifchat.dto.UserChatMemberDto;
import com.vladrip.ifchat.service.ChatMemberService;
import com.vladrip.ifchat.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/persons")
public class PersonController {
    private final PersonService personService;
    private final ChatMemberService chatMemberService;

    @GetMapping("/{uid}")
    public PersonDto get(@PathVariable String uid) {
        return personService.get(uid);
    }

    @PutMapping("{uid}/chats/{chatId}")
    public UserChatMemberDto getChatMember(@PathVariable String uid,
                                           @PathVariable Long chatId) {
        return chatMemberService.getChatMember(uid, chatId);
    }

    @PutMapping("/{uid}/chats/{chatId}/chat-muted")
    public UserChatMemberDto setChatMuted(@PathVariable String uid,
                                          @PathVariable Long chatId,
                                          @RequestBody BooleanWrapperDto booleanWrapper) {
        return chatMemberService.setChatMuted(uid, chatId, booleanWrapper.getValue());
    }
}
