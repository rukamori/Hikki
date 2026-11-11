/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * GPL-3.0 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package moe.rukamori.hikki.ui.component

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.Tag
import moe.rukamori.hikki.ui.model.NoteCardUiModel
import moe.rukamori.hikki.ui.theme.MdSpacing
import moe.rukamori.hikki.ui.theme.tagAccentColor

enum class NoteTimeline(
    @param:StringRes val labelRes: Int,
) {
    Edited(R.string.edited_relative),
    Archived(R.string.archived_relative),
    Trashed(R.string.trashed_relative),
}

private val NoteTileShape = RoundedCornerShape(28.dp)
private const val MaxVisibleTags = 3

@Composable
fun NoteTile(
    note: NoteCardUiModel,
    onClick: () -> Unit,
    onShowActions: () -> Unit,
    modifier: Modifier = Modifier,
    featured: Boolean = false,
    timeline: NoteTimeline = NoteTimeline.Edited,
    previewMaxLines: Int = 4,
    highlight: String = "",
) {
    val motion = MaterialTheme.motionScheme
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = motion.fastSpatialSpec(),
        label = "note_tile_scale",
    )
    val containerColor by animateColorAsState(
        targetValue = if (featured) colors.secondaryContainer else colors.surfaceContainer,
        animationSpec = motion.defaultEffectsSpec(),
        label = "note_tile_container",
    )
    val contentColor = if (featured) colors.onSecondaryContainer else colors.onSurface
    val supportingColor = if (featured) colors.onSecondaryContainer.copy(alpha = 0.78f) else colors.onSurfaceVariant
    val accentColor = if (featured) colors.onSecondaryContainer else colors.primary
    val actionsLabel = stringResource(R.string.more_actions)

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(NoteTileShape)
                    .background(containerColor)
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        onClick = onClick,
                        onLongClick = onShowActions,
                        onLongClickLabel = actionsLabel,
                    )
                    .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MdSpacing.xxs + 2.dp),
                ) {
                    if (note.folderName != null) {
                        Icon(
                            imageVector = Icons.Outlined.Folder,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = note.folderName,
                            style = MaterialTheme.typography.labelLargeEmphasized,
                            color = accentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (note.isPinned) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = stringResource(R.string.pinned_state),
                        tint = accentColor,
                        modifier = Modifier.padding(horizontal = MdSpacing.xxs).size(18.dp),
                    )
                }
                if (note.isFavorite) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = stringResource(R.string.favorite_state),
                        tint = if (featured) colors.onSecondaryContainer else colors.tertiary,
                        modifier = Modifier.padding(horizontal = MdSpacing.xxs).size(18.dp),
                    )
                }
                IconButton(
                    onClick = onShowActions,
                    shapes = IconButtonDefaults.shapes(),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = actionsLabel,
                        tint = supportingColor,
                    )
                }
            }
            Column(
                modifier = Modifier.padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(MdSpacing.xs),
            ) {
                Text(
                    text = highlighted(note.title.ifBlank { stringResource(R.string.untitled_note) }, highlight),
                    style =
                        if (featured) {
                            MaterialTheme.typography.titleLargeEmphasized
                        } else {
                            MaterialTheme.typography.titleMediumEmphasized
                        },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (note.preview.isNotBlank()) {
                    Text(
                        text = highlighted(note.preview, highlight),
                        style = MaterialTheme.typography.bodyMedium,
                        color = supportingColor,
                        maxLines = previewMaxLines,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (note.tags.isNotEmpty()) {
                    TagRow(
                        tags = note.tags,
                        onSurface = featured,
                    )
                }
                Spacer(Modifier.height(MdSpacing.xxs))
                NoteFooter(
                    note = note,
                    timeline = timeline,
                    color = supportingColor,
                )
            }
        }
    }
}

