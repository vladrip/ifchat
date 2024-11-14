package com.vladrip.ifchat.ui.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.vladrip.ifchat.R
import com.vladrip.ifchat.ui.utils.sdp
import com.vladrip.ifchat.ui.utils.ssp

@Composable
fun PersonItem(upperText: String, lowerText: String, onClick: () -> Unit = {}) {
    Row(
        Modifier
            .height(48.sdp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painterResource(R.drawable.ic_launcher_foreground),
            stringResource(R.string.user_avatar)
        )
        Spacer(Modifier.width(8.sdp))
        Column {
            Text(upperText)
            Spacer(Modifier.height(2.sdp))
            Text(lowerText, color = MaterialTheme.colorScheme.secondary, fontSize = 12.ssp)
        }
    }
}