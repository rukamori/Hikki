/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * GPL-3.0 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.hikki.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Preview
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.ScreenState
import moe.rukamori.hikki.ui.component.EmptyState
import moe.rukamori.hikki.ui.component.MarkdownPreview
import moe.rukamori.hikki.ui.component.StateContent
import moe.rukamori.hikki.ui.theme.MdSpacing
import moe.rukamori.hikki.viewmodel.NoteEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotePreviewScreen(
    onBack: () -> Unit,
    viewModel: NoteEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val model = (state as? ScreenState.Success)?.data
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        text = model?.title?.ifBlank { null } ?: stringResource(R.string.untitled_note),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                subtitle = {
                    if (model != null) {
                        Text(
                            stringResource(
                                R.string.meta_separator,
                                pluralStringResource(R.plurals.words_count, model.wordCount, model.wordCount),
                                pluralStringResource(R.plurals.reading_minutes, model.readingMinutes, model.readingMinutes),
                            ),
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, shapes = IconButtonDefaults.shapes()) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
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
            if (current.markdown.isBlank()) {
                EmptyState(
                    titleRes = R.string.nothing_to_preview_title,
                    messageRes = R.string.nothing_to_preview_message,
                    icon = Icons.Outlined.Preview,
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    MarkdownPreview(
                        markdown = current.markdown,
                        modifier = Modifier.widthIn(max = 760.dp),
                        contentPadding = PaddingValues(horizontal = MdSpacing.md, vertical = MdSpacing.sm),
                    )
                }
            }
        }
    }
}
