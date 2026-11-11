/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * Proprietary License
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package moe.rukamori.hikki.ui.screen

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.HorizontalRule
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Preview
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.VerticalSplit
import androidx.compose.material.icons.outlined.StrikethroughS
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.EditorMode
import moe.rukamori.hikki.domain.model.NoteLifecycle
import moe.rukamori.hikki.domain.model.ScreenState
import moe.rukamori.hikki.ui.component.ConnectedChoiceGroup
import moe.rukamori.hikki.ui.component.EmptyState
import moe.rukamori.hikki.ui.component.MarkdownPreview
import moe.rukamori.hikki.ui.component.StateContent
import moe.rukamori.hikki.ui.component.clockTime
import moe.rukamori.hikki.ui.model.NoteEditorUiModel
import moe.rukamori.hikki.ui.theme.MdSpacing
import moe.rukamori.hikki.ui.theme.tagAccentColor
import moe.rukamori.hikki.viewmodel.EditorEvent
import moe.rukamori.hikki.viewmodel.MarkdownFormat
import moe.rukamori.hikki.viewmodel.NoteEditorViewModel

private val EditorMaxWidth = 760.dp
private val SplitBreakpoint = 720.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    onBack: () -> Unit,
    onClosed: (Int) -> Unit,
    onOpenPreview: (Long) -> Unit,
    viewModel: NoteEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val model = (state as? ScreenState.Success)?.data
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    DisposableEffect(viewModel) {
        onDispose { viewModel.flushAutosave() }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        viewModel.flushAutosave()
    }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is EditorEvent.Message -> snackbarHostState.showSnackbar(context.getString(event.messageRes))
                is EditorEvent.Close -> onClosed(event.messageRes)
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.ime),
        topBar = {
            EditorTopBar(
                model = model,
                scrollBehavior = scrollBehavior,
                onBack = {
                    viewModel.flushAutosave()
                    onBack()
                },
                onSave = viewModel::flushAutosave,
                onTogglePin = viewModel::togglePinned,
                onToggleFavorite = viewModel::toggleFavorite,
                onOpenPreview = { model?.id?.let(onOpenPreview) },
                onArchive = viewModel::archiveAndClose,
                onRestore = viewModel::restore,
                onTrash = viewModel::moveToTrashAndClose,
            )
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
            emptyTitleRes = R.string.error_note_missing,
            emptyMessageRes = R.string.error_note_missing,
        ) { current ->
            EditorContent(
                model = current,
                onTitleChange = viewModel::updateTitle,
                onContentChange = viewModel::updateContent,
                onModeChange = viewModel::setMode,
                onFormat = viewModel::insertMarkdown,
                onUndo = viewModel::undo,
                onRedo = viewModel::redo,
                onFolderChange = viewModel::assignFolder,
                onCategoryChange = viewModel::assignCategory,
                onTagToggle = viewModel::toggleTag,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorTopBar(
    model: NoteEditorUiModel?,
    scrollBehavior: TopAppBarScrollBehavior,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenPreview: () -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit,
    onTrash: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    TopAppBar(
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(onClick = onBack, shapes = IconButtonDefaults.shapes()) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                )
            }
        },
        title = {
            if (model != null) {
                SaveStatusPill(
                    isSaving = model.isSaving,
                    lastSavedAt = model.lastSavedAt,
                    onSave = onSave,
                )
            }
        },
        actions = {
            if (model != null) {
                val pinLabel = stringResource(if (model.isPinned) R.string.unpin else R.string.pin)
                IconToggleButton(
                    checked = model.isPinned,
                    onCheckedChange = { onTogglePin() },
                    shapes = IconButtonDefaults.toggleableShapes(),
                    modifier = Modifier.semantics { contentDescription = pinLabel },
                ) {
                    Icon(
                        imageVector = if (model.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = null,
                    )
                }
                val favoriteLabel =
                    stringResource(if (model.isFavorite) R.string.remove_from_favorites else R.string.add_to_favorites)
                IconToggleButton(
                    checked = model.isFavorite,
                    onCheckedChange = { onToggleFavorite() },
                    shapes = IconButtonDefaults.toggleableShapes(),
                    modifier = Modifier.semantics { contentDescription = favoriteLabel },
                ) {
                    Icon(
                        imageVector = if (model.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                    )
                }
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        shapes = IconButtonDefaults.shapes(),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = stringResource(R.string.more_actions),
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        shape = MaterialTheme.shapes.large,
                    ) {
                        EditorMenuItem(Icons.Outlined.Preview, R.string.preview) {
                            menuExpanded = false
                            onOpenPreview()
                        }
                        if (model.lifecycle == NoteLifecycle.Archived) {
                            EditorMenuItem(Icons.Outlined.Restore, R.string.restore) {
                                menuExpanded = false
                                onRestore()
                            }
                        } else {
                            EditorMenuItem(Icons.Outlined.Archive, R.string.archive) {
                                menuExpanded = false
                                onArchive()
                            }
                        }
                        EditorMenuItem(Icons.Outlined.Delete, R.string.move_to_trash, destructive = true) {
                            menuExpanded = false
                            onTrash()
                        }
                    }
                }
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
    )
}

@Composable
private fun EditorMenuItem(
    icon: ImageVector,
    @StringRes labelRes: Int,
    destructive: Boolean = false,
    onClick: () -> Unit,
) {
    val color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    DropdownMenuItem(
        text = { Text(stringResource(labelRes), color = color) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = color) },
        onClick = onClick,
    )
}

