/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * Proprietary License
 */

package moe.rukamori.hikki.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.DisplayMode
import moe.rukamori.hikki.ui.component.EmptyState
import moe.rukamori.hikki.ui.component.NoteActionsSheet
import moe.rukamori.hikki.ui.component.NoteCollection
import moe.rukamori.hikki.ui.component.ResultMessageEffect
import moe.rukamori.hikki.ui.component.SectionHeader
import moe.rukamori.hikki.ui.component.StateContent
import moe.rukamori.hikki.ui.component.showMessage
import moe.rukamori.hikki.ui.model.SearchScope
import moe.rukamori.hikki.ui.model.SearchUiModel
import moe.rukamori.hikki.ui.theme.MdSpacing
import moe.rukamori.hikki.ui.theme.tagAccentColor
import moe.rukamori.hikki.viewmodel.SearchEvent
import moe.rukamori.hikki.viewmodel.SearchViewModel

private val SearchMaxWidth = 760.dp

@Composable
fun SearchScreen(
    onOpenNote: (Long) -> Unit,
    resultMessageRes: Int?,
    onResultMessageShown: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val textFieldState = rememberTextFieldState()

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }.collect(viewModel::updateQuery)
    }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SearchEvent.Message -> scope.showMessage(snackbarHostState, context.getString(event.messageRes))
            }
        }
    }
    ResultMessageEffect(snackbarHostState, resultMessageRes, onResultMessageShown)

    Scaffold(
        topBar = { SearchHeader(textFieldState) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        StateContent(
            state = state,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            emptyTitleRes = R.string.search_empty_title,
            emptyMessageRes = R.string.search_empty_message,
            emptyIcon = Icons.Outlined.Search,
        ) { model ->
            SearchContent(
                model = model,
                onOpenNote = onOpenNote,
                onScopeChange = viewModel::setScope,
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
private fun SearchHeader(textFieldState: TextFieldState) {
    val focusManager = LocalFocusManager.current
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                .padding(horizontal = MdSpacing.sm),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(Modifier.widthIn(max = SearchMaxWidth)) {
            Spacer(Modifier.height(MdSpacing.sm))
            Text(
                text = stringResource(R.string.search),
                style = MaterialTheme.typography.headlineLargeEmphasized,
                modifier = Modifier.padding(horizontal = MdSpacing.xxs),
            )
            Spacer(Modifier.height(MdSpacing.sm))
            TextField(
                state = textFieldState,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                placeholder = { Text(stringResource(R.string.search_notes)) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = textFieldState.text.isNotEmpty(),
                        enter = fadeIn(MaterialTheme.motionScheme.fastEffectsSpec()) + scaleIn(MaterialTheme.motionScheme.fastSpatialSpec()),
                        exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()) + scaleOut(MaterialTheme.motionScheme.fastSpatialSpec()),
                    ) {
                        IconButton(onClick = { textFieldState.clearText() }, shapes = IconButtonDefaults.shapes()) {
                            Icon(Icons.Outlined.Clear, contentDescription = stringResource(R.string.clear_search))
                        }
                    }
                },
                lineLimits = TextFieldLineLimits.SingleLine,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                onKeyboardAction = { focusManager.clearFocus() },
                shape = CircleShape,
                colors =
                    TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
            )
            Spacer(Modifier.height(MdSpacing.xs))
        }
    }
}

@Composable
private fun SearchContent(
    model: SearchUiModel,
    onOpenNote: (Long) -> Unit,
    onScopeChange: (SearchScope) -> Unit,
    onTogglePinned: (Long, Boolean) -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onDuplicate: (Long) -> Unit,
    onArchive: (Long) -> Unit,
    onTrash: (Long) -> Unit,
) {
    var actionNoteId by rememberSaveable { mutableStateOf<Long?>(null) }
    val actionNote = actionNoteId?.let { id -> model.notes.firstOrNull { it.id == id } }
    val hasScopes = model.folders.isNotEmpty() || model.tags.isNotEmpty()

    if (model.notes.isEmpty()) {
        Column(Modifier.fillMaxSize()) {
            if (hasScopes) {
                ScopeRow(model = model, onScopeChange = onScopeChange, horizontalInset = MdSpacing.sm)
            }
            EmptyState(
                titleRes = R.string.search_no_results_title,
                messageRes = R.string.search_scope_empty_message,
                message =
                    if (model.query.isBlank()) {
                        null
                    } else {
                        stringResource(R.string.search_no_results_message, model.query.trim())
                    },
                icon = Icons.Outlined.SearchOff,
                modifier = Modifier.weight(1f),
            )
        }
    } else {
        NoteCollection(
            notes = model.notes,
            displayMode = DisplayMode.List,
            onOpen = { onOpenNote(it.id) },
            onShowActions = { actionNoteId = it.id },
            contentPadding = PaddingValues(start = MdSpacing.sm, end = MdSpacing.sm, bottom = MdSpacing.xl),
            highlight = model.query.trim(),
            header = {
                if (hasScopes) {
                    item(key = "scopes", contentType = "scopes", span = StaggeredGridItemSpan.FullLine) {
                        Column {
                            SectionHeader(title = stringResource(R.string.search_browse))
                            ScopeRow(model = model, onScopeChange = onScopeChange, horizontalInset = 0.dp)
                        }
                    }
                }
                item(key = "summary", contentType = "section", span = StaggeredGridItemSpan.FullLine) {
                    if (model.query.isBlank()) {
                        SectionHeader(title = stringResource(R.string.search_all_notes), count = model.notes.size)
                    } else {
                        SectionHeader(
                            title = pluralStringResource(R.plurals.results_count, model.notes.size, model.notes.size),
                        )
                    }
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

@Composable
private fun ScopeRow(
    model: SearchUiModel,
    onScopeChange: (SearchScope) -> Unit,
    horizontalInset: Dp,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectableGroup()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = horizontalInset, vertical = MdSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(MdSpacing.xs),
    ) {
        FilterOption(
            label = stringResource(R.string.search_everywhere),
            selected = model.scope == SearchScope.Everywhere,
            onClick = { onScopeChange(SearchScope.Everywhere) },
        )
        model.folders.forEach { folder ->
            val scope = SearchScope.InFolder(folder.id)
            FilterOption(
                label = folder.name,
                selected = model.scope == scope,
                onClick = { onScopeChange(if (model.scope == scope) SearchScope.Everywhere else scope) },
                icon = { Icon(Icons.Outlined.Folder, contentDescription = null, Modifier.size(18.dp)) },
            )
        }
        model.tags.forEach { tag ->
            val scope = SearchScope.WithTag(tag.id)
            val accent = tagAccentColor(tag.colorSeed)
            FilterOption(
                label = tag.name,
                selected = model.scope == scope,
                onClick = { onScopeChange(if (model.scope == scope) SearchScope.Everywhere else scope) },
                icon = {
                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(accent),
                    )
                },
            )
        }
    }
}
