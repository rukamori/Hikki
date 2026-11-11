/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * GPL-3.0 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.hikki.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MediumExtendedFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.ScreenState
import moe.rukamori.hikki.domain.model.Tag
import moe.rukamori.hikki.ui.component.EmptyState
import moe.rukamori.hikki.ui.component.StateContent
import moe.rukamori.hikki.ui.component.showMessage
import moe.rukamori.hikki.ui.theme.MdSpacing
import moe.rukamori.hikki.ui.theme.tagAccentColor
import moe.rukamori.hikki.viewmodel.CollectionsEvent
import moe.rukamori.hikki.viewmodel.CollectionsViewModel

@Composable
fun TagsScreen(
    onBack: () -> Unit,
    viewModel: CollectionsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var request by remember { mutableStateOf<CollectionRequest?>(null) }
    val tags = (state as? ScreenState.Success)?.data?.tags

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CollectionsEvent.Message -> scope.showMessage(snackbarHostState, context.getString(event.messageRes))
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.tags)) },
                subtitle = {
                    if (tags != null) Text(pluralStringResource(R.plurals.tags_count, tags.size, tags.size))
                },
                navigationIcon = {
                    IconButton(onClick = onBack, shapes = IconButtonDefaults.shapes()) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            if (state !is ScreenState.Loading) {
                MediumExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.create_tag)) },
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    onClick = { request = CollectionRequest.Create(CollectionKind.Tag) },
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
            emptyTitleRes = R.string.tags_empty_title,
            emptyMessageRes = R.string.tags_empty_message,
            emptyIcon = Icons.AutoMirrored.Outlined.Label,
            emptyActionRes = R.string.create_tag,
            onEmptyAction = { request = CollectionRequest.Create(CollectionKind.Tag) },
        ) { model ->
            if (model.tags.isEmpty()) {
                EmptyState(
                    titleRes = R.string.tags_empty_title,
                    messageRes = R.string.tags_empty_message,
                    icon = Icons.AutoMirrored.Outlined.Label,
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .widthIn(max = 760.dp),
                        contentPadding = PaddingValues(start = MdSpacing.sm, end = MdSpacing.sm, top = MdSpacing.xs, bottom = 128.dp),
                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                    ) {
                        itemsIndexed(model.tags, key = { _, tag -> tag.id }, contentType = { _, _ -> "tag" }) { index, tag ->
                            TagRow(
                                tag = tag,
                                noteCount = model.tagNoteCounts[tag.id] ?: 0,
                                index = index,
                                count = model.tags.size,
                                onRequest = { request = it },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
            }
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
private fun TagRow(
    tag: Tag,
    noteCount: Int,
    index: Int,
    count: Int,
    onRequest: (CollectionRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val accent = tagAccentColor(tag.colorSeed)
    val badgeShape = MaterialShapes.Cookie6Sided.toShape()
    Box(modifier) {
        SegmentedListItem(
            onClick = { menuOpen = true },
            shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
            leadingContent = {
                Box(
                    modifier =
                        Modifier
                            .size(40.dp)
                            .clip(badgeShape)
                            .background(accent.copy(alpha = 0.24f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(accent),
                    )
                }
            },
            supportingContent = { Text(pluralStringResource(R.plurals.notes_count, noteCount, noteCount)) },
            trailingContent = { Icon(Icons.Outlined.MoreVert, contentDescription = null) },
        ) {
            Text(tag.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        CollectionItemMenu(
            expanded = menuOpen,
            onDismiss = { menuOpen = false },
            onRename = { onRequest(CollectionRequest.Rename(CollectionKind.Tag, tag.id, tag.name)) },
            onDelete = { onRequest(CollectionRequest.Delete(CollectionKind.Tag, tag.id, tag.name)) },
        )
    }
}
