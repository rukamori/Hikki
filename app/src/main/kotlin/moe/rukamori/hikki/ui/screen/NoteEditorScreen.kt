package moe.rukamori.hikki.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.HorizontalRule
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Preview
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Redo
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.StrikethroughS
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.EditorMode
import moe.rukamori.hikki.domain.model.ScreenState
import moe.rukamori.hikki.ui.component.MarkdownPreview
import moe.rukamori.hikki.ui.component.StateContent
import moe.rukamori.hikki.ui.model.NoteEditorUiModel
import moe.rukamori.hikki.ui.theme.MdSpacing
import moe.rukamori.hikki.viewmodel.EditorEvent
import moe.rukamori.hikki.viewmodel.MarkdownFormat
import moe.rukamori.hikki.viewmodel.NoteEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    onBack: () -> Unit,
    onOpenPreview: (Long) -> Unit,
    viewModel: NoteEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val currentModel = when (val currentState = state) {
        is ScreenState.Success -> currentState.data
        else -> null
    }

    DisposableEffect(viewModel) {
        onDispose { viewModel.flushAutosave() }
    }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is EditorEvent.Message -> snackbarHostState.showSnackbar(context.getString(event.messageRes))
                is EditorEvent.Close -> {
                    snackbarHostState.showSnackbar(context.getString(event.messageRes))
                    onBack()
                }
            }
        }
    }
    BackHandler {
        viewModel.flushAutosave()
        onBack()
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.editor),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.flushAutosave()
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::togglePinned) {
                        Icon(
                            imageVector = Icons.Outlined.PushPin,
                            contentDescription = stringResource(R.string.toggle_pin),
                        )
                    }
                    IconButton(onClick = viewModel::toggleFavorite) {
                        Icon(
                            imageVector = Icons.Outlined.Favorite,
                            contentDescription = stringResource(R.string.toggle_favorite),
                        )
                    }
                    IconButton(onClick = { viewModel.flushAutosave() }) {
                        Icon(
                            imageVector = Icons.Outlined.Save,
                            contentDescription = stringResource(R.string.save),
                        )
                    }
                    IconButton(onClick = {
                        currentModel?.id?.let(onOpenPreview)
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Preview,
                            contentDescription = stringResource(R.string.preview),
                        )
                    }
                    IconButton(onClick = viewModel::moveToTrashAndClose) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.move_to_trash),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        StateContent(
            state = state,
            modifier = Modifier.padding(innerPadding),
            emptyTitleRes = R.string.error_note_missing,
            emptyMessageRes = R.string.error_note_missing,
        ) { model ->
            EditorContent(
                model = model,
                onTitleChange = viewModel::updateTitle,
                onContentChange = viewModel::updateContent,
                onModeChange = viewModel::setMode,
                onFormat = viewModel::insertMarkdown,
                onUndo = viewModel::undo,
                onRedo = viewModel::redo,
                onFolderChange = viewModel::assignFolder,
                onCategoryChange = viewModel::assignCategory,
                onTagToggle = viewModel::toggleTag,
                modifier = Modifier.padding(innerPadding),
            )
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
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val wide = maxWidth >= 720.dp
        Column(Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = model.title,
                onValueChange = onTitleChange,
                placeholder = { Text(stringResource(R.string.title)) },
                singleLine = true,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MdSpacing.sm),
            )
            EditorModeRow(
                selected = model.editorMode,
                onSelected = onModeChange,
                allowSplit = wide,
            )
            MetadataRow(
                model = model,
                onFolderChange = onFolderChange,
                onCategoryChange = onCategoryChange,
                onTagToggle = onTagToggle,
            )
            FormattingToolbar(
                canUndo = model.canUndo,
                canRedo = model.canRedo,
                onUndo = onUndo,
                onRedo = onRedo,
                onFormat = onFormat,
            )
            HorizontalDivider()
            when {
                model.editorMode == EditorMode.Preview -> MarkdownPreview(model.markdown)
                model.editorMode == EditorMode.Split && wide ->
                    Row(Modifier.fillMaxSize()) {
                        MarkdownEditor(
                            value = model.content,
                            onValueChange = onContentChange,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                        )
                        VerticalDivider(Modifier.fillMaxHeight())
                        MarkdownPreview(
                            markdown = model.markdown,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                        )
                    }
                else ->
                    MarkdownEditor(
                        value = model.content,
                        onValueChange = onContentChange,
                    )
            }
        }
    }
}

@Composable
private fun MarkdownEditor(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(stringResource(R.string.start_writing)) },
        modifier =
            modifier
                .fillMaxSize()
                .padding(MdSpacing.sm),
    )
}

