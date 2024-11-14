package com.vladrip.ifchat.ui.contacts.info

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.vladrip.ifchat.R
import com.vladrip.ifchat.data.Data
import com.vladrip.ifchat.data.entity.Chat
import com.vladrip.ifchat.theme.IFChatTheme
import com.vladrip.ifchat.ui.shared.Graph
import com.vladrip.ifchat.ui.shared.Status
import com.vladrip.ifchat.ui.shared.TopInfoBar
import com.vladrip.ifchat.ui.shared.UiState
import com.vladrip.ifchat.ui.utils.sdp
import com.vladrip.ifchat.ui.utils.ssp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PersonInfoScreen(
    viewModel: PersonInfoViewModel = koinViewModel(),
    navController: NavController,
) {
    val person by viewModel.person.collectAsStateWithLifecycle(UiState.Person(Status.LOADING))
    PersonInfoScreen(
        person,
        { navController.navigateUp() },
        { personUid: String -> viewModel.getOrCreatePrivateChat(personUid) }
    )
    { chatId ->
        //make sure navigation isn't called twice (because person gets changed and ui renders again)
        if (navController.currentDestination?.route == Graph.PersonInfo.route) {
            val prevChatId = try {
                navController
                    .getBackStackEntry(Graph.Chat.route).arguments?.getLong("chatId")
            } catch (_: IllegalArgumentException) {
                -1
            }
            if (prevChatId == chatId) navController.navigateUp()
            else navController.navigate(Graph.Chat.routeWithArgs(chatId, Chat.ChatType.PRIVATE))
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter") //needed for ui
@Composable
fun PersonInfoScreen(
    person: UiState.Person,
    navUp: () -> Unit,
    getChatFlow: (String) -> Flow<Data<Chat>>,
    navToChat: (chatId: Long) -> Unit,
) {
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopInfoBar(
                person.displayName(),
                person.lastOnline,
                navBtnClick = navUp,
                transparent = true
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                person.uid?.let { uid ->
                    scope.launch { getChatFlow(uid).collect {
                        if (it is Data.Success) {
                            navToChat(it.payload.id)
                        } else {
                            //TODO: snackbar
                        }
                    } }
                }
            }) {
                Icon(painterResource(R.drawable.chat), stringResource(R.string.chat))
            }
        }
    ) {
        Column(Modifier.fillMaxWidth()) {
            Image(
                painterResource(R.drawable.ic_launcher_foreground),
                stringResource(R.string.user_avatar),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop,
            )

            Column(Modifier.padding(16.sdp)) {
                LabeledField(stringResource(R.string.name), person.displayName)
                person.phoneNumber?.let { LabeledField(stringResource(R.string.phone_number), it) }
                person.bio?.let { LabeledField(stringResource(R.string.bio), it) }
                person.tag?.let { LabeledField(stringResource(R.string.tag), "@$it") }
            }
        }
    }
}

@Composable
fun LabeledField(label: String, value: String, last: Boolean = false) {
    Text(label, fontSize = 10.ssp, color = MaterialTheme.colorScheme.secondary)
    Text(value, fontSize = 12.ssp)
    if (!last) Spacer(Modifier.height(16.sdp))
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(showBackground = true)
@Composable
fun PersonInfoScreenPreview() {
    IFChatTheme {
        PersonInfoScreen(
            UiState.Person(
                Status.SUCCESS,
                "Barack Obama",
                "+12345678901",
                "obamus",
                "A bit of my biography",
                "last online at Mesozoic era"
            ),
            {}, { flowOf(Data.Success(Chat(1, Chat.ChatType.PRIVATE))) }
        ) {}
    }
}