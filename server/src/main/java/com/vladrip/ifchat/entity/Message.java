package com.vladrip.ifchat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Message {
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Length(max = 4096)
    @NotNull
    private String content;

    @NotNull
    @Column(columnDefinition = "timestamp(3)")
    private LocalDateTime sentAt;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "sender_uid", columnDefinition = "varchar(128)")
    private Person sender;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chat chat;
}