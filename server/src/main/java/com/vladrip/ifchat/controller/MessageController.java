package com.vladrip.ifchat.controller;

import com.vladrip.ifchat.dto.MessageDto;
import com.vladrip.ifchat.entity.Message;
import com.vladrip.ifchat.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {
    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody MessageDto messageDto) {
        Message persistedMessage = messageService.create(messageDto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(persistedMessage.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        messageService.delete(id);
    }
}
