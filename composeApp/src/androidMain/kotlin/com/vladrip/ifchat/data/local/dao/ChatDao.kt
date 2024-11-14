package com.vladrip.ifchat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vladrip.ifchat.data.entity.Chat
import com.vladrip.ifchat.data.network.model.UserChatMemberDto
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllFromChatList(chats: List<Chat>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chat: Chat)

    @Query("SELECT * FROM Chat WHERE id = :id")
    fun get(id: Long): Flow<Chat>

    @Query("SELECT * FROM Chat WHERE otherPersonUid = :uid")
    fun getByOtherPersonUid(uid: String): Flow<Chat>

    @Query("UPDATE Chat SET userChatMember = :userChatMember WHERE id = :id")
    suspend fun updateUserChatMember(id: Long, userChatMember: UserChatMemberDto)

    @Query("UPDATE Chat SET otherPersonUid = :otherPersonUid WHERE id = :id")
    suspend fun updateOtherPersonUid(id: Long, otherPersonUid: String)
}