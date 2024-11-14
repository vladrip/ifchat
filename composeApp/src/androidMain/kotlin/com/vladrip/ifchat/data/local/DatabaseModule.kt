package com.vladrip.ifchat.data.local

import android.content.Context
import androidx.room.Room
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class DatabaseModule {

    @Single
    fun provideLocalDatabase(context: Context): LocalDatabase {
        return Room
            .databaseBuilder(context, LocalDatabase::class.java, "Local.db")
            .build()
    }
}