@Composable
private fun EditorModeRow(
    selected: EditorMode,
    allowSplit: Boolean,
    onSelected: (EditorMode) -> Unit,
) {
    val modes = remember(allowSplit) { if (allowSplit) EditorMode.entries else listOf(EditorMode.Edit, EditorMode.Preview) }
    SingleChoiceSegmentedButtonRow(Modifier.padding(MdSpacing.sm)) {
        modes.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = selected == mode || selected == EditorMode.Split && !allowSplit && mode == EditorMode.Edit,
                onClick = { onSelected(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
            ) {
                Text(stringResource(mode.labelRes))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MetadataRow(
    model: NoteEditorUiModel,
    onFolderChange: (Long?) -> Unit,
    onCategoryChange: (Long?) -> Unit,
    onTagToggle: (Long) -> Unit,
) {
    var foldersExpanded by remember { mutableStateOf(false) }
    var categoriesExpanded by remember { mutableStateOf(false) }
    FlowRow(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = MdSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(MdSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(MdSpacing.xs),
    ) {
        androidx.compose.foundation.layout.Box {
            AssistChip(
                onClick = { foldersExpanded = true },
                label = {
                    Text(
                        model.folders.firstOrNull { it.id == model.selectedFolderId }?.name
                            ?: stringResource(R.string.no_folder),
                    )
                },
            )
            DropdownMenu(
                expanded = foldersExpanded,
                onDismissRequest = { foldersExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.no_folder)) },
                    onClick = {
                        foldersExpanded = false
                        onFolderChange(null)
                    },
                )
                model.folders.forEach { folder ->
                    DropdownMenuItem(
                        text = { Text(folder.name) },
                        onClick = {
                            foldersExpanded = false
                            onFolderChange(folder.id)
                        },
                    )
                }
            }
        }
        androidx.compose.foundation.layout.Box {
            AssistChip(
                onClick = { categoriesExpanded = true },
                label = {
                    Text(
                        model.categories.firstOrNull { it.id == model.selectedCategoryId }?.name
                            ?: stringResource(R.string.no_category),
                    )
                },
            )
            DropdownMenu(
                expanded = categoriesExpanded,
                onDismissRequest = { categoriesExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.no_category)) },
                    onClick = {
                        categoriesExpanded = false
                        onCategoryChange(null)
                    },
                )
                model.categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            categoriesExpanded = false
                            onCategoryChange(category.id)
                        },
                    )
                }
            }
        }
        model.tags.forEach { tag ->
            FilterChip(
                selected = tag.id in model.selectedTagIds,
                onClick = { onTagToggle(tag.id) },
                label = { Text(tag.name) },
            )
        }
        AssistChip(
            onClick = {},
            label = { Text(stringResource(R.string.editor_counts, model.wordCount, model.characterCount, model.readingMinutes)) },
        )
        AssistChip(
            onClick = {},
            label = {
                Text(
                    if (model.isSaving) {
                        stringResource(R.string.saving)
                    } else {
                        stringResource(R.string.saved)
                    },
                )
            },
        )
    }
}

@Composable
private fun FormattingToolbar(
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onFormat: (MarkdownFormat) -> Unit,
) {
    val scrollState = rememberScrollState()
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = MdSpacing.xs),
    ) {
        ToolbarButton(Icons.Outlined.Undo, R.string.undo, enabled = canUndo, onClick = onUndo)
        ToolbarButton(Icons.Outlined.Redo, R.string.redo, enabled = canRedo, onClick = onRedo)
        ToolbarButton(Icons.Outlined.Title, R.string.heading) { onFormat(MarkdownFormat.Heading) }
        ToolbarButton(Icons.Outlined.FormatBold, R.string.bold) { onFormat(MarkdownFormat.Bold) }
        ToolbarButton(Icons.Outlined.FormatItalic, R.string.italic) { onFormat(MarkdownFormat.Italic) }
        ToolbarButton(Icons.Outlined.StrikethroughS, R.string.strikethrough) { onFormat(MarkdownFormat.Strike) }
        ToolbarButton(Icons.Outlined.Code, R.string.inline_code) { onFormat(MarkdownFormat.InlineCode) }
        ToolbarButton(Icons.Outlined.Code, R.string.code_block) { onFormat(MarkdownFormat.CodeBlock) }
        ToolbarButton(Icons.Outlined.FormatQuote, R.string.quote) { onFormat(MarkdownFormat.Quote) }
        ToolbarButton(Icons.Outlined.FormatListBulleted, R.string.bullet_list) { onFormat(MarkdownFormat.BulletList) }
        ToolbarButton(Icons.Outlined.FormatListNumbered, R.string.ordered_list) { onFormat(MarkdownFormat.OrderedList) }
        ToolbarButton(Icons.Outlined.TaskAlt, R.string.task_list) { onFormat(MarkdownFormat.TaskList) }
        ToolbarButton(Icons.Outlined.Link, R.string.link) { onFormat(MarkdownFormat.Link) }
        ToolbarButton(Icons.Outlined.Image, R.string.image) { onFormat(MarkdownFormat.Image) }
        ToolbarButton(Icons.Outlined.TableChart, R.string.table) { onFormat(MarkdownFormat.Table) }
        ToolbarButton(Icons.Outlined.HorizontalRule, R.string.horizontal_rule) { onFormat(MarkdownFormat.Rule) }
    }
}

@Composable
private fun ToolbarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    labelRes: Int,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    IconButton(
        enabled = enabled,
        onClick = onClick,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(labelRes),
        )
    }
}
