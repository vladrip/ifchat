package com.vladrip.ifchat.controller;

import com.vladrip.ifchat.dto.ChatDto;
import com.vladrip.ifchat.dto.ChatListElDto;
import com.vladrip.ifchat.dto.ChatMemberShortDto;
import com.vladrip.ifchat.dto.MessageDto;
import com.vladrip.ifchat.service.ChatService;
import com.vladrip.ifchat.service.FirebaseService;
import com.vladrip.ifchat.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/chats")
public class ChatController {
    private final ChatService chatService;
    private final MessageService messageService;
    private final FirebaseService firebaseService;

    @GetMapping("/{id}")
    public ChatDto get(@PathVariable Long id, @RequestHeader(name = "Authorization") String authToken) {
        return chatService.getChat(id, firebaseService.uidFromToken(authToken));
    }

    @GetMapping
    public Page<ChatListElDto> getChatList(@RequestHeader(name = "Authorization") String authToken,
                                           @PageableDefault @ParameterObject Pageable pageable) {
        return chatService.getChatList(firebaseService.uidFromToken(authToken), pageable);
    }

    @GetMapping("/{id}/members")
    public Page<ChatMemberShortDto> getMembers(@PathVariable Long id,
                                               @PageableDefault @ParameterObject Pageable pageable) {
        return chatService.getMembers(id, pageable);
    }

    @GetMapping("/{id}/messages")
    public List<MessageDto> getMessages(@PathVariable Long id, Long beforeId, Long afterId, int limit) {
        return messageService.getAll(id, beforeId, afterId, limit);
    }
}