@Composable
private fun NoteFooter(
    note: NoteCardUiModel,
    timeline: NoteTimeline,
    color: Color,
) {
    val timestamp =
        when (timeline) {
            NoteTimeline.Edited -> note.updatedAt
            NoteTimeline.Archived -> note.archivedAt ?: note.updatedAt
            NoteTimeline.Trashed -> note.trashedAt ?: note.updatedAt
        }
    val typography = MaterialTheme.typography
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MdSpacing.xs),
    ) {
        Text(
            text =
                stringResource(
                    R.string.meta_separator,
                    stringResource(timeline.labelRes, relativeTime(timestamp)),
                    pluralStringResource(R.plurals.words_count, note.wordCount, note.wordCount),
                ),
            style = typography.labelMedium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (note.categoryName != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MdSpacing.xxs),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Category,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = note.categoryName,
                    style = typography.labelMedium,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun TagRow(
    tags: List<Tag>,
    onSurface: Boolean,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(MdSpacing.xxs + 2.dp),
        verticalArrangement = Arrangement.spacedBy(MdSpacing.xxs + 2.dp),
    ) {
        tags.take(MaxVisibleTags).forEach { tag ->
            TagPill(name = tag.name, colorSeed = tag.colorSeed, tonal = onSurface)
        }
        val hidden = tags.size - MaxVisibleTags
        if (hidden > 0) {
            Box(
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = stringResource(R.string.tags_overflow, hidden),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun TagPill(
    name: String,
    colorSeed: Long,
    modifier: Modifier = Modifier,
    tonal: Boolean = false,
) {
    val background =
        if (tonal) MaterialTheme.colorScheme.surfaceContainerLowest else MaterialTheme.colorScheme.surfaceContainerHighest
    Row(
        modifier =
            modifier
                .clip(CircleShape)
                .background(background)
                .padding(start = 8.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(tagAccentColor(colorSeed)),
        )
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun highlighted(
    text: String,
    query: String,
): AnnotatedString {
    val terms =
        remember(query) {
            query.split(Regex("\\s+")).map { it.trim() }.filter { it.length >= 2 }
        }
    val highlightStyle =
        SpanStyle(
            background = MaterialTheme.colorScheme.tertiaryContainer,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            fontWeight = FontWeight.SemiBold,
        )
    return remember(text, terms, highlightStyle) {
        if (terms.isEmpty()) {
            AnnotatedString(text)
        } else {
            val ranges =
                terms
                    .flatMap { term ->
                        Regex(Regex.escape(term), RegexOption.IGNORE_CASE).findAll(text).map { it.range }.toList()
                    }.sortedBy { it.first }
            buildAnnotatedString {
                var cursor = 0
                ranges.forEach { range ->
                    if (range.first >= cursor) {
                        append(text.substring(cursor, range.first))
                        withStyle(highlightStyle) { append(text.substring(range.first, range.last + 1)) }
                        cursor = range.last + 1
                    }
                }
                if (cursor < text.length) append(text.substring(cursor))
            }
        }
    }
}

@Immutable
data class NoteAction(
    val icon: ImageVector,
    @param:StringRes val labelRes: Int,
    val destructive: Boolean = false,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteActionsSheet(
    note: NoteCardUiModel,
    actions: List<NoteAction>,
    onDismiss: () -> Unit,
    timeline: NoteTimeline = NoteTimeline.Edited,
) {
    val sheetState =
        rememberBottomSheetState(
            initialValue = SheetValue.Hidden,
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
        )
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MdSpacing.md, vertical = MdSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MdSpacing.sm),
        ) {
            ShapedIcon(
                icon = Icons.Outlined.Description,
                polygon = MaterialShapes.Cookie4Sided,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                size = 52.dp,
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = note.title.ifBlank { stringResource(R.string.untitled_note) },
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val timestamp =
                    when (timeline) {
                        NoteTimeline.Edited -> note.updatedAt
                        NoteTimeline.Archived -> note.archivedAt ?: note.updatedAt
                        NoteTimeline.Trashed -> note.trashedAt ?: note.updatedAt
                    }
                Text(
                    text = stringResource(timeline.labelRes, relativeTime(timestamp)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(MdSpacing.sm))
        Column(
            modifier = Modifier.padding(horizontal = MdSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        ) {
            actions.forEachIndexed { index, action ->
                val tint = if (action.destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                SegmentedListItem(
                    onClick = {
                        scope
                            .launch { sheetState.hide() }
                            .invokeOnCompletion {
                                onDismiss()
                                action.onClick()
                            }
                    },
                    shapes = ListItemDefaults.segmentedShapes(index = index, count = actions.size),
                    leadingContent = {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = null,
                            tint = tint,
                        )
                    },
                ) {
                    Text(
                        text = stringResource(action.labelRes),
                        color = if (action.destructive) MaterialTheme.colorScheme.error else Color.Unspecified,
                    )
                }
            }
        }
        Spacer(Modifier.height(MdSpacing.lg))
    }
}
