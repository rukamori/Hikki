/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * GPL-3.0 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.hikki.ui.screen

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.Category
import moe.rukamori.hikki.domain.model.Folder
import moe.rukamori.hikki.domain.model.ScreenState
import moe.rukamori.hikki.ui.component.SectionHeader
import moe.rukamori.hikki.ui.component.ShapedIcon
import moe.rukamori.hikki.ui.component.StateContent
import moe.rukamori.hikki.ui.component.TagPill
import moe.rukamori.hikki.ui.component.showMessage
import moe.rukamori.hikki.ui.model.CollectionsUiModel
import moe.rukamori.hikki.ui.theme.MdSpacing
import moe.rukamori.hikki.viewmodel.CollectionsEvent
import moe.rukamori.hikki.viewmodel.CollectionsViewModel

private val FolderPolygons: List<RoundedPolygon>
    get() =
        listOf(
            MaterialShapes.Cookie9Sided,
            MaterialShapes.Clover4Leaf,
            MaterialShapes.Sunny,
            MaterialShapes.Cookie6Sided,
            MaterialShapes.Pentagon,
            MaterialShapes.Flower,
        )

@Composable
fun FoldersScreen(
    onOpenTags: () -> Unit,
    viewModel: CollectionsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var request by remember { mutableStateOf<CollectionRequest?>(null) }
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    val model = (state as? ScreenState.Success)?.data

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CollectionsEvent.Message -> scope.showMessage(snackbarHostState, context.getString(event.messageRes))
            }
        }
    }
    BackHandler(enabled = menuExpanded) { menuExpanded = false }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.workspace)) },
                subtitle = {
                    if (model != null) {
                        Text(
                            stringResource(
                                R.string.meta_separator,
                                pluralStringResource(R.plurals.folders_count, model.folders.size, model.folders.size),
                                pluralStringResource(R.plurals.categories_count, model.categories.size, model.categories.size),
                            ),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenTags, shapes = IconButtonDefaults.shapes()) {
                        Icon(Icons.AutoMirrored.Outlined.Label, contentDescription = stringResource(R.string.manage_tags))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            CreateMenu(
                expanded = menuExpanded,
                onExpandedChange = { menuExpanded = it },
                visible = state !is ScreenState.Loading,
                onCreate = { kind -> request = CollectionRequest.Create(kind) },
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
            emptyTitleRes = R.string.collections_empty_title,
            emptyMessageRes = R.string.collections_empty_message,
            emptyIcon = Icons.Outlined.FolderOpen,
            emptyActionRes = R.string.create_folder,
            onEmptyAction = { request = CollectionRequest.Create(CollectionKind.Folder) },
        ) { current ->
            WorkspaceGrid(
                model = current,
                onRequest = { request = it },
                onOpenTags = onOpenTags,
            )
        }
    }

    request?.let { current ->
        CollectionRequestDialog(
            request = current,
            viewModel = viewModel,
            onDismiss = { request = null },
        )
    }
}

@Composable
private fun CreateMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    visible: Boolean,
    onCreate: (CollectionKind) -> Unit,
) {
    val stateText = stringResource(if (expanded) R.string.menu_expanded else R.string.menu_collapsed)
    val toggleLabel = stringResource(R.string.open_create_menu)
    FloatingActionButtonMenu(
        expanded = expanded,
        button = {
            ToggleFloatingActionButton(
                checked = expanded,
                onCheckedChange = onExpandedChange,
                modifier =
                    Modifier
                        .semantics {
                            traversalIndex = -1f
                            stateDescription = stateText
                            contentDescription = toggleLabel
                        }
                        .animateFloatingActionButton(visible = visible || expanded, alignment = Alignment.BottomEnd),
            ) {
                val icon by remember { derivedStateOf { if (checkedProgress > 0.5f) Icons.Outlined.Close else Icons.Outlined.Add } }
                Icon(
                    painter = rememberVectorPainter(icon),
                    contentDescription = null,
                    modifier = Modifier.animateIcon({ checkedProgress }),
                )
            }
        },
    ) {
        CollectionKind.entries.forEach { kind ->
            FloatingActionButtonMenuItem(
                onClick = {
                    onExpandedChange(false)
                    onCreate(kind)
                },
                icon = { Icon(kind.createIcon, contentDescription = null) },
                text = { Text(stringResource(kind.createTitleRes)) },
            )
        }
    }
}

