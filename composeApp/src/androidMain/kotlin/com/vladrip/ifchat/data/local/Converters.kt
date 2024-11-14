package com.vladrip.ifchat.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.vladrip.ifchat.data.network.model.UserChatMemberDto
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

object Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneOffset.UTC) }
    }

    @TypeConverter
    fun toTimestamp(date: LocalDateTime?): Long? {
        return date?.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
    }

    @TypeConverter
    fun toJson(userChatMember: UserChatMemberDto?): String? {
        return gson.toJson(userChatMember)
    }

    @TypeConverter
    fun fromJson(userChatMember: String?): UserChatMemberDto? {
        return gson.fromJson(userChatMember, UserChatMemberDto::class.java)
    }

    //TODO: enum converters?
}