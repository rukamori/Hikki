package moe.rukamori.hikki.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.DisplayMode
import moe.rukamori.hikki.ui.component.StateContent
import moe.rukamori.hikki.viewmodel.ArchiveViewModel

@Composable
fun ArchiveScreen(
    onOpenNote: (Long) -> Unit,
    onOpenTrash: () -> Unit,
    viewModel: ArchiveViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.archive)) },
                actions = {
                    IconButton(onClick = onOpenTrash) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.trash),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        StateContent(
            state = state,
            modifier = Modifier.padding(innerPadding),
            emptyTitleRes = R.string.archive_empty_title,
            emptyMessageRes = R.string.archive_empty_message,
        ) { model ->
            NoteListContent(
                notes = model.notes,
                displayMode = DisplayMode.List,
                onOpenNote = onOpenNote,
                onPin = { _, _ -> },
                onFavorite = { _, _ -> },
                onDuplicate = {},
                onArchive = {},
                onTrash = viewModel::moveToTrash,
                trashLabelRes = R.string.move_to_trash,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}
