package com.vladrip.ifchat.ui.main

import androidx.lifecycle.ViewModel
import androidx.room.withTransaction
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.vladrip.ifchat.data.Data
import com.vladrip.ifchat.data.local.LocalDatabase
import com.vladrip.ifchat.data.network.RequestRestorer
import com.vladrip.ifchat.data.repository.MessagingRepository
import com.vladrip.ifchat.data.repository.PersonRepository
import com.vladrip.ifchat.ui.shared.UiState
import kotlinx.coroutines.flow.map
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class MainViewModel(
    personRepository: PersonRepository,
    private val messagingRepository: MessagingRepository,
    private val requestRestorer: RequestRestorer,
    private val localDb: LocalDatabase,
    val gson: Gson,
) : ViewModel() {
    val person = personRepository.getPerson(Firebase.auth.uid!!).map {
        if (it is Data.Success) {
            val person = it.payload!!
            UiState.Person(displayName = person.displayName)
        } else UiState.Person(it.uiStatus())
    }

    suspend fun restoreRequests() {
        requestRestorer.restoreRequests()
    }

    suspend fun saveDeviceToken(token: String) {
        messagingRepository.saveDeviceToken(token)
    }

    suspend fun logout() {
        messagingRepository.deleteCurrentDeviceToken()
        localDb.withTransaction { localDb.clearAllTables() }
        Firebase.auth.signOut()
    }
}
