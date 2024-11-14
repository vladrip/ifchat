package com.vladrip.ifchat.ui.chatlist

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vladrip.ifchat.R
import com.vladrip.ifchat.exception.WaitingForNetworkException
import com.vladrip.ifchat.theme.IFChatTheme
import com.vladrip.ifchat.ui.shared.Graph
import com.vladrip.ifchat.ui.shared.TopInfoBar
import com.vladrip.ifchat.ui.shared.UiState
import com.vladrip.ifchat.ui.utils.sdp
import com.vladrip.ifchat.ui.utils.ssp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalTime

@Composable
fun ChatListScreen(
    viewModel: ChatListViewModel = koinViewModel(),
    navController: NavController = rememberNavController(),
    drawerState: DrawerState,
) {
    val scope = rememberCoroutineScope()
    ChatListScreen(viewModel.chatList, navController) { scope.launch { drawerState.open() } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    chats: Flow<PagingData<UiState.ChatItem>>,
    navController: NavController,
    openDrawer: () -> Unit,
) {
    val lazyChats = chats.collectAsLazyPagingItems()
    val mediatorState = lazyChats.loadState.mediator?.refresh
    LaunchedEffect(mediatorState) {
        if (mediatorState is LoadState.Error && mediatorState.error is WaitingForNetworkException) {
            delay(1000)
            lazyChats.refresh()
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        lazyChats.refresh()
    }

    Scaffold(
        topBar = { ChatListTopBar(openDrawer, lazyChats.loadState) }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            ChatList(lazyChats, navController)
            NewChatButton(navController)
        }
    }
}

@Composable
fun ChatList(lazyChats: LazyPagingItems<UiState.ChatItem>, navController: NavController) {
    LazyColumn(Modifier.fillMaxSize()) {
        items(lazyChats.itemCount, key = lazyChats.itemKey { it.chatId }) { index ->
            lazyChats[index]?.let {
                ChatItem(it, navController)
                if (index != lazyChats.itemCount - 1) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun ChatItem(chatItemUiState: UiState.ChatItem, navController: NavController) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(56.sdp)
            .clickable {
                navController.navigate(
                    Graph.Chat.routeWithArgs(
                        chatItemUiState.chatId,
                        chatItemUiState.chatType
                    )
                )
            }
    ) {
        Image(
            painterResource(R.drawable.ic_launcher_foreground),
            stringResource(R.string.user_avatar)
        )
        Column(Modifier.padding(8.sdp, 0.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(chatItemUiState.name, Modifier.weight(1f, true))
                Text(
                    chatItemUiState.lastMsgSentAt ?: "",
                    fontSize = 10.ssp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(Modifier.height(2.sdp))
            Text(
                chatItemUiState.lastMsgContent ?: "",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun BoxScope.NewChatButton(navController: NavController) {
    var showMessageMenu by remember { mutableStateOf(false) }
    Box(
        Modifier
            .align(Alignment.BottomEnd)
            .padding(16.sdp)
    ) {
        FloatingActionButton(onClick = { showMessageMenu = true }, shape = CircleShape) {
            Icon(painterResource(R.drawable.create_chat), stringResource(R.string.create_chat))
        }
        DropdownMenu(expanded = showMessageMenu, onDismissRequest = { showMessageMenu = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.new_group)) },
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.group),
                        stringResource(R.string.new_group)
                    )
                },
                onClick = { navController.navigate(Graph.CreateChat.route) }
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(showBackground = true)
@Composable
fun ChatListScreenPreview() {
    val chatItems = Gson().fromJson<List<UiState.ChatItem>>(
        LocalContext.current.assets.open("preview/chat_items.json").reader(),
        object : TypeToken<List<UiState.ChatItem>>() {}.type
    ).sortedByDescending { LocalTime.parse(it.lastMsgSentAt) }
    val chatItemsFlow = flowOf(
        PagingData.from(chatItems)
    )
    IFChatTheme {
        ChatListScreen(chatItemsFlow, rememberNavController()) {}
    }
}

@ExperimentalMaterial3Api
@Composable
fun ChatListTopBar(openDrawer: () -> Unit, loadState: CombinedLoadStates) {
    var title by remember { mutableStateOf("") }

    val waitingForNetwork =
        stringResource(R.string.waiting_for_network).replaceFirstChar { it.uppercase() }
    val loading = stringResource(R.string.loading).replaceFirstChar { it.uppercase() }
    val mediatorState = loadState.mediator?.refresh
    if (mediatorState is LoadState.Error) {
        mediatorState.error
    } else if (mediatorState is LoadState.Loading && title != waitingForNetwork) {
        title = loading
    } else if (mediatorState is LoadState.NotLoading) {
        title = stringResource(R.string.app_name)
    }

    TopInfoBar(title) {
        IconButton(onClick = openDrawer) {
            Icon(Icons.Filled.Menu, stringResource(R.string.open_drawer))
        }
    }
}
