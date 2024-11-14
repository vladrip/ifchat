package com.vladrip.ifchat.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vladrip.ifchat.data.entity.ChatMemberShort

@Dao
interface ChatMemberShortDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chatMemberShort: ChatMemberShort)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chatMembersShort: List<ChatMemberShort>)

    @Query("SELECT * FROM ChatMemberShort WHERE chatId = :chatId ORDER BY lastOnline DESC")
    fun getOrderByMostRecentOnline(chatId: Long): PagingSource<Int, ChatMemberShort>

    @Query("DELETE FROM ChatMemberShort WHERE chatId = :chatId")
    suspend fun clear(chatId: Long)
}