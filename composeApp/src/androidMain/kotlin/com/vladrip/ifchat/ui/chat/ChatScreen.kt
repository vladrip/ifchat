package com.vladrip.ifchat.ui.chat

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import androidx.paging.insertSeparators
import androidx.paging.map
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.reflect.TypeToken
import com.vladrip.ifchat.R
import com.vladrip.ifchat.data.entity.Chat.ChatType
import com.vladrip.ifchat.data.entity.Message
import com.vladrip.ifchat.data.local.Converters
import com.vladrip.ifchat.theme.IFChatTheme
import com.vladrip.ifchat.ui.shared.ActionMenuItem
import com.vladrip.ifchat.ui.shared.Graph
import com.vladrip.ifchat.ui.shared.Status
import com.vladrip.ifchat.ui.shared.TopInfoBar
import com.vladrip.ifchat.ui.shared.UiState
import com.vladrip.ifchat.ui.utils.FormatHelper
import com.vladrip.ifchat.ui.utils.sdp
import com.vladrip.ifchat.ui.utils.ssp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDateTime

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = koinViewModel(),
    navController: NavController = rememberNavController(),
) {
    val scope = rememberCoroutineScope()
    val chat by viewModel.chat.collectAsStateWithLifecycle(UiState.Chat(Status.LOADING))
    val messagesState = rememberLazyListState()

    ChatScreen(
        messagesFlow = viewModel.messages,
        userUid = Firebase.auth.uid!!,
        chatType = viewModel.chatType,
        chat = chat,
        messagesState = messagesState,
        delete = { scope.launch { viewModel.deleteMessage(it) } },
        send = {
            scope.launch {
                viewModel.sendMessage(it)
                messagesState.animateScrollToItem(0)
            }
        },
        navUp = { navController.navigate(Graph.ChatList.route) },
        navChatInfo = {
            val route =
                if (chat.otherPersonUid != null) Graph.PersonInfo.routeWithArgs(chat.otherPersonUid!!)
                else Graph.Chat.Info.route
            navController.navigate(route)
        },
        muteChat = { scope.launch { viewModel.muteChat(it) } }
    )
}

@Composable
fun ChatScreen(
    messagesFlow: Flow<PagingData<UiModel>>,
    userUid: String,
    chatType: ChatType,
    chat: UiState.Chat,
    messagesState: LazyListState,
    delete: (id: Long) -> Unit,
    send: (text: String) -> Unit,
    navUp: () -> Unit,
    navChatInfo: () -> Unit,
    muteChat: (value: Boolean) -> Unit,
) {
    val chatMuted = chat.userChatMember?.isChatMuted == true
    val muteItem =
        ActionMenuItem.NeverShown(
            stringResource(if (chatMuted) R.string.unmute else R.string.mute),
            if (chatMuted) Icons.Outlined.Notifications else ImageVector.vectorResource(R.drawable.mute)
        ) { muteChat(!chatMuted) }
    val actions: List<ActionMenuItem> = listOf(
        ActionMenuItem.NeverShown(
            stringResource(R.string.search),
            Icons.Outlined.Search
        ) {},
        muteItem,
    )

    Scaffold(
        topBar = {
            TopInfoBar(chat.name, chat.shortInfo(), "", actions, navChatInfo, navUp)
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxHeight()
                .padding(innerPadding)
        ) {
            Box(
                Modifier
                    .weight(1f, true)
                    .padding(8.sdp, 0.dp)
            ) {
                MessageList(messagesFlow, userUid, chatType, messagesState, delete)
            }
            MessageInput(send)
        }
    }
}

@Composable
fun MessageList(
    messagesFlow: Flow<PagingData<UiModel>>,
    userUid: String,
    chatType: ChatType,
    messagesState: LazyListState,
    delete: (id: Long) -> Unit,
) {
    val messages = messagesFlow.collectAsLazyPagingItems()
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        messages.refresh()
    }

    LazyColumn(state = messagesState, reverseLayout = true, modifier = Modifier.fillMaxHeight()) {
        items(messages.itemCount, key = messages.itemKey {
            when (it) {
                is UiModel.MessageItem -> it.message.id
                is UiModel.DateSeparator -> it.formattedDate
            }
        }) { index ->
            messages[index]?.let {
                when (it) {
                    is UiModel.MessageItem -> {
                        val senderUid = it.message.sender.uid
                        val nextItem =
                            if (index > 0) messages[index - 1] else null
                        val prevItem =
                            if (index < messages.itemCount - 1) messages[index + 1] else null
                        val sameSenderAbove =
                            prevItem is UiModel.MessageItem && prevItem.message.sender.uid == senderUid
                        val sameSenderBelow =
                            nextItem is UiModel.MessageItem && nextItem.message.sender.uid == senderUid
                        MessageItem(
                            it.message,
                            senderUid == userUid,
                            sameSenderAbove,
                            sameSenderBelow,
                            chatType,
                            delete
                        )
                    }

                    is UiModel.DateSeparator -> DateSeparator(it)
                }
            }
        }
    }
}

