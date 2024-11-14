package com.vladrip.ifchat.data.service

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds
import android.provider.ContactsContract.PhoneLookup
import android.telephony.PhoneNumberUtils
import android.util.Patterns
import com.vladrip.ifchat.data.entity.Person
import org.koin.core.annotation.Single


@Single
class LocalContactsService(
    context: Context,
) {
    private val contentResolver = context.contentResolver

    fun getAllPhoneNumbers(): List<String> {
        val phoneNumbers = ArrayList<String>()
        contentResolver.query(
            CommonDataKinds.Phone.CONTENT_URI, null, null, null, null
        )?.use {
            while (it.moveToNext()) {
                @SuppressLint("Range")
                val phoneNumber =
                    it.getString(it.getColumnIndexOrThrow(CommonDataKinds.Phone.NUMBER))
                if (Patterns.PHONE.matcher(phoneNumber).matches())
                    phoneNumbers.add(PhoneNumberUtils.stripSeparators(phoneNumber))
            }
        }
        return phoneNumbers
    }

    fun withContactsData(registeredPersons: List<Person>): List<Person> {
        val contacts = mutableListOf<Person>()

        for (person in registeredPersons) {
            person.phoneNumber?.let {
                getContactData(it)?.let { contactData ->
                    contacts.add(
                        Person(
                            uid = person.uid,
                            phoneNumber = person.phoneNumber,
                            displayName = contactData.displayName,
                            lastOnline = person.lastOnline,
                            bio = person.bio,
                            tag = person.tag
                        )
                    )
                }
            }
        }

        return contacts
    }

    private fun getContactData(phoneNumber: String): ContactData? {
        val uri = Uri.withAppendedPath(
            PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(PhoneNumberUtils.normalizeNumber(phoneNumber))
        )

        return contentResolver.query(uri, arrayOf(PhoneLookup.DISPLAY_NAME), null, null, null)
            ?.use {
                if (it.moveToFirst()) {
                    val colI = it.getColumnIndex(PhoneLookup.DISPLAY_NAME)
                    val displayName = it.getString(colI)
                    ContactData(displayName)
                } else null
            }
    }

    data class ContactData(
        val displayName: String,
    )
}