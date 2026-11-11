/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * Proprietary License
 */

package moe.rukamori.hikki.ui.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material.icons.outlined.ViewModule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumExtendedFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.DisplayMode
import moe.rukamori.hikki.domain.model.NoteSort
import moe.rukamori.hikki.domain.model.ScreenState
import moe.rukamori.hikki.ui.component.EmptyState
import moe.rukamori.hikki.ui.component.NoteAction
import moe.rukamori.hikki.ui.component.NoteActionsSheet
import moe.rukamori.hikki.ui.component.NoteCollection
import moe.rukamori.hikki.ui.component.ResultMessageEffect
import moe.rukamori.hikki.ui.component.StateContent
import moe.rukamori.hikki.ui.component.showMessage
import moe.rukamori.hikki.ui.model.NoteCardUiModel
import moe.rukamori.hikki.ui.model.NoteFilter
import moe.rukamori.hikki.ui.model.NotesHomeUiModel
import moe.rukamori.hikki.ui.theme.MdSpacing
import moe.rukamori.hikki.viewmodel.HomeEvent
import moe.rukamori.hikki.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onOpenNote: (Long) -> Unit,
    onOpenSearch: () -> Unit,
    resultMessageRes: Int?,
    onResultMessageShown: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val gridState = rememberLazyStaggeredGridState()
    val fabExpanded by remember { derivedStateOf { gridState.firstVisibleItemIndex == 0 } }
    val model = (state as? ScreenState.Success)?.data

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.OpenEditor -> onOpenNote(event.noteId)
                is HomeEvent.Message -> scope.showMessage(snackbarHostState, context.getString(event.messageRes))
            }
        }
    }
    ResultMessageEffect(snackbarHostState, resultMessageRes, onResultMessageShown)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                subtitle = {
                    if (model != null) {
                        val notes = pluralStringResource(R.plurals.notes_count, model.totalCount, model.totalCount)
                        Text(
                            if (model.favoriteCount > 0) {
                                stringResource(
                                    R.string.meta_separator,
                                    notes,
                                    pluralStringResource(R.plurals.favorites_count, model.favoriteCount, model.favoriteCount),
                                )
                            } else {
                                notes
                            },
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSearch, shapes = IconButtonDefaults.shapes()) {
                        Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.search))
                    }
                    if (model != null) {
                        SortMenu(sort = model.sort, onSortChange = viewModel::setSort)
                        IconButton(
                            onClick = {
                                viewModel.setDisplayMode(
                                    if (model.displayMode == DisplayMode.List) DisplayMode.Grid else DisplayMode.List,
                                )
                            },
                            shapes = IconButtonDefaults.shapes(),
                        ) {
                            Icon(
                                imageVector =
                                    if (model.displayMode == DisplayMode.List) Icons.Outlined.ViewModule else Icons.Outlined.ViewAgenda,
                                contentDescription = stringResource(R.string.change_layout),
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            if (model != null) {
                MediumExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.create_note)) },
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    onClick = viewModel::createNote,
                    expanded = fabExpanded,
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        StateContent(
            state = state,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            onEmptyAction = viewModel::createNote,
        ) { current ->
            HomeContent(
                model = current,
                gridState = gridState,
                onOpenNote = onOpenNote,
                onFilterChange = viewModel::setFilter,
                onTogglePinned = viewModel::togglePinned,
                onToggleFavorite = viewModel::toggleFavorite,
                onDuplicate = viewModel::duplicate,
                onArchive = viewModel::archive,
                onTrash = viewModel::moveToTrash,
            )
        }
    }
}

@Composable
private fun HomeContent(
    model: NotesHomeUiModel,
    gridState: LazyStaggeredGridState,
    onOpenNote: (Long) -> Unit,
    onFilterChange: (NoteFilter) -> Unit,
    onTogglePinned: (Long, Boolean) -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onDuplicate: (Long) -> Unit,
    onArchive: (Long) -> Unit,
    onTrash: (Long) -> Unit,
) {
    var actionNoteId by rememberSaveable { mutableStateOf<Long?>(null) }
    val actionNote = actionNoteId?.let { id -> model.notes.firstOrNull { it.id == id } }

    if (model.notes.isEmpty()) {
        Column(Modifier.fillMaxSize()) {
            FilterRow(model = model, onFilterChange = onFilterChange)
            EmptyState(
                titleRes = R.string.filter_empty_title,
                messageRes = R.string.filter_empty_message,
                icon = if (model.filter is NoteFilter.InFolder) Icons.Outlined.Folder else Icons.Outlined.FavoriteBorder,
                actionRes = R.string.show_all_notes,
                actionIcon = Icons.Outlined.ViewAgenda,
                onAction = { onFilterChange(NoteFilter.All) },
                modifier = Modifier.weight(1f),
            )
        }
    } else {
        NoteCollection(
            notes = model.notes,
            displayMode = model.displayMode,
            onOpen = { onOpenNote(it.id) },
            onShowActions = { actionNoteId = it.id },
            state = gridState,
            contentPadding =
                PaddingValues(
                    start = MdSpacing.sm,
                    end = MdSpacing.sm,
                    bottom = 120.dp,
                ),
            splitPinned = model.sort != NoteSort.Favorite,
            header = {
                item(key = "filters", contentType = "filters", span = StaggeredGridItemSpan.FullLine) {
                    FilterRow(
                        model = model,
                        onFilterChange = onFilterChange,
                        horizontalInset = 0.dp,
                    )
                }
            },
        )
    }

    if (actionNoteId != null && actionNote == null) {
        LaunchedEffect(actionNoteId) { actionNoteId = null }
    }
    if (actionNote != null) {
        NoteActionsSheet(
            note = actionNote,
            actions = activeNoteActions(actionNote, onTogglePinned, onToggleFavorite, onDuplicate, onArchive, onTrash),
            onDismiss = { actionNoteId = null },
        )
    }
}

