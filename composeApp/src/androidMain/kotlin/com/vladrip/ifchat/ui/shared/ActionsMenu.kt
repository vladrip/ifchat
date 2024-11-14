package com.vladrip.ifchat.ui.shared

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.vladrip.ifchat.theme.IFChatTheme

//implementation from https://fvilarino.medium.com/creating-a-reusable-actions-menu-in-jetpack-compose-95aec8eeb493
sealed interface ActionMenuItem {
    val title: String
    val icon: ImageVector?
    val contentDescription: String?
    val onClick: () -> Unit

    data class AlwaysShown(
        override val title: String,
        override val icon: ImageVector? = null,
        override val contentDescription: String? = title,
        override val onClick: () -> Unit = {},
    ) : ActionMenuItem

    data class NeverShown(
        override val title: String,
        override val icon: ImageVector? = null,
        override val contentDescription: String? = title,
        override val onClick: () -> Unit = {},
    ) : ActionMenuItem
}

@Composable
fun ActionsMenu(items: List<ActionMenuItem>) {
    var menuExpanded by remember { mutableStateOf(false) }
    val menuItems = remember(items) {
        splitMenuItems(items)
    }

    menuItems.alwaysShownItems.forEach { item ->
        IconButton(onClick = item.onClick) {
            Icon(
                imageVector = item.icon!!,
                contentDescription = item.contentDescription,
            )
        }
    }

    if (menuItems.overflowItems.isNotEmpty()) {
        IconButton(onClick = { menuExpanded = true }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "Overflow",
            )
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            menuItems.overflowItems.forEach { item ->
                DropdownMenuItem(
                    leadingIcon = { if (item.icon != null) { Icon(item.icon!!, item.icon!!.name) } },
                    text = { Text(item.title) },
                    onClick = item.onClick
                )
            }
        }
    }
}

private data class MenuItems(
    val alwaysShownItems: List<ActionMenuItem>,
    val overflowItems: List<ActionMenuItem>,
)

private fun splitMenuItems(items: List<ActionMenuItem>): MenuItems {
    val alwaysShownItems: MutableList<ActionMenuItem> =
        items.filterIsInstance<ActionMenuItem.AlwaysShown>().toMutableList()
    val overflowItems = items.filterIsInstance<ActionMenuItem.NeverShown>()

    return MenuItems(
        alwaysShownItems = alwaysShownItems,
        overflowItems = overflowItems,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(widthDp = 440, showBackground = true)
@Composable
private fun ActionMenuPreview(
    @PreviewParameter(ActionMenuParameterProvider::class) items: List<ActionMenuItem>
) {
    IFChatTheme {
        val numAlwaysShown = items.count { item -> item is ActionMenuItem.AlwaysShown }
        val numOverflow = items.count { item -> item is ActionMenuItem.NeverShown }
        val label = "Always: $numAlwaysShown Never: $numOverflow"
        TopAppBar(
            title = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            actions = { ActionsMenu(items = items) }
        )
    }
}

private class ActionMenuParameterProvider : PreviewParameterProvider<List<ActionMenuItem>> {
    override val values: Sequence<List<ActionMenuItem>>
        get() = sequenceOf(
            listOf(
                ActionMenuItem.AlwaysShown(
                    title = "Search",
                    icon = Icons.Filled.Search,
                ),
                ActionMenuItem.AlwaysShown(
                    title = "Favorite",
                    icon = Icons.Filled.FavoriteBorder,
                ),
                ActionMenuItem.AlwaysShown(
                    title = "Star",
                    icon = Icons.Filled.Star,
                ),
                ActionMenuItem.NeverShown(
                    title = "Settings",
                ),
                ActionMenuItem.NeverShown(
                    title = "About",
                ),
            ),
            listOf(
                ActionMenuItem.AlwaysShown(
                    title = "Search",
                    icon = Icons.Filled.Search,
                ),
                ActionMenuItem.AlwaysShown(
                    title = "Favorite",
                    icon = Icons.Filled.FavoriteBorder,
                ),
                ActionMenuItem.NeverShown(
                    title = "Settings",
                ),
                ActionMenuItem.NeverShown(
                    title = "About",
                ),
            ),
            listOf(
                ActionMenuItem.AlwaysShown(
                    title = "Search",
                    icon = Icons.Filled.Search,
                ),
                ActionMenuItem.NeverShown(
                    title = "Settings",
                ),
                ActionMenuItem.NeverShown(
                    title = "About",
                ),
            ),
            listOf(
                ActionMenuItem.NeverShown(
                    title = "Settings",
                ),
                ActionMenuItem.NeverShown(
                    title = "About",
                ),
            )
        )
}