@Composable
private fun WorkspaceGrid(
    model: CollectionsUiModel,
    onRequest: (CollectionRequest) -> Unit,
    onOpenTags: () -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(156.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = MdSpacing.sm, end = MdSpacing.sm, bottom = 128.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        fullLine("folders_header") {
            SectionHeader(title = stringResource(R.string.folders), count = model.folders.size)
        }
        if (model.folders.isEmpty()) {
            fullLine("folders_none") { EmptyLine(stringResource(R.string.folders_none)) }
        } else {
            items(model.folders.withIndex().toList(), key = { "folder_${it.value.id}" }, contentType = { "folder" }) { (index, folder) ->
                FolderTile(
                    folder = folder,
                    noteCount = model.folderNoteCounts[folder.id] ?: 0,
                    index = index,
                    onRequest = onRequest,
                    modifier = Modifier.animateItem(),
                )
            }
        }
        fullLine("categories_header") {
            SectionHeader(
                title = stringResource(R.string.categories),
                count = model.categories.size,
                modifier = Modifier.padding(top = MdSpacing.xs),
            )
        }
        fullLine("categories") {
            if (model.categories.isEmpty()) {
                EmptyLine(stringResource(R.string.categories_none))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                    model.categories.forEachIndexed { index, category ->
                        CategoryRow(
                            category = category,
                            noteCount = model.categoryNoteCounts[category.id] ?: 0,
                            index = index,
                            count = model.categories.size,
                            onRequest = onRequest,
                        )
                    }
                }
            }
        }
        fullLine("tags") {
            TagsEntry(model = model, onOpenTags = onOpenTags, modifier = Modifier.padding(top = MdSpacing.xs))
        }
    }
}

private fun LazyGridScope.fullLine(
    key: String,
    content: @Composable () -> Unit,
) {
    item(key = key, contentType = key, span = { GridItemSpan(maxLineSpan) }) { content() }
}

@Composable
private fun EmptyLine(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = MdSpacing.xxs, vertical = MdSpacing.xs),
    )
}

@Composable
private fun FolderTile(
    folder: Folder,
    noteCount: Int,
    index: Int,
    onRequest: (CollectionRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val palette =
        when (index % 3) {
            0 -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
            1 -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
            else -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        }
    Box(modifier) {
        Surface(
            onClick = { menuOpen = true },
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(MdSpacing.sm)) {
                Row(verticalAlignment = Alignment.Top) {
                    ShapedIcon(
                        icon = Icons.Outlined.Folder,
                        polygon = FolderPolygons[index % FolderPolygons.size],
                        containerColor = palette.first,
                        contentColor = palette.second,
                        size = 56.dp,
                        iconSize = 26.dp,
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(MdSpacing.md))
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = pluralStringResource(R.plurals.notes_count, noteCount, noteCount),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        CollectionItemMenu(
            expanded = menuOpen,
            onDismiss = { menuOpen = false },
            onRename = { onRequest(CollectionRequest.Rename(CollectionKind.Folder, folder.id, folder.name)) },
            onDelete = { onRequest(CollectionRequest.Delete(CollectionKind.Folder, folder.id, folder.name)) },
        )
    }
}

@Composable
private fun CategoryRow(
    category: Category,
    noteCount: Int,
    index: Int,
    count: Int,
    onRequest: (CollectionRequest) -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Box {
        SegmentedListItem(
            onClick = { menuOpen = true },
            shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
            leadingContent = {
                ShapedIcon(
                    icon = Icons.Outlined.Category,
                    polygon = MaterialShapes.Cookie4Sided,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    size = 40.dp,
                    iconSize = 20.dp,
                )
            },
            supportingContent = { Text(pluralStringResource(R.plurals.notes_count, noteCount, noteCount)) },
            trailingContent = { Icon(Icons.Outlined.MoreVert, contentDescription = null) },
        ) {
            Text(category.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        CollectionItemMenu(
            expanded = menuOpen,
            onDismiss = { menuOpen = false },
            onRename = { onRequest(CollectionRequest.Rename(CollectionKind.Category, category.id, category.name)) },
            onDelete = { onRequest(CollectionRequest.Delete(CollectionKind.Category, category.id, category.name)) },
        )
    }
}

@Composable
private fun TagsEntry(
    model: CollectionsUiModel,
    onOpenTags: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onOpenTags,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(MdSpacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShapedIcon(
                    icon = Icons.AutoMirrored.Outlined.Label,
                    polygon = MaterialShapes.Slanted,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Column(
                    Modifier
                        .weight(1f)
                        .padding(horizontal = MdSpacing.sm),
                ) {
                    Text(stringResource(R.string.manage_tags), style = MaterialTheme.typography.titleMediumEmphasized)
                    Text(
                        text =
                            if (model.tags.isEmpty()) {
                                stringResource(R.string.manage_tags_summary)
                            } else {
                                pluralStringResource(R.plurals.tags_count, model.tags.size, model.tags.size)
                            },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
            }
            if (model.tags.isNotEmpty()) {
                Spacer(Modifier.height(MdSpacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    model.tags.take(3).forEach { tag ->
                        TagPill(name = tag.name, colorSeed = tag.colorSeed, tonal = true, modifier = Modifier.weight(1f, fill = false))
                    }
                    if (model.tags.size > 3) {
                        Text(
                            text = stringResource(R.string.tags_overflow, model.tags.size - 3),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
