package com.vladrip.ifchat.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    indices = [Index(
        value = ["phoneNumber"],
        unique = true
    ), Index(
        value = ["tag"],
        unique = true
    )]
)
data class Person(
    @PrimaryKey val uid: String,
    val phoneNumber: String? = null,
    val tag: String? = null,
    val displayName: String,
    val bio: String? = null,
    val lastOnline: LocalDateTime?,
)