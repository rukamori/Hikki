package moe.rukamori.hikki.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.DisplayMode
import moe.rukamori.hikki.domain.model.NoteSort
import moe.rukamori.hikki.ui.component.StateContent
import moe.rukamori.hikki.ui.theme.MdSpacing
import moe.rukamori.hikki.viewmodel.HomeEvent
import moe.rukamori.hikki.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenNote: (Long) -> Unit,
    onOpenSearch: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.OpenEditor -> onOpenNote(event.noteId)
                is HomeEvent.Message -> snackbarHostState.showSnackbar(context.getString(event.messageRes))
            }
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.notes)) },
                actions = {
                    IconButton(onClick = onOpenSearch) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = stringResource(R.string.search),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::createNote,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                    )
                },
                text = { Text(stringResource(R.string.create_note)) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        StateContent(
            state = state,
            modifier = Modifier.padding(innerPadding),
            onEmptyAction = viewModel::createNote,
        ) { model ->
            androidx.compose.foundation.layout.Column(Modifier.padding(innerPadding)) {
                NotesToolbar(
                    displayMode = model.displayMode,
                    sort = model.sort,
                    onDisplayModeChange = viewModel::setDisplayMode,
                    onSortChange = viewModel::setSort,
                )
                NoteListContent(
                    notes = model.notes,
                    displayMode = model.displayMode,
                    onOpenNote = onOpenNote,
                    onPin = viewModel::togglePinned,
                    onFavorite = viewModel::toggleFavorite,
                    onDuplicate = viewModel::duplicate,
                    onArchive = viewModel::archive,
                    onTrash = viewModel::moveToTrash,
                )
            }
        }
    }
}

@Composable
fun NotesToolbar(
    displayMode: DisplayMode,
    sort: NoteSort,
    modifier: Modifier = Modifier,
    onDisplayModeChange: (DisplayMode) -> Unit,
    onSortChange: (NoteSort) -> Unit,
) {
    var sortExpanded by remember { mutableStateOf(false) }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = MdSpacing.sm, vertical = MdSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        SingleChoiceSegmentedButtonRow {
            DisplayMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = displayMode == mode,
                    onClick = { onDisplayModeChange(mode) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = DisplayMode.entries.size),
                ) {
                    Text(stringResource(mode.labelRes))
                }
            }
        }
        androidx.compose.foundation.layout.Box {
            TextButton(onClick = { sortExpanded = true }) {
                Text(stringResource(sort.labelRes))
            }
            DropdownMenu(
                expanded = sortExpanded,
                onDismissRequest = { sortExpanded = false },
            ) {
                NoteSort.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(stringResource(option.labelRes)) },
                        onClick = {
                            sortExpanded = false
                            onSortChange(option)
                        },
                    )
                }
            }
        }
    }
}
