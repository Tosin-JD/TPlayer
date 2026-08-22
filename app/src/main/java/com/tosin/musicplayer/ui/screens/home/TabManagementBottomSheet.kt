package com.tosin.musicplayer.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tosin.musicplayer.ui.icons.AppIcons
import com.tosin.musicplayer.ui.state.LibraryTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabManagementBottomSheet(
    tab: LibraryTab,
    tabIndex: Int,
    totalTabs: Int,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onHideTab: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = tab.icon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(
                    text = "${tab.label} Tab Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            val canMoveLeft = tabIndex > 0
            ListItem(
                headlineContent = {
                    Text(
                        text = "Move to the Left",
                        color = if (canMoveLeft) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = AppIcons.ArrowBack,
                        contentDescription = "Move Left",
                        tint = if (canMoveLeft) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (canMoveLeft) {
                            Modifier.noRippleClick {
                                onMoveLeft()
                                onDismiss()
                            }
                        } else Modifier
                    )
            )

            val canMoveRight = tabIndex < totalTabs - 1
            ListItem(
                headlineContent = {
                    Text(
                        text = "Move to the Right",
                        color = if (canMoveRight) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = AppIcons.ArrowForward,
                        contentDescription = "Move Right",
                        tint = if (canMoveRight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (canMoveRight) {
                            Modifier.noRippleClick {
                                onMoveRight()
                                onDismiss()
                            }
                        } else Modifier
                    )
            )

            val canHide = totalTabs > 1
            ListItem(
                headlineContent = {
                    Text(
                        text = "Hide tab",
                        color = if (canHide) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = AppIcons.VisibilityOff,
                        contentDescription = "Hide Tab",
                        tint = if (canHide) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (canHide) {
                            Modifier.noRippleClick {
                                onHideTab()
                                onDismiss()
                            }
                        } else Modifier
                    )
            )
        }
    }
}

@Composable
private fun Modifier.noRippleClick(onClick: () -> Unit): Modifier = this.clickable(
    interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
    indication = null,
    onClick = onClick
)