@Composable
fun MessageItem(
    message: Message,
    isUser: Boolean,
    sameSenderAbove: Boolean,
    sameSenderBelow: Boolean,
    chatType: ChatType,
    delete: (id: Long) -> Unit,
) {
    var showMessageMenu by remember { mutableStateOf(false) }

    Row(Modifier.fillMaxWidth()) {
        if (isUser) Spacer(Modifier.weight(1f))
        Column(
            Modifier
                .padding(
                    PaddingValues(
                        0.dp,
                        if (sameSenderAbove) 1.sdp else 4.sdp,
                        0.dp,
                        if (sameSenderBelow) 1.sdp else 4.sdp
                    )
                )
                .clip(
                    RoundedCornerShape(
                        topStart = if (sameSenderAbove) 28f else 48f, topEnd = 48f,
                        bottomStart = if (isUser) 48f else if (sameSenderBelow) 28f else 0f,
                        bottomEnd = if (isUser && !sameSenderBelow) 0f else 48f
                    )
                )
                .background(if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer)
                .padding(8.sdp, 4.sdp)
                .widthIn(max = 250.sdp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { showMessageMenu = true },
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            if (chatType == ChatType.GROUP && !isUser && !sameSenderAbove)
                Text(message.sender.displayName ?: "", color = MaterialTheme.colorScheme.secondary)
            Text(message.content)

            Row(Modifier.align(Alignment.End), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    FormatHelper.formatMessageSentAt(message.sentAt),
                    fontSize = 10.ssp,
                    color = MaterialTheme.colorScheme.secondary
                )
                if (isUser) {
                    Icon(
                        painterResource(
                            when (message.status) {
                                Message.Status.SENDING -> R.drawable.message_sending
                                Message.Status.SENT -> R.drawable.message_sent
                                Message.Status.DELETING -> R.drawable.message_delete
                                else -> R.drawable.message_read
                            }
                        ), stringResource(R.string.message_status),
                        Modifier
                            .size(14.sdp)
                            .padding(4.sdp, 0.dp, 0.dp, 0.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = showMessageMenu,
                onDismissRequest = { showMessageMenu = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.delete)) },
                    leadingIcon = { Icon(painterResource(R.drawable.message_delete), "delete") },
                    onClick = { delete(message.id) }
                )
            }
        }
        if (!isUser) Spacer(Modifier.weight(1f))
    }
}

@Composable
fun DateSeparator(dateSeparator: UiModel.DateSeparator) {
    Text(
        dateSeparator.formattedDate,
        fontSize = 11.ssp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 6.sdp)
    )
}

@Composable
fun MessageInput(send: (text: String) -> Unit) {
    var messageInput by remember { mutableStateOf(TextFieldValue("")) }
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onPrimary)
    ) {
        TextField(value = messageInput, onValueChange = {
            if (it.text.length < 4096) messageInput = it
        }, maxLines = 5, modifier = Modifier.weight(1f))
        IconButton(
            onClick = {
                val message = messageInput.text
                if (message.isNotBlank()) {
                    send(message)
                    messageInput = TextFieldValue("")
                }
            },
            enabled = messageInput.text.isNotBlank(),
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Icon(
                painterResource(R.drawable.send),
                stringResource(R.string.send_message),
                Modifier.size(22.sdp)
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    val gson = GsonBuilder()
        .registerTypeAdapter(
            LocalDateTime::class.java,
            JsonDeserializer { json, _, _ -> Converters.fromTimestamp(json.asLong) },
        ).create()
    val messages = gson.fromJson<List<Message>>(
        LocalContext.current.assets.open("preview/messages.json").reader(),
        object : TypeToken<List<Message>>() {}.type
    ).sortedByDescending { it.sentAt }.subList(0, 3)

    val messagesFlow = flowOf(PagingData.from(messages))
        .map { pagingData ->
            pagingData.map { UiModel.MessageItem(it) }
        }.map {
            it.insertSeparators { after, before ->
                if (after == null) return@insertSeparators null
                else if (before == null || after.message.sentAt.dayOfYear != before.message.sentAt.dayOfYear)
                    UiModel.DateSeparator(FormatHelper.formatDateSeparator(after.message.sentAt))
                else null
            }
        }

    IFChatTheme {
        ChatScreen(messagesFlow, "cB4o1nENC4WIEhzYiNlNvyF2bj83", ChatType.GROUP,
            UiState.Chat(shortInfo = stringResource(R.string.loading)), rememberLazyListState(),
            {}, {}, {}, {}, {}
        )
    }
}
