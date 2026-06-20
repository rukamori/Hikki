package moe.rukamori.hikki.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.rukamori.hikki.domain.model.DisplayMode
import moe.rukamori.hikki.ui.component.NoteCard
import moe.rukamori.hikki.ui.model.NoteCardUiModel
import moe.rukamori.hikki.ui.theme.MdSpacing

@Composable
fun NoteListContent(
    notes: List<NoteCardUiModel>,
    displayMode: DisplayMode,
    modifier: Modifier = Modifier,
    onOpenNote: (Long) -> Unit,
    onPin: (Long, Boolean) -> Unit,
    onFavorite: (Long, Boolean) -> Unit,
    onDuplicate: (Long) -> Unit,
    onArchive: (Long) -> Unit,
    onTrash: (Long) -> Unit,
    archiveLabelRes: Int = moe.rukamori.hikki.R.string.archive,
    trashLabelRes: Int = moe.rukamori.hikki.R.string.move_to_trash,
) {
    when (displayMode) {
        DisplayMode.List ->
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(MdSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(MdSpacing.sm),
            ) {
                items(
                    items = notes,
                    key = { it.id },
                    contentType = { "note_list_card" },
                ) { note ->
                    NoteCard(
                        note = note,
                        onOpen = onOpenNote,
                        onPin = onPin,
                        onFavorite = onFavorite,
                        onDuplicate = onDuplicate,
                        onArchive = onArchive,
                        onTrash = onTrash,
                        archiveLabelRes = archiveLabelRes,
                        trashLabelRes = trashLabelRes,
                    )
                }
            }
        DisplayMode.Grid ->
            LazyVerticalGrid(
                modifier = modifier.fillMaxSize(),
                columns = GridCells.Adaptive(240.dp),
                contentPadding = PaddingValues(MdSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(MdSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(MdSpacing.sm),
            ) {
                items(
                    items = notes,
                    key = { it.id },
                    contentType = { "note_grid_card" },
                ) { note ->
                    NoteCard(
                        note = note,
                        onOpen = onOpenNote,
                        onPin = onPin,
                        onFavorite = onFavorite,
                        onDuplicate = onDuplicate,
                        onArchive = onArchive,
                        onTrash = onTrash,
                        archiveLabelRes = archiveLabelRes,
                        trashLabelRes = trashLabelRes,
                    )
                }
            }
    }
}
