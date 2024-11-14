package com.vladrip.ifchat.data.repository

import com.vladrip.ifchat.data.Data
import com.vladrip.ifchat.data.local.LocalDatabase
import com.vladrip.ifchat.data.network.IFChatService
import com.vladrip.ifchat.data.service.LocalContactsService
import com.vladrip.ifchat.data.utils.handleLocalRequest
import com.vladrip.ifchat.data.utils.handleNetworkRequest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.Single

@Single
class PersonRepository(
    private val api: IFChatService,
    localDb: LocalDatabase,
    private val contactsService: LocalContactsService,
) {
    private val personDao = localDb.personDao()

    fun getPerson(uid: String) = merge(
        handleLocalRequest { personDao.get(uid) },

        handleNetworkRequest { api.getPerson(uid) }.onEach { data ->
            if (data is Data.Success) {
                val person = data.payload
                personDao.insert(person)
            }
        }
    )

    fun getContacts() = flow {
        val phoneNumbers: List<String> = contactsService.getAllPhoneNumbers()

        val localRegisteredContacts = personDao.getByPhoneNumbers(phoneNumbers)
        if (localRegisteredContacts.isNotEmpty())
            emit(Data.Success(contactsService.withContactsData(localRegisteredContacts)))
        else emit(Data.Loading)

        handleNetworkRequest { api.getRegisteredPersons(phoneNumbers) }.collect { data ->
            if (data is Data.Success) {
                val registeredContacts = contactsService.withContactsData(data.payload)
                personDao.insertAll(registeredContacts)
                emit(Data.Success(registeredContacts))
            } else if (data is Data.NotSuccess) emit(data)
        }
    }
}