@Composable
private fun SaveStatusPill(
    isSaving: Boolean,
    lastSavedAt: Long?,
    onSave: () -> Unit,
) {
    val motion = MaterialTheme.motionScheme
    val saveLabel = stringResource(R.string.save)
    Surface(
        onClick = onSave,
        shape = CircleShape,
        color = if (isSaving) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = if (isSaving) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.semantics { contentDescription = saveLabel },
    ) {
        AnimatedContent(
            targetState = isSaving,
            transitionSpec = {
                (fadeIn(motion.defaultEffectsSpec()) + scaleIn(motion.fastSpatialSpec(), initialScale = 0.85f)) togetherWith
                    fadeOut(motion.fastEffectsSpec())
            },
            label = "save_status",
        ) { saving ->
            Row(
                modifier = Modifier.padding(start = 10.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (saving) {
                    LoadingIndicator(
                        modifier = Modifier.size(20.dp),
                        color = LocalContentColor.current,
                    )
                    Text(
                        text = stringResource(R.string.saving),
                        style = MaterialTheme.typography.labelLargeEmphasized,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text =
                            if (lastSavedAt != null) {
                                stringResource(R.string.saved_at, clockTime(lastSavedAt))
                            } else {
                                stringResource(R.string.saved)
                            },
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorContent(
    model: NoteEditorUiModel,
    onTitleChange: (String) -> Unit,
    onContentChange: (TextFieldValue) -> Unit,
    onModeChange: (EditorMode) -> Unit,
    onFormat: (MarkdownFormat) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onFolderChange: (Long?) -> Unit,
    onCategoryChange: (Long?) -> Unit,
    onTagToggle: (Long) -> Unit,
) {
    val motion = MaterialTheme.motionScheme
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth >= SplitBreakpoint
        val mode =
            when {
                model.editorMode == EditorMode.Split && !wide -> EditorMode.Edit
                else -> model.editorMode
            }
        val modes = if (wide) EditorMode.entries else listOf(EditorMode.Edit, EditorMode.Preview)
        val bodyFocus = remember { FocusRequester() }
        val editorScroll = rememberScrollState()

        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Column(
                        modifier =
                            Modifier
                                .widthIn(max = EditorMaxWidth)
                                .fillMaxSize()
                                .verticalScroll(editorScroll)
                                .padding(horizontal = MdSpacing.md)
                                .padding(bottom = MdSpacing.lg),
                    ) {
                        EditorHeader(
                            model = model,
                            mode = mode,
                            modes = modes,
                            onTitleChange = onTitleChange,
                            onModeChange = onModeChange,
                            onFolderChange = onFolderChange,
                            onCategoryChange = onCategoryChange,
                            onTagToggle = onTagToggle,
                            onTitleDone = { bodyFocus.requestFocus() },
                        )
                        Spacer(Modifier.height(MdSpacing.sm))
                        AnimatedContent(
                            targetState = mode == EditorMode.Preview,
                            transitionSpec = {
                                fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec())
                            },
                            label = "editor_body",
                        ) { previewing ->
                            if (previewing) {
                                if (model.markdown.isBlank()) {
                                    EmptyState(
                                        titleRes = R.string.nothing_to_preview_title,
                                        messageRes = R.string.nothing_to_preview_message,
                                        icon = Icons.Outlined.Preview,
                                        modifier = Modifier.height(360.dp),
                                    )
                                } else {
                                    MarkdownPreview(
                                        markdown = model.markdown,
                                        scrollable = false,
                                        contentPadding = PaddingValues(0.dp),
                                    )
                                }
                            } else {
                                MarkdownBody(
                                    value = model.content,
                                    onValueChange = onContentChange,
                                    focusRequester = bodyFocus,
                                )
                            }
                        }
                    }
                }
                if (mode == EditorMode.Split) {
                    VerticalDivider(Modifier.fillMaxHeight())
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    ) {
                        if (model.markdown.isBlank()) {
                            EmptyState(
                                titleRes = R.string.nothing_to_preview_title,
                                messageRes = R.string.nothing_to_preview_message,
                                icon = Icons.Outlined.Preview,
                            )
                        } else {
                            MarkdownPreview(
                                markdown = model.markdown,
                                contentPadding = PaddingValues(MdSpacing.md),
                            )
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = mode != EditorMode.Preview,
                enter = slideInVertically(motion.defaultSpatialSpec()) { it } + fadeIn(motion.defaultEffectsSpec()),
                exit = slideOutVertically(motion.fastSpatialSpec()) { it } + fadeOut(motion.fastEffectsSpec()),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                FormattingToolbar(
                    canUndo = model.canUndo,
                    canRedo = model.canRedo,
                    onUndo = onUndo,
                    onRedo = onRedo,
                    onFormat = onFormat,
                    modifier =
                        Modifier
                            .widthIn(max = EditorMaxWidth)
                            .padding(horizontal = MdSpacing.sm, vertical = MdSpacing.xs + 4.dp),
                )
            }
        }
    }
}

@Composable
private fun EditorHeader(
    model: NoteEditorUiModel,
    mode: EditorMode,
    modes: List<EditorMode>,
    onTitleChange: (String) -> Unit,
    onModeChange: (EditorMode) -> Unit,
    onFolderChange: (Long?) -> Unit,
    onCategoryChange: (Long?) -> Unit,
    onTagToggle: (Long) -> Unit,
    onTitleDone: () -> Unit,
) {
    val titleStyle = MaterialTheme.typography.headlineMediumEmphasized.copy(color = MaterialTheme.colorScheme.onSurface)
    Column(verticalArrangement = Arrangement.spacedBy(MdSpacing.sm)) {
        BasicTextField(
            value = model.title,
            onValueChange = onTitleChange,
            textStyle = titleStyle,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            maxLines = 3,
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next,
                ),
            keyboardActions = KeyboardActions(onNext = { onTitleDone() }),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = MdSpacing.xs),
            decorationBox = { inner ->
                Box {
                    if (model.title.isEmpty()) {
                        Text(
                            text = stringResource(R.string.untitled_note),
                            style = titleStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    }
                    inner()
                }
            },
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MdSpacing.sm),
        ) {
            Text(
                text =
                    stringResource(
                        R.string.meta_separator,
                        pluralStringResource(R.plurals.words_count, model.wordCount, model.wordCount),
                        pluralStringResource(R.plurals.reading_minutes, model.readingMinutes, model.readingMinutes),
                    ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                modifier = Modifier.weight(1f),
            )
            val modeGroupLabel = stringResource(R.string.editor_mode)
            ConnectedChoiceGroup(
                options = modes,
                selected = mode,
                onSelect = onModeChange,
                label = { stringResource(it.labelRes) },
                icon = { it.icon },
                fill = false,
                showLabels = false,
                modifier = Modifier.semantics { contentDescription = modeGroupLabel },
            )
        }
        MetadataRow(
            model = model,
            onFolderChange = onFolderChange,
            onCategoryChange = onCategoryChange,
            onTagToggle = onTagToggle,
        )
    }
}

private val EditorMode.icon: ImageVector
    get() =
        when (this) {
            EditorMode.Edit -> Icons.Outlined.Edit
            EditorMode.Split -> Icons.Outlined.VerticalSplit
            EditorMode.Preview -> Icons.Outlined.Preview
        }

@Composable
private fun MetadataRow(
    model: NoteEditorUiModel,
    onFolderChange: (Long?) -> Unit,
    onCategoryChange: (Long?) -> Unit,
    onTagToggle: (Long) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(MdSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PickerChip(
            icon = Icons.Outlined.Folder,
            selectedLabel = model.folders.firstOrNull { it.id == model.selectedFolderId }?.name,
            emptyLabelRes = R.string.no_folder,
            options = model.folders.map { it.id to it.name },
            onSelect = onFolderChange,
        )
        PickerChip(
            icon = Icons.Outlined.Category,
            selectedLabel = model.categories.firstOrNull { it.id == model.selectedCategoryId }?.name,
            emptyLabelRes = R.string.no_category,
            options = model.categories.map { it.id to it.name },
            onSelect = onCategoryChange,
        )
        model.tags.forEach { tag ->
            val selected = tag.id in model.selectedTagIds
            FilterChip(
                selected = selected,
                onClick = { onTagToggle(tag.id) },
                label = { Text(tag.name) },
                shape = CircleShape,
                leadingIcon = {
                    Box(
                        Modifier
                            .size(FilterChipDefaults.IconSize / 2)
                            .clip(CircleShape)
                            .background(tagAccentColor(tag.colorSeed)),
                    )
                },
            )
        }
    }
}

@Composable
private fun PickerChip(
    icon: ImageVector,
    selectedLabel: String?,
    @StringRes emptyLabelRes: Int,
    options: List<Pair<Long, String>>,
    onSelect: (Long?) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val expandedLabel = stringResource(if (expanded) R.string.menu_expanded else R.string.menu_collapsed)
    Box {
        FilterChip(
            selected = selectedLabel != null,
            onClick = { expanded = true },
            label = {
                Text(
                    text = selectedLabel ?: stringResource(emptyLabelRes),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            shape = CircleShape,
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Outlined.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            },
            modifier = Modifier.semantics { stateDescription = expandedLabel },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = MaterialTheme.shapes.large,
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(emptyLabelRes)) },
                onClick = {
                    expanded = false
                    onSelect(null)
                },
            )
            options.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        expanded = false
                        onSelect(id)
                    },
                )
            }
        }
    }
}

@Composable
private fun MarkdownBody(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    focusRequester: FocusRequester,
) {
    val style =
        MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.15f,
        )
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = style,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        modifier =
            Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        decorationBox = { inner ->
            Box(Modifier.fillMaxWidth()) {
                if (value.text.isEmpty()) {
                    Text(
                        text = stringResource(R.string.start_writing),
                        style = style,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
                inner()
            }
        },
    )
}

private data class FormatTool(
    val icon: ImageVector,
    @param:StringRes val labelRes: Int,
    val format: MarkdownFormat,
)

private val FormatTools =
    listOf(
        FormatTool(Icons.Outlined.Title, R.string.heading, MarkdownFormat.Heading),
        FormatTool(Icons.Outlined.FormatBold, R.string.bold, MarkdownFormat.Bold),
        FormatTool(Icons.Outlined.FormatItalic, R.string.italic, MarkdownFormat.Italic),
        FormatTool(Icons.Outlined.StrikethroughS, R.string.strikethrough, MarkdownFormat.Strike),
        FormatTool(Icons.AutoMirrored.Outlined.FormatListBulleted, R.string.bullet_list, MarkdownFormat.BulletList),
        FormatTool(Icons.Outlined.FormatListNumbered, R.string.ordered_list, MarkdownFormat.OrderedList),
        FormatTool(Icons.Outlined.TaskAlt, R.string.task_list, MarkdownFormat.TaskList),
        FormatTool(Icons.Outlined.FormatQuote, R.string.quote, MarkdownFormat.Quote),
        FormatTool(Icons.Outlined.Code, R.string.inline_code, MarkdownFormat.InlineCode),
        FormatTool(Icons.Outlined.Code, R.string.code_block, MarkdownFormat.CodeBlock),
        FormatTool(Icons.Outlined.Link, R.string.link, MarkdownFormat.Link),
        FormatTool(Icons.Outlined.Image, R.string.image, MarkdownFormat.Image),
        FormatTool(Icons.Outlined.TableChart, R.string.table, MarkdownFormat.Table),
        FormatTool(Icons.Outlined.HorizontalRule, R.string.horizontal_rule, MarkdownFormat.Rule),
    )