internal fun activeNoteActions(
    note: NoteCardUiModel,
    onTogglePinned: (Long, Boolean) -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onDuplicate: (Long) -> Unit,
    onArchive: (Long) -> Unit,
    onTrash: (Long) -> Unit,
): List<NoteAction> =
    listOf(
        NoteAction(
            icon = Icons.Outlined.PushPin,
            labelRes = if (note.isPinned) R.string.unpin else R.string.pin,
            onClick = { onTogglePinned(note.id, !note.isPinned) },
        ),
        NoteAction(
            icon = if (note.isFavorite) Icons.Outlined.FavoriteBorder else Icons.Outlined.Favorite,
            labelRes = if (note.isFavorite) R.string.remove_from_favorites else R.string.add_to_favorites,
            onClick = { onToggleFavorite(note.id, !note.isFavorite) },
        ),
        NoteAction(
            icon = Icons.Outlined.ContentCopy,
            labelRes = R.string.duplicate,
            onClick = { onDuplicate(note.id) },
        ),
        NoteAction(
            icon = Icons.Outlined.Archive,
            labelRes = R.string.archive,
            onClick = { onArchive(note.id) },
        ),
        NoteAction(
            icon = Icons.Outlined.Delete,
            labelRes = R.string.move_to_trash,
            destructive = true,
            onClick = { onTrash(note.id) },
        ),
    )

@Composable
private fun FilterRow(
    model: NotesHomeUiModel,
    onFilterChange: (NoteFilter) -> Unit,
    modifier: Modifier = Modifier,
    horizontalInset: Dp = MdSpacing.sm,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = horizontalInset, vertical = MdSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(MdSpacing.xs),
    ) {
        FilterOption(
            label = stringResource(R.string.filter_all),
            selected = model.filter == NoteFilter.All,
            onClick = { onFilterChange(NoteFilter.All) },
        )
        if (model.favoriteCount > 0 || model.filter == NoteFilter.Favorites) {
            FilterOption(
                label = stringResource(R.string.filter_favorites),
                selected = model.filter == NoteFilter.Favorites,
                onClick = { onFilterChange(NoteFilter.Favorites) },
                icon = { Icon(Icons.Outlined.Favorite, contentDescription = null, Modifier.size(FilterChipDefaults.IconSize)) },
            )
        }
        model.folders.forEach { folder ->
            val filter = NoteFilter.InFolder(folder.id)
            FilterOption(
                label = folder.name,
                selected = model.filter == filter,
                onClick = { onFilterChange(filter) },
                icon = { Icon(Icons.Outlined.Folder, contentDescription = null, Modifier.size(FilterChipDefaults.IconSize)) },
            )
        }
    }
}

@Composable
internal fun FilterOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: (@Composable () -> Unit)? = null,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, maxLines = 1) },
        leadingIcon =
            if (selected) {
                { Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(FilterChipDefaults.IconSize)) }
            } else {
                icon
            },
        colors =
            FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
    )
}

@Composable
private fun SortMenu(
    sort: NoteSort,
    onSortChange: (NoteSort) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }, shapes = IconButtonDefaults.shapes()) {
            Icon(Icons.Outlined.SwapVert, contentDescription = stringResource(R.string.sort_by))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            NoteSort.entries.forEach { option ->
                val selected = option == sort
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(option.labelRes),
                            style = if (selected) MaterialTheme.typography.labelLargeEmphasized else MaterialTheme.typography.labelLarge,
                        )
                    },
                    onClick = {
                        expanded = false
                        onSortChange(option)
                    },
                    trailingIcon = if (selected) {
                        { Icon(Icons.Filled.Check, contentDescription = null) }
                    } else {
                        null
                    },
                )
            }
        }
    }
}
