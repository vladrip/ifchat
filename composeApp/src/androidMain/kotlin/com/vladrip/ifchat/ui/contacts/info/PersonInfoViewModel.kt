package com.vladrip.ifchat.ui.contacts.info

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.vladrip.ifchat.data.Data
import com.vladrip.ifchat.data.entity.Chat
import com.vladrip.ifchat.data.repository.ChatRepository
import com.vladrip.ifchat.data.repository.PersonRepository
import com.vladrip.ifchat.ui.shared.UiState
import com.vladrip.ifchat.ui.shared.updateStatus
import com.vladrip.ifchat.ui.utils.FormatHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.android.annotation.KoinViewModel

@SuppressLint("StaticFieldLeak") //lint doesn't know APP context is injected and thinks there is a leak
@KoinViewModel
class PersonInfoViewModel(
    private val context: Context,
    savedStateHandle: SavedStateHandle,
    personRepository: PersonRepository,
    private val chatRepository: ChatRepository,
) : ViewModel() {
    private val uid: String = savedStateHandle["uid"]!!
    val person = personRepository.getPerson(uid).map {
        if (it is Data.Success) {
            val person = it.payload!!
            UiState.Person(
                displayName = person.displayName,
                uid = person.uid,
                phoneNumber = person.phoneNumber,
                tag = person.tag,
                bio = person.bio,
                lastOnline = FormatHelper.lastOnline(person.lastOnline, context),
            )
        } else UiState.Person(it.uiStatus())
    }.updateStatus()

    fun getOrCreatePrivateChat(otherPersonUid: String): Flow<Data<Chat>> =
        chatRepository.getPrivateChat(otherPersonUid).map {
            if (it is Data.Error && it.body?.status == 404) {
                val chat = chatRepository.createChat(
                    Chat.ChatType.PRIVATE,
                    otherPersonUid = otherPersonUid
                )
                if (chat != null) Data.Success(chat) else it
            } else it
        }
}
