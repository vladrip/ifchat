package com.vladrip.ifchat.repository;

import com.vladrip.ifchat.dto.ChatListElDto;
import com.vladrip.ifchat.entity.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    @Query("""
                SELECT new com.vladrip.ifchat.dto.ChatListElDto(c.id, c.name, c.type, m.id, m.content, m.sentAt, cm.isChatMuted)
                FROM Chat c
                INNER JOIN ChatMember cm ON c.id = cm.chat.id
                INNER JOIN Message m ON c.id = m.chat.id
                WHERE cm.person.uid = :personUid
                  AND m.sentAt in
                    (SELECT max(m2.sentAt)
                     FROM ChatMember cm2
                     INNER JOIN Message m2 ON cm2.chat.id = m2.chat.id
                     WHERE cm2.person.uid = :personUid
                     GROUP BY m2.chat.id)
                ORDER BY m.sentAt DESC
            """)
    Page<ChatListElDto> collectChatListByPersonId(String personUid, Pageable pageable);
}