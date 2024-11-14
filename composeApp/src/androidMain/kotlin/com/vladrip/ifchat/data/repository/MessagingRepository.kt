package com.vladrip.ifchat.data.repository

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.haroldadmin.cnradapter.executeWithRetry
import com.vladrip.ifchat.data.network.IFChatService
import com.vladrip.ifchat.data.network.model.StringWrapper
import kotlinx.coroutines.tasks.await
import org.koin.core.annotation.Single

@Single
class MessagingRepository(
    private val api: IFChatService,
) {

    suspend fun saveDeviceToken(deviceToken: String) {
        executeWithRetry(times = Int.MAX_VALUE, initialDelay = 1000L) {
            api.saveDeviceToken(StringWrapper(deviceToken))
        }
        Log.i(TAG, "saveDeviceToken($deviceToken)")
    }

    suspend fun deleteCurrentDeviceToken() {
        val deviceToken = Firebase.messaging.token.await()
        executeWithRetry(times = Int.MAX_VALUE, initialDelay = 1000L) {
            api.deleteDeviceToken(deviceToken)
        }
        Log.i(TAG, "deleteDeviceToken($deviceToken)")
    }

    companion object {
        private const val TAG = "MessagingRepository"
    }
}