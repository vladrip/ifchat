package com.vladrip.ifchat.ui.chat.info.tabs

import android.content.res.Configuration
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.vladrip.ifchat.data.entity.ChatMemberShort
import com.vladrip.ifchat.ui.shared.PersonItem
import com.vladrip.ifchat.ui.utils.FormatHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime

@Composable
fun ChatMembersTab(
    members: Flow<PagingData<ChatMemberShort>>,
    navPersonInfo: (uid: String) -> Unit,
) {
    ChatMemberList(members, navPersonInfo)
}

@Composable
fun ChatMemberList(
    members: Flow<PagingData<ChatMemberShort>>,
    navPersonInfo: (uid: String) -> Unit,
) {
    val lazyItems = members.collectAsLazyPagingItems()
    LazyColumn {
        items(lazyItems.itemCount, key = lazyItems.itemKey { it.id }) { index ->
            lazyItems[index]?.let {
                PersonItem(
                    it.displayName,
                    FormatHelper.lastOnline(it.lastOnline, LocalContext.current)
                ) { navPersonInfo(it.personUid) }
                if (index != lazyItems.itemCount - 1) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(showBackground = true)
@Composable
fun ChatMemberListPreview() {
    val mockMembers = listOf(
        ChatMemberShort(7, 4, "", "Barack", LocalDateTime.now()),
        ChatMemberShort(8, 4, "", "Donald", LocalDateTime.now().minusHours(6)),
        ChatMemberShort(9, 4, "", "Joe", LocalDateTime.now().minusMonths(2))
    )
    ChatMemberList(flowOf(PagingData.from(mockMembers))) {}
}