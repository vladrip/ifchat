package com.vladrip.ifchat.ui.chat.info.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.vladrip.ifchat.R
import com.vladrip.ifchat.ui.shared.UiState
import com.vladrip.ifchat.ui.utils.sdp
import com.vladrip.ifchat.ui.utils.ssp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

@Composable
fun ChatInfoTab(chatInfo: Flow<UiState.ChatInfo>) {
    val noDescription = stringResource(R.string.no_description_provided)
    Column(Modifier.padding(8.sdp)) {
        Text(stringResource(R.string.description), fontSize = 14.ssp)
        Spacer(Modifier.height(4.sdp))
        val description by chatInfo
            .transform<UiState.ChatInfo, String> { it.description }
            .collectAsState(noDescription)
        if (description == noDescription) {
            Text(description, color = Color.Gray)
        } else Text(description)
    }
}