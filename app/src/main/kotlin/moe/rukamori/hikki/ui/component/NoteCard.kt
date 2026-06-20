package moe.rukamori.hikki.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import moe.rukamori.hikki.R
import moe.rukamori.hikki.ui.model.NoteCardUiModel
import moe.rukamori.hikki.ui.theme.MdSpacing

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun NoteCard(
    note: NoteCardUiModel,
    modifier: Modifier = Modifier,
    onOpen: (Long) -> Unit,
    onPin: (Long, Boolean) -> Unit,
    onFavorite: (Long, Boolean) -> Unit,
    onDuplicate: (Long) -> Unit,
    onArchive: (Long) -> Unit,
    onTrash: (Long) -> Unit,
    archiveLabelRes: Int = R.string.archive,
    trashLabelRes: Int = R.string.move_to_trash,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onOpen(note.id) },
                    onLongClick = { menuExpanded = true },
                ),
    ) {
        Column(Modifier.padding(MdSpacing.sm)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = note.title.ifBlank { stringResource(R.string.untitled_note) },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (note.isPinned) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (note.preview.isNotBlank()) {
                        Spacer(Modifier.height(MdSpacing.xs))
                        Text(
                            text = note.preview,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                IconButton(onClick = { onPin(note.id, !note.isPinned) }) {
                    Icon(
                        imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = stringResource(R.string.toggle_pin),
                    )
                }
                IconButton(onClick = { onFavorite(note.id, !note.isFavorite) }) {
                    Icon(
                        imageVector = if (note.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = stringResource(R.string.toggle_favorite),
                    )
                }
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = stringResource(R.string.more_actions),
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.duplicate)) },
                        onClick = {
                            menuExpanded = false
                            onDuplicate(note.id)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(archiveLabelRes)) },
                        onClick = {
                            menuExpanded = false
                            onArchive(note.id)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(trashLabelRes)) },
                        onClick = {
                            menuExpanded = false
                            onTrash(note.id)
                        },
                    )
                }
            }
            Spacer(Modifier.height(MdSpacing.xs))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(MdSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(MdSpacing.xs),
            ) {
                note.folderName?.let { label ->
                    AssistChip(onClick = {}, label = { Text(label) })
                }
                note.categoryName?.let { label ->
                    AssistChip(onClick = {}, label = { Text(label) })
                }
                note.tags.take(3).forEach { tag ->
                    AssistChip(onClick = {}, label = { Text(tag.name) })
                }
                Spacer(Modifier.width(MdSpacing.xxs))
                Text(
                    text = stringResource(R.string.word_count_short, note.wordCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
            }
        }
    }
}