@Composable
private fun FormattingToolbar(
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onFormat: (MarkdownFormat) -> Unit,
    modifier: Modifier = Modifier,
) {
    val formattingLabel = stringResource(R.string.formatting)
    HorizontalFloatingToolbar(
        expanded = true,
        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
        modifier = modifier.semantics { contentDescription = formattingLabel },
    ) {
        ToolbarButton(Icons.AutoMirrored.Outlined.Undo, R.string.undo, enabled = canUndo, onClick = onUndo)
        ToolbarButton(Icons.AutoMirrored.Outlined.Redo, R.string.redo, enabled = canRedo, onClick = onRedo)
        VerticalDivider(
            modifier =
                Modifier
                    .height(24.dp)
                    .padding(horizontal = MdSpacing.xxs),
            color = LocalContentColor.current.copy(alpha = 0.3f),
        )
        Row(
            modifier =
                Modifier
                    .weight(1f, fill = false)
                    .horizontalScroll(rememberScrollState()),
        ) {
            FormatTools.forEach { tool ->
                ToolbarButton(tool.icon, tool.labelRes) { onFormat(tool.format) }
            }
        }
    }
}

@Composable
private fun ToolbarButton(
    icon: ImageVector,
    @StringRes labelRes: Int,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    IconButton(
        enabled = enabled,
        onClick = onClick,
        shapes = IconButtonDefaults.shapes(),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(labelRes),
        )
    }
}
