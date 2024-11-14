package com.vladrip.ifchat.ui.contacts

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vladrip.ifchat.R
import com.vladrip.ifchat.ui.shared.Graph
import com.vladrip.ifchat.ui.shared.PersonItem
import com.vladrip.ifchat.ui.shared.Status
import com.vladrip.ifchat.ui.shared.TopInfoBar
import com.vladrip.ifchat.ui.shared.UiState
import com.vladrip.ifchat.ui.utils.FormatHelper
import com.vladrip.ifchat.ui.utils.PermissionHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDateTime

@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel = koinViewModel(),
    navController: NavController = rememberNavController(),
) {
    val contactsFlow =
        if (PermissionHelper.tryGrantReadContacts().value) viewModel.contacts
        else flowOf(UiState.Batch(Status.LOADING))
    ContactsScreen(contactsFlow, { navController.navigateUp() })
    { uid: String -> navController.navigate(Graph.PersonInfo.routeWithArgs(uid)) }
}

@Composable
fun ContactsScreen(
    contactsFlow: Flow<UiState.Batch<UiState.Person>>,
    navUp: () -> Unit,
    navPersonInfo: (uid: String) -> Unit,
) {
    val contacts by contactsFlow.collectAsStateWithLifecycle(UiState.Batch(Status.LOADING))

    Scaffold(topBar = {
        TopInfoBar(
            if (contacts.isSuccess()) stringResource(R.string.contacts)
            else contacts.status.defaultMsg(true),
            navBtnClick = navUp
        )
    }) { innerPadding ->
        LazyColumn(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            itemsIndexed(contacts.content) { index, contact ->
                PersonItem(contact.displayName, contact.lastOnline) { navPersonInfo(contact.uid!!) }
                if (index != contacts.content.size - 1) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(showBackground = true)
@Composable
fun ContactsScreenPreview() {
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

    ContactsScreen(flowOf(UiState.Batch(content = mockContacts)), {}, {})
}
