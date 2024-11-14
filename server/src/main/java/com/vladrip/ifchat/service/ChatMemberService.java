package com.vladrip.ifchat.service;

import com.vladrip.ifchat.dto.UserChatMemberDto;
import com.vladrip.ifchat.entity.ChatMember;
import com.vladrip.ifchat.exception.ItemNotFoundException;
import com.vladrip.ifchat.mapping.Mapper;
import com.vladrip.ifchat.repository.ChatMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ChatMemberService {
    private final ChatMemberRepository chatMemberRepository;
    private final FirebaseService firebaseService;
    private final Mapper mapper;

    public UserChatMemberDto getChatMember(String uid, Long chatId) {
        return mapper.toUserChatMemberDto(retrieve(uid, chatId));
    }

    public UserChatMemberDto setChatMuted(String uid, Long chatId, Boolean value) {
        ChatMember chatMember = retrieve(uid, chatId);
        chatMember.setIsChatMuted(value);
        firebaseService.unsubscribeUserFromChat(uid, chatMember.getChat().getType(), chatId);
        return mapper.toUserChatMemberDto(chatMemberRepository.save(chatMember));
    }

    private ChatMember retrieve(String uid, Long chatId) {
        return chatMemberRepository.getByChatIdAndPersonUid(chatId, uid)
                .orElseThrow(() -> new ItemNotFoundException(ChatMember.class, "personUid:%s, chatId:%d", chatId, uid));
    }
}
