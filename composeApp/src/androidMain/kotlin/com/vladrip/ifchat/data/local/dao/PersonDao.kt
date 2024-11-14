package com.vladrip.ifchat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vladrip.ifchat.data.entity.Person
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(person: Person)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(persons: List<Person>)

    @Query("SELECT * FROM Person WHERE uid = :uid")
    fun get(uid: String): Flow<Person?>

    @Query("SELECT * FROM Person WHERE phoneNumber IN (:phoneNumbers)")
    fun getByPhoneNumbers(phoneNumbers: List<String>): List<Person>
}