/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * GPL-3.0 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.hikki.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.DisplayMode
import moe.rukamori.hikki.ui.model.NoteCardUiModel
import moe.rukamori.hikki.ui.theme.MdSpacing

private val ListMaxWidth = 760.dp
private val GridMinCellWidth = 168.dp

@Composable
fun NoteCollection(
    notes: List<NoteCardUiModel>,
    displayMode: DisplayMode,
    onOpen: (NoteCardUiModel) -> Unit,
    onShowActions: (NoteCardUiModel) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    contentPadding: PaddingValues = PaddingValues(),
    timeline: NoteTimeline = NoteTimeline.Edited,
    splitPinned: Boolean = false,
    highlight: String = "",
    header: (LazyStaggeredGridScope.() -> Unit)? = null,
) {
    val motion = MaterialTheme.motionScheme
    val sections =
        remember(notes, splitPinned) {
            val pinned = if (splitPinned) notes.filter { it.isPinned } else emptyList()
            val others = if (pinned.isEmpty()) notes else notes.filterNot { it.isPinned }
            pinned to others
        }
    val (pinned, others) = sections
    val grid = displayMode == DisplayMode.Grid
    val previewLines = if (grid) 8 else 3

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyVerticalStaggeredGrid(
            columns = if (grid) StaggeredGridCells.Adaptive(GridMinCellWidth) else StaggeredGridCells.Fixed(1),
            state = state,
            modifier =
                Modifier
                    .fillMaxSize()
                    .then(if (grid) Modifier else Modifier.widthIn(max = ListMaxWidth)),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(MdSpacing.sm - 4.dp),
            verticalItemSpacing = MdSpacing.sm - 4.dp,
        ) {
            header?.invoke(this)
            if (pinned.isNotEmpty()) {
                item(key = "section_pinned", contentType = "section", span = StaggeredGridItemSpan.FullLine) {
                    SectionHeader(
                        title = stringResource(R.string.section_pinned),
                        count = pinned.size,
                        modifier = Modifier.animateItem(),
                    )
                }
                items(
                    items = pinned,
                    key = { "note_${it.id}" },
                    contentType = { "note_featured" },
                ) { note ->
                    NoteTile(
                        note = note,
                        onClick = { onOpen(note) },
                        onShowActions = { onShowActions(note) },
                        featured = true,
                        timeline = timeline,
                        previewMaxLines = previewLines,
                        highlight = highlight,
                        modifier =
                            Modifier.animateItem(
                                fadeInSpec = motion.defaultEffectsSpec(),
                                placementSpec = motion.defaultSpatialSpec(),
                                fadeOutSpec = motion.fastEffectsSpec(),
                            ),
                    )
                }
                if (others.isNotEmpty()) {
                    item(key = "section_others", contentType = "section", span = StaggeredGridItemSpan.FullLine) {
                        SectionHeader(
                            title = stringResource(R.string.section_everything_else),
                            count = others.size,
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
            items(
                items = others,
                key = { "note_${it.id}" },
                contentType = { "note" },
            ) { note ->
                NoteTile(
                    note = note,
                    onClick = { onOpen(note) },
                    onShowActions = { onShowActions(note) },
                    timeline = timeline,
                    previewMaxLines = previewLines,
                    highlight = highlight,
                    modifier =
                        Modifier.animateItem(
                            fadeInSpec = motion.defaultEffectsSpec(),
                            placementSpec = motion.defaultSpatialSpec(),
                            fadeOutSpec = motion.fastEffectsSpec(),
                        ),
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    count: Int? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = MdSpacing.xxs, top = MdSpacing.xs, bottom = MdSpacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MdSpacing.xs),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmallEmphasized,
            color = MaterialTheme.colorScheme.primary,
        )
        if (count != null) {
            Box(
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}
