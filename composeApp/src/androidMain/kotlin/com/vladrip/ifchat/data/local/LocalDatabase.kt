package com.vladrip.ifchat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vladrip.ifchat.data.entity.Chat
import com.vladrip.ifchat.data.entity.ChatListItem
import com.vladrip.ifchat.data.entity.ChatMemberShort
import com.vladrip.ifchat.data.entity.Message
import com.vladrip.ifchat.data.entity.Person
import com.vladrip.ifchat.data.local.dao.ChatDao
import com.vladrip.ifchat.data.local.dao.ChatListDao
import com.vladrip.ifchat.data.local.dao.ChatMemberShortDao
import com.vladrip.ifchat.data.local.dao.MessageDao
import com.vladrip.ifchat.data.local.dao.PersonDao

@TypeConverters(Converters::class)
@Database(
    entities = [Chat::class, ChatListItem::class, ChatMemberShort::class,
        Message::class, Person::class],
    version = 1,
    exportSchema = false
)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun chatListDao(): ChatListDao
    abstract fun messageDao(): MessageDao
    abstract fun personDao(): PersonDao
    abstract fun chatMemberShortDao(): ChatMemberShortDao
}