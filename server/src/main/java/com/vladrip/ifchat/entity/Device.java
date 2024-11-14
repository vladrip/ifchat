package com.vladrip.ifchat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Device {
    @EqualsAndHashCode.Include
    @Id
    private String deviceToken;

    @Column(columnDefinition = "timestamp(3)")
    private LocalDateTime tokenTimestamp;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "person_uid", columnDefinition = "varchar(128)")
    private Person person;
}
