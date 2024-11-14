package com.vladrip.ifchat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Chat {
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Length(max = 150)
    private String name;

    private String description;

    @Column(columnDefinition = "varchar(16)", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChatType type;

    @ToString.Exclude
    @OneToMany(mappedBy = "chat")
    private List<Message> messages;

    @ToString.Exclude
    @OneToMany(mappedBy = "chat")
    private List<ChatMember> chatMembers;

    public enum ChatType {
        PRIVATE,
        GROUP
    }
}
