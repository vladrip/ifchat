package com.vladrip.ifchat.ui.shared

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vladrip.ifchat.R
import com.vladrip.ifchat.theme.IFChatTheme
import com.vladrip.ifchat.ui.utils.sdp
import com.vladrip.ifchat.ui.utils.ssp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopInfoBar(
    primaryText: String,
    secondaryText: String? = null,
    image: String? = null,
    actions: List<ActionMenuItem> = listOf(),
    onClick: (() -> Unit)? = null,
    navBtnClick: () -> Unit = {},
    transparent: Boolean = false,
    navBtn: @Composable () -> Unit = { NavBackIconButton(navBtnClick) },
) {
    var modifier = Modifier
        .height(48.sdp)
        .fillMaxWidth()
    if (onClick != null) modifier = modifier.clickable { onClick() }
    TopAppBar(
        title = {
            Row(
                modifier.background(Color.Transparent),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (image != null) {
                    Image(
                        painterResource(R.drawable.ic_launcher_foreground),
                        stringResource(R.string.user_avatar),
                    )
                }
                Spacer(Modifier.width(4.sdp))
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        primaryText,
                        fontSize = if (secondaryText == null) 16.ssp else 13.ssp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (secondaryText != null) {
                        Text(
                            secondaryText,
                            fontSize = 10.ssp,
                            color = MaterialTheme.colorScheme.secondary,
                            lineHeight = 16.ssp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        },
        navigationIcon = navBtn,
        actions = { ActionsMenu(actions) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (transparent) Color.Black.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    )
}

@Composable
fun NavBackIconButton(navUp: () -> Unit) {
    IconButton(onClick = navUp) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            stringResource(R.string.navigate_back)
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(showBackground = true)
@Composable
fun TopInfoBarPreview() {

    IFChatTheme {
        TopInfoBar("Group With Big Name", "10613 members", "", testAction)
    }
}

val testAction = listOf(
    ActionMenuItem.AlwaysShown(title = "Search", icon = Icons.Filled.Search),
    ActionMenuItem.NeverShown(title = "Settings"),
    ActionMenuItem.NeverShown(title = "About"),
)
