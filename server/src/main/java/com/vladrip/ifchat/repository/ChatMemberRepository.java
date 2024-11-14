package com.vladrip.ifchat.repository;

import com.vladrip.ifchat.entity.ChatMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {

    Page<ChatMember> getChatMembersByChatId(Long chatId, Pageable pageable);

    @Query("""
                select cm from ChatMember cm
                inner join Person p on p.uid = cm.person.uid
                where cm.chat.id = :chatId and p.uid != :personUid
            """)
    Optional<ChatMember> getOtherPrivateChatMember(Long chatId, String personUid);

    int countByChatId(Long chatId);

    List<ChatMember> findAllByPersonUid(String personUid);

    Optional<ChatMember> getByChatIdAndPersonUid(Long chatId, String personUid);
}