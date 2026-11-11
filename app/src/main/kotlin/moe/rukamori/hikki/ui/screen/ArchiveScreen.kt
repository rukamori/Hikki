/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * GPL-3.0 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.hikki.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moe.rukamori.hikki.R
import moe.rukamori.hikki.ui.component.NoteAction
import moe.rukamori.hikki.ui.component.NoteTimeline
import moe.rukamori.hikki.viewmodel.ArchiveViewModel

@Composable
fun ArchiveScreen(
    onOpenNote: (Long) -> Unit,
    onOpenTrash: () -> Unit,
    resultMessageRes: Int?,
    onResultMessageShown: () -> Unit,
    viewModel: ArchiveViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleNotesLayout(
        titleRes = R.string.archive,
        state = state,
        events = viewModel.events,
        timeline = NoteTimeline.Archived,
        emptyTitleRes = R.string.archive_empty_title,
        emptyMessageRes = R.string.archive_empty_message,
        emptyIcon = Icons.Outlined.Archive,
        infoRes = R.string.archive_summary,
        infoIcon = Icons.Outlined.Inventory2,
        onOpen = { note -> onOpenNote(note.id) },
        noteActions = { note ->
            listOf(
                NoteAction(
                    icon = Icons.Outlined.Edit,
                    labelRes = R.string.open,
                    onClick = { onOpenNote(note.id) },
                ),
                NoteAction(
                    icon = Icons.Outlined.Unarchive,
                    labelRes = R.string.restore,
                    onClick = { viewModel.restore(note.id) },
                ),
                NoteAction(
                    icon = Icons.Outlined.Delete,
                    labelRes = R.string.move_to_trash,
                    destructive = true,
                    onClick = { viewModel.moveToTrash(note.id) },
                ),
            )
        },
        resultMessageRes = resultMessageRes,
        onResultMessageShown = onResultMessageShown,
        actions = {
            IconButton(onClick = onOpenTrash, shapes = IconButtonDefaults.shapes()) {
                Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.trash))
            }
        },
    )
}
