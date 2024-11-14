package com.vladrip.ifchat.repository;

import com.vladrip.ifchat.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("""
                select m from Message m where m.chat.id = :chatId
                and m.id < :beforeId order by m.id desc
            """)
    List<Message> getByChatIdAndBeforeId(Long chatId, Long beforeId, Pageable pageable);

    @Query("""
                select m from Message m where m.chat.id = :chatId
                and m.id > :afterId order by m.id asc
            """)
    List<Message> getByChatIdAndAfterId(Long chatId, Long afterId, Pageable pageable);

}