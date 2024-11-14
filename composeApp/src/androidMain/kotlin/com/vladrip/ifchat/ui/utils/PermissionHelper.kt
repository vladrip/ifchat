package com.vladrip.ifchat.ui.utils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

object PermissionHelper {

    @Composable
    private fun tryGrant(permission: String): MutableState<Boolean> {
        val result = remember { mutableStateOf(false) }

        if (ContextCompat.checkSelfPermission(
                LocalContext.current,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) result.value = true
        else {
            val permissionRequestLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                    result.value = it
                }
            LaunchedEffect(permissionRequestLauncher) {
                permissionRequestLauncher.launch(permission)
            }
        }

        return result
    }

    @Composable
    fun tryGrantReadContacts(): MutableState<Boolean> {
        return tryGrant(Manifest.permission.READ_CONTACTS)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Composable
    fun tryGrantPostNotifications(): MutableState<Boolean> {
        return tryGrant(Manifest.permission.POST_NOTIFICATIONS)
    }
}