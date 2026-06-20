package moe.rukamori.hikki.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier

fun Modifier.clickableListItem(onClick: () -> Unit): Modifier = clickable(onClick = onClick)
