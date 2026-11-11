/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * Proprietary License
 */

package moe.rukamori.hikki.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.ScreenState
import moe.rukamori.hikki.ui.component.ConfirmDestructiveDialog
import moe.rukamori.hikki.ui.component.NoteAction
import moe.rukamori.hikki.ui.component.NoteTimeline
import moe.rukamori.hikki.viewmodel.TrashViewModel

@Composable
fun TrashScreen(
    onBack: () -> Unit,
    viewModel: TrashViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pendingDeleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    val pendingNote =
        pendingDeleteId?.let { id -> (state as? ScreenState.Success)?.data?.notes?.firstOrNull { it.id == id } }

    LifecycleNotesLayout(
        titleRes = R.string.trash,
        state = state,
        events = viewModel.events,
        timeline = NoteTimeline.Trashed,
        emptyTitleRes = R.string.trash_empty_title,
        emptyMessageRes = R.string.trash_empty_message,
        emptyIcon = Icons.Outlined.Delete,
        infoRes = R.string.trash_info,
        infoIcon = Icons.Outlined.Info,
        onOpen = null,
        noteActions = { note ->
            listOf(
                NoteAction(
                    icon = Icons.Outlined.RestoreFromTrash,
                    labelRes = R.string.restore,
                    onClick = { viewModel.restore(note.id) },
                ),
                NoteAction(
                    icon = Icons.Outlined.DeleteForever,
                    labelRes = R.string.delete_forever,
                    destructive = true,
                    onClick = { pendingDeleteId = note.id },
                ),
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack, shapes = IconButtonDefaults.shapes()) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
            }
        },
    )

    if (pendingNote != null) {
        ConfirmDestructiveDialog(
            icon = Icons.Outlined.DeleteForever,
            title = stringResource(R.string.permanently_delete_note),
            message =
                stringResource(
                    R.string.permanently_delete_note_message,
                    pendingNote.title.ifBlank { stringResource(R.string.untitled_note) },
                ),
            confirmRes = R.string.delete_forever,
            onConfirm = { viewModel.permanentlyDelete(pendingNote.id) },
            onDismiss = { pendingDeleteId = null },
        )
    }
}
