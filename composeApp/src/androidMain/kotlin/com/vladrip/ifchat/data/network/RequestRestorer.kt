package com.vladrip.ifchat.data.network

import com.vladrip.ifchat.data.entity.Message
import com.vladrip.ifchat.data.local.LocalDatabase
import com.vladrip.ifchat.data.repository.MessageRepository
import org.koin.core.annotation.Single

@Single
class RequestRestorer(
    private val messageRepository: MessageRepository,
    private val localDb: LocalDatabase,
) {
    private val inProgressMessageStatuses = listOf(
        Message.Status.SENDING, Message.Status.DELETING
    )

    suspend fun restoreRequests() {
        val messageDao = localDb.messageDao()
        val messages = messageDao
            .getFilteredByStatuses(inProgressMessageStatuses.map { it.name })
        messages.forEach {
            when (it.status) {
                Message.Status.SENDING -> messageRepository.save(it, false)
                Message.Status.DELETING -> messageRepository.delete(it.id)
                else -> {}
            }
        }
    }
}