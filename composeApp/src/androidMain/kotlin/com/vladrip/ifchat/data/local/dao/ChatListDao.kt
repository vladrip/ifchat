package com.vladrip.ifchat.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vladrip.ifchat.data.entity.ChatListItem
import java.time.LocalDateTime

@Dao
interface ChatListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chatListItem: List<ChatListItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chatListItem: ChatListItem)

    @Query("SELECT * FROM ChatListItem ORDER BY lastMsgSentAt DESC")
    fun getOrderByLatestMsg(): PagingSource<Int, ChatListItem>

    @Query("SELECT lastMsgId FROM ChatListItem WHERE chatId = :chatId")
    suspend fun getLastMsgId(chatId: Long): Long?

    @Query(
        """
        UPDATE ChatListItem 
        SET lastMsgId = :msgId, lastMsgContent = :msgContent, lastMsgSentAt = :msgSentAt
        WHERE chatId = :chatId
    """
    )
    suspend fun updateLastMsg(chatId: Long,
                              msgId: Long, msgContent: String, msgSentAt: LocalDateTime)

    @Query("UPDATE ChatListItem SET isMuted = :isMuted WHERE chatId = :chatId")
    suspend fun updateIsMuted(chatId: Long, isMuted: Boolean)

    @Query("DELETE FROM ChatListItem")
    suspend fun clear()
}