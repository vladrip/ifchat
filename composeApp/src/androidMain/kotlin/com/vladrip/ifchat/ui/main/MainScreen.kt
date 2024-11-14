package com.vladrip.ifchat.ui.main

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.vladrip.ifchat.MainActivity
import com.vladrip.ifchat.R
import com.vladrip.ifchat.data.entity.Chat
import com.vladrip.ifchat.data.entity.Message
import com.vladrip.ifchat.ui.chat.ChatScreen
import com.vladrip.ifchat.ui.chat.ChatViewModel
import com.vladrip.ifchat.ui.chat.info.ChatInfoScreen
import com.vladrip.ifchat.ui.chat.info.ChatInfoViewModel
import com.vladrip.ifchat.ui.chatlist.ChatListScreen
import com.vladrip.ifchat.ui.contacts.ContactsScreen
import com.vladrip.ifchat.ui.contacts.info.PersonInfoScreen
import com.vladrip.ifchat.ui.createchat.CreateChatScreen
import com.vladrip.ifchat.ui.settings.SettingsScreen
import com.vladrip.ifchat.ui.shared.Graph
import com.vladrip.ifchat.ui.shared.Status
import com.vladrip.ifchat.ui.shared.UiState
import com.vladrip.ifchat.ui.utils.findActivity
import com.vladrip.ifchat.ui.utils.sdp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel = koinViewModel(),
    navController: NavHostController = rememberNavController(),
) {
    val scope = rememberCoroutineScope()
    MainScreen(viewModel.person, { scope.launch { viewModel.logout() } }, navController, viewModel.gson)
}

@Composable
fun MainScreen(
    personFlow: Flow<UiState.Person>,
    logout: () -> Unit,
    navController: NavHostController = rememberNavController(),
    gson: Gson,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    NavHost(
        navController = navController,
        startDestination = Graph.ChatList.route,
        modifier = Modifier.fillMaxSize(),
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
    ) {
        composable(Graph.Contacts.route) { ContactsScreen(navController = navController) }
        composable(Graph.SavedMessages.route) { }
        composable(Graph.Settings.route) { SettingsScreen() }

        composable(Graph.ChatList.route) {
            MainDrawer(drawerState, navController, personFlow, logout) {
                ChatListScreen(navController = navController, drawerState = drawerState)
            }
        }
        composable(Graph.CreateChat.route) { CreateChatScreen(navController = navController) }

        navigation(
            route = Graph.Chat.route,
            startDestination = Graph.Chat.Messages.route,
            arguments = listOf(
                navArgument("chatId") { type = NavType.LongType },
                navArgument("chatType") { type = NavType.EnumType(Chat.ChatType::class.java) }
            ),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) }
        ) {
            composable(Graph.Chat.Messages.route) {
                ChatScreen(navController = navController)
            }

            composable(Graph.Chat.Info.route) {
                val chatGraph = remember(it) {
                    navController.getBackStackEntry(Graph.Chat.route)
                }
                val chatScreen = remember(it) {
                    navController.getBackStackEntry(Graph.Chat.Messages.route)
                }

                val chatInfoViewModel: ChatInfoViewModel = koinViewModel(viewModelStoreOwner = chatGraph)
                val chatViewModel: ChatViewModel = koinViewModel(viewModelStoreOwner = chatGraph)
                ChatInfoScreen(chatInfoViewModel, chatViewModel, navController)
            }
        }

        composable(
            Graph.PersonInfo.route,
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) {
            PersonInfoScreen(navController = navController)
        }
    }

    LocalContext.current.findActivity()?.intent?.let { intent ->
        intent.getStringExtra("message")?.let {
            val chatId = gson.fromJson(it, Message::class.java).chatId
            val chatType: Chat.ChatType = Chat.ChatType
                .valueOf(intent.getStringExtra("chatType") ?: Chat.ChatType.GROUP.name)
            navController.navigate(Graph.Chat.routeWithArgs(chatId, chatType))
        }
    }
}

@Composable
fun MainDrawer(
    drawerState: DrawerState,
    navController: NavController,
    personFlow: Flow<UiState.Person>,
    logout: () -> Unit,
    content: @Composable () -> Unit,
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                val person by personFlow.collectAsStateWithLifecycle(UiState.Person(Status.LOADING))
                val scope = rememberCoroutineScope()
                val context = LocalContext.current

                Column(Modifier.padding(16.sdp)) {
                    Image(
                        painterResource(R.drawable.ic_launcher_foreground),
                        stringResource(R.string.user_avatar)
                    )
                    Text(person.displayName())
                    Text(Firebase.auth.currentUser?.phoneNumber ?: stringResource(R.string.loading))
                }
                HorizontalDivider()
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            painterResource(R.drawable.contacts),
                            stringResource(R.string.contacts)
                        )
                    },
                    label = { Text(stringResource(R.string.contacts)) },
                    selected = false,
                    shape = RectangleShape,
                    onClick = { navController.navigate(Graph.Contacts.route); scope.launch { drawerState.close() } }
                )
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            painterResource(R.drawable.saved_messages),
                            stringResource(R.string.saved_messages)
                        )
                    },
                    label = { Text(stringResource(R.string.saved_messages)) },
                    selected = false,
                    shape = RectangleShape,
                    onClick = { navController.navigate(Graph.SavedMessages.route); scope.launch { drawerState.close() } }
                )
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            painterResource(R.drawable.settings),
                            stringResource(R.string.settings)
                        )
                    },
                    label = { Text(stringResource(R.string.settings)) },
                    selected = false,
                    shape = RectangleShape,
                    onClick = { navController.navigate(Graph.Settings.route); scope.launch { drawerState.close() } }
                )
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            painterResource(R.drawable.logout),
                            stringResource(R.string.logout)
                        )
                    },
                    label = { Text(stringResource(R.string.logout)) },
                    selected = false,
                    shape = RectangleShape,
                    onClick = { logout(); MainActivity.startAuthActivity(context) }
                )
            }
        },
        content = content
    )
}
