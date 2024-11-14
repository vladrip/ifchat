package com.vladrip.ifchat.ui.createchat

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vladrip.ifchat.R
import com.vladrip.ifchat.theme.IFChatTheme
import com.vladrip.ifchat.ui.shared.Graph
import com.vladrip.ifchat.ui.shared.PersonItem
import com.vladrip.ifchat.ui.shared.Status
import com.vladrip.ifchat.ui.shared.TopInfoBar
import com.vladrip.ifchat.ui.shared.UiState
import com.vladrip.ifchat.ui.utils.FormatHelper
import com.vladrip.ifchat.ui.utils.PermissionHelper
import com.vladrip.ifchat.ui.utils.sdp
import com.vladrip.ifchat.ui.utils.ssp
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDateTime

@Composable
fun CreateChatScreen(
    viewModel: CreateChatViewModel = koinViewModel(),
    navController: NavController = rememberNavController(),
) {
    val contactsFlow =
        if (PermissionHelper.tryGrantReadContacts().value) viewModel.contacts
        else flowOf(UiState.Batch(Status.LOADING))
    val contacts by contactsFlow.collectAsStateWithLifecycle(UiState.Batch(Status.LOADING))
    val scope = rememberCoroutineScope()

    CreateChatScreen(
        contacts,
        viewModel.selectedMembers,
        { navController.navigateUp() }
    ) { name ->
        scope.launch {
            val chat = viewModel.createChat(name)
            if (chat != null) {
                navController.navigate(Graph.Chat.routeWithArgs(chat.id, chat.type)) {
                    popUpTo(Graph.ChatList.route)
                }
            } else {
                //TODO: snackbar
            }
        }
    }
}

@Composable
fun CreateChatScreen(
    contacts: UiState.Batch<UiState.Person>,
    selectedMembers: SnapshotStateList<UiState.Person>,
    navUp: () -> Unit,
    createAndNavChat: (name: String) -> Unit,
) {
    var name by remember { mutableStateOf(TextFieldValue("")) }

    Scaffold(topBar = {
        TopInfoBar(
            if (contacts.isSuccess()) stringResource(R.string.create_chat)
            else contacts.status.defaultMsg(true),
            navBtnClick = navUp
        )
    }, floatingActionButton = {
        if (selectedMembers.size > 0 && name.text.isNotEmpty()) {
            FloatingActionButton(onClick = { createAndNavChat(name.text) }) {
                Icon(Icons.Filled.Check, stringResource(R.string.create_chat))
            }
        }
    })
    { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            Row(Modifier.padding(0.dp, 8.sdp, 0.dp, 24.sdp), Arrangement.Center) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = stringResource(R.string.new_group),
                    modifier = Modifier.size(64.sdp)
                )
                Spacer(Modifier.width(4.sdp))
                TextField(
                    value = name,
                    onValueChange = { if (it.text.length < 256) name = it },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                    ),
                    placeholder = { Text(stringResource(R.string.group_name)) }
                )
            }

            if (selectedMembers.size == 0) {
                Text(
                    text = stringResource(R.string.add_members) + "...",
                    modifier = Modifier.padding(8.sdp),
                    fontSize = 13.ssp,
                    color = MaterialTheme.colorScheme.secondary
                )
            } else {
                LazyRow(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.sdp),
                    horizontalArrangement = Arrangement.spacedBy(4.sdp)
                ) {
                    items(selectedMembers.toList()) {
                        InputChip(
                            modifier = Modifier
                                .height(28.sdp)
                                .widthIn(60.sdp, 80.sdp),
                            selected = true,
                            onClick = { selectedMembers.remove(it) },
                            label = {
                                Text(
                                    it.displayName.split(' ')[0],
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            avatar = {
                                Image(
                                    painterResource(R.drawable.ic_launcher_foreground),
                                    stringResource(R.string.user_avatar)
                                )
                            }
                        )
                    }
                }
            }

            LazyColumn(Modifier.fillMaxSize()) {
                itemsIndexed(contacts.content) { index, contact ->
                    PersonItem(
                        contact.displayName,
                        contact.lastOnline
                    ) {
                        val i = selectedMembers.indexOf(contact)
                        if (i == -1) {
                            selectedMembers.add(contact)
                        } else selectedMembers.remove(contact)
                    }
                    if (index != contacts.content.size - 1) {
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(showBackground = true)
@Composable
fun CreateChatScreenPreview() {

    val mockContacts = listOf(
        UiState.Person(
            displayName = "John Cena",
            lastOnline = FormatHelper.lastOnline(LocalDateTime.now())
        ),
        UiState.Person(
            displayName = "Ryan Gosling",
            lastOnline = FormatHelper.lastOnline(LocalDateTime.now().minusHours(3))
        ),
        UiState.Person(
            displayName = "Tom Hanks",
            lastOnline = FormatHelper.lastOnline(LocalDateTime.now().minusDays(2))
        ),
        UiState.Person(
            displayName = "Brad Pitt",
            lastOnline = FormatHelper.lastOnline(LocalDateTime.now().minusYears(1))
        )
    )

    IFChatTheme {
        CreateChatScreen(
            UiState.Batch(content = mockContacts),
            remember { mutableStateListOf() },
            {}, {}
        )
    }
}