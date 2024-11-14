package com.vladrip.ifchat.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.vladrip.ifchat.R
import com.vladrip.ifchat.data.entity.Message
import com.vladrip.ifchat.data.repository.MessageRepository
import com.vladrip.ifchat.data.repository.MessagingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MessagingService : FirebaseMessagingService() {
    private val messagingRepository: MessagingRepository by inject()
    private val messageRepository: MessageRepository by inject()
    private val gson: Gson by inject()
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onNewToken(token: String) {
        Log.i(TAG, "onNewToken($token)")
        scope.launch {
            while (Firebase.auth.currentUser == null) delay(1000L)
            messagingRepository.saveDeviceToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (message.data.isNotEmpty()) {
            val messageBody =
                gson.fromJson(message.data["message"], Message::class.java) ?: return
            scope.launch {
                messageRepository.saveMessageLocally(messageBody)
            }
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "MessagingService"

        @Composable
        fun createNotificationChannel(): NotificationChannel {
            val manager = NotificationManagerCompat.from(LocalContext.current)
            val name = stringResource(R.string.notifications_messages)
            var channel = manager.getNotificationChannel(name)
            if (channel == null) {
                channel = NotificationChannel(name, name, NotificationManager.IMPORTANCE_DEFAULT)
                manager.createNotificationChannel(channel)
            }
            return channel
        }
    }
}