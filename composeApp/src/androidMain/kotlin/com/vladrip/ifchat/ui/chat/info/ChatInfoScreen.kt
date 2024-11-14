package com.vladrip.ifchat.ui.chat.info

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.PagingData
import com.vladrip.ifchat.R
import com.vladrip.ifchat.data.entity.ChatMemberShort
import com.vladrip.ifchat.ui.chat.ChatViewModel
import com.vladrip.ifchat.ui.chat.info.tabs.ChatInfoTab
import com.vladrip.ifchat.ui.chat.info.tabs.ChatMembersTab
import com.vladrip.ifchat.ui.shared.Graph
import com.vladrip.ifchat.ui.shared.Status
import com.vladrip.ifchat.ui.shared.TopInfoBar
import com.vladrip.ifchat.ui.shared.UiState
import com.vladrip.ifchat.ui.utils.sdp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@Composable
fun ChatInfoScreen(
    viewModel: ChatInfoViewModel,
    chatViewModel: ChatViewModel,
    navController: NavController,
) {
    ChatInfoScreen(
        chatViewModel.chat,
        viewModel.chatInfo,
        viewModel.members,
        { navController.navigateUp() }
    ) { uid: String -> navController.navigate(Graph.PersonInfo.routeWithArgs(uid)) }
}

@Composable
fun ChatInfoScreen(
    chatFlow: Flow<UiState.Chat>,
    chatInfo: Flow<UiState.ChatInfo>,
    members: Flow<PagingData<ChatMemberShort>>,
    navUp: () -> Unit,
    navPersonInfo: (uid: String) -> Unit,
) {
    val tabData = listOf(stringResource(R.string.tab_info), stringResource(R.string.tab_members))
    val pagerState = rememberPagerState(pageCount = tabData::size)
    val chat by chatFlow.collectAsStateWithLifecycle(UiState.Chat(Status.LOADING))

    Scaffold(topBar = {
        TopInfoBar(chat.name, chat.shortInfo(), "", navBtnClick = navUp)
    }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabLayout(pagerState, tabData)
            TabContent(pagerState, chatInfo, members, navPersonInfo)
        }
    }
}

@Composable
fun TabLayout(pagerState: PagerState, tabData: List<String>) {
    val scope = rememberCoroutineScope()
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        divider = {
            Spacer(modifier = Modifier.height(5.sdp))
        },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        tabData.forEachIndexed { index, s ->
            Tab(
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                text = { Text(s) }
            )
        }
    }
}

@Composable
fun TabContent(
    pagerState: PagerState,
    chatInfo: Flow<UiState.ChatInfo>,
    members: Flow<PagingData<ChatMemberShort>>,
    navPersonInfo: (uid: String) -> Unit,
) {
    HorizontalPager(
        pagerState,
        Modifier.fillMaxHeight(),
        verticalAlignment = Alignment.Top
    ) { index ->
        when (index) {
            0 -> ChatInfoTab(chatInfo)
            1 -> ChatMembersTab(members, navPersonInfo)
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(showBackground = true)
@Composable
fun ChatInfoScreenPreview() {
    val mockMembers = listOf(
        ChatMemberShort(7, 4, "", "Barack", LocalDateTime.now()),
        ChatMemberShort(8, 4, "", "Donald", LocalDateTime.now().minusHours(6)),
        ChatMemberShort(9, 4, "", "Joe", LocalDateTime.now().minusMonths(2))
    )
    ChatInfoScreen(
        flowOf(UiState.Chat(Status.SUCCESS, "Some group", "23213 members")),
        flowOf(UiState.ChatInfo("Some dummy description")),
        flowOf(PagingData.from(mockMembers)),
        {}, {}
    )
}