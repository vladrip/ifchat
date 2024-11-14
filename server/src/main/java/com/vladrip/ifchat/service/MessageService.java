package com.vladrip.ifchat.service;

import com.vladrip.ifchat.dto.MessageDto;
import com.vladrip.ifchat.entity.Chat;
import com.vladrip.ifchat.entity.Message;
import com.vladrip.ifchat.entity.Person;
import com.vladrip.ifchat.exception.ItemNotFoundException;
import com.vladrip.ifchat.mapping.Mapper;
import com.vladrip.ifchat.repository.ChatRepository;
import com.vladrip.ifchat.repository.MessageRepository;
import com.vladrip.ifchat.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final PersonRepository personRepository;
    private final FirebaseService firebaseService;
    private final Mapper mapper;

    public List<MessageDto> getAll(Long chatId, Long beforeId, Long afterId, int limit) {
        boolean isRefresh = beforeId.equals(afterId);
        Pageable queryLimit = PageRequest.of(0, isRefresh ? limit : limit / 2);
        List<Message> messages = new ArrayList<>();

        if (beforeId != 0 || isRefresh)
            messages.addAll(messageRepository.getByChatIdAndBeforeId(chatId, beforeId, queryLimit));
        if (isRefresh)
            messageRepository.findById(beforeId).ifPresent(messages::add);
        if (afterId != 0 || isRefresh)
            messages.addAll(messageRepository.getByChatIdAndAfterId(chatId, afterId, queryLimit));

        return messages.stream()
                .map(mapper::toMessageDto)
                .collect(Collectors.toList());
    }

    public Message create(MessageDto messageDto) {
        Message message = mapper.toMessage(messageDto);
        Long chatId = messageDto.getChatId();
        String senderUid = messageDto.getSender().getUid();
        message.setChat(chatRepository.findById(chatId).orElseThrow(() -> new ItemNotFoundException(Chat.class, chatId)));
        message.setSender(personRepository.findById(senderUid).orElseThrow(() -> new ItemNotFoundException(Person.class, senderUid)));

        Message persistedMessage = messageRepository.save(message);
        firebaseService.sendFirebaseMessage(message);
        return persistedMessage;
    }

    public void delete(Long id) {
        messageRepository.deleteById(id);
    }
}