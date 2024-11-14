package com.vladrip.ifchat.ui.contacts

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import com.vladrip.ifchat.data.Data
import com.vladrip.ifchat.data.repository.PersonRepository
import com.vladrip.ifchat.ui.shared.UiState
import com.vladrip.ifchat.ui.shared.updateStatus
import com.vladrip.ifchat.ui.utils.FormatHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.koin.android.annotation.KoinViewModel

@SuppressLint("StaticFieldLeak") //lint doesn't know APP context is injected and thinks there is a leak
@KoinViewModel
class ContactsViewModel(
    private val context: Context,
    personRepository: PersonRepository,
) : ViewModel() {
    val contacts = personRepository.getContacts().map {
        if (it is Data.Success) {
            val contacts = it.payload
            UiState.Batch(content = contacts.map { contact ->
                UiState.Person(
                    uid = contact.uid,
                    displayName = contact.displayName,
                    lastOnline = FormatHelper.lastOnline(contact.lastOnline, context)
                )
            })
        } else UiState.Batch(it.uiStatus())
    }.updateStatus().flowOn(Dispatchers.IO)
}