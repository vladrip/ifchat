package com.vladrip.ifchat

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.vladrip.ifchat.data.service.MessagingService
import com.vladrip.ifchat.ui.auth.AuthActivity
import com.vladrip.ifchat.ui.main.MainScreen
import com.vladrip.ifchat.ui.main.MainViewModel
import com.vladrip.ifchat.ui.utils.PermissionHelper
import kotlinx.coroutines.tasks.await
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KoinContext {
                if (Firebase.auth.currentUser == null) {
                    return@KoinContext startAuthActivity(this)
                } else {
                    val mainNavController: NavHostController = rememberNavController()
                    val viewModel: MainViewModel = koinViewModel()
                    LaunchedEffect("onMainActivityStart") {
                        viewModel.restoreRequests()
                        viewModel.saveDeviceToken(Firebase.messaging.token.await())
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        PermissionHelper.tryGrantPostNotifications()
                    }
                    MessagingService.createNotificationChannel()

                    MainScreen(viewModel, mainNavController)
                }
            }
        }
    }

    companion object {
        fun startAuthActivity(context: Context) {
            val authIntent = Intent(context, AuthActivity::class.java)
                .setAction(Intent.ACTION_VIEW)
            authIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(context, authIntent, null)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}