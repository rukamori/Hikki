package moe.rukamori.hikki.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.ScreenState
import moe.rukamori.hikki.ui.component.NoteAction
import moe.rukamori.hikki.ui.component.NoteActionsSheet
import moe.rukamori.hikki.ui.component.NoteCollection
import moe.rukamori.hikki.ui.component.NoteTimeline
import moe.rukamori.hikki.ui.component.ResultMessageEffect
import moe.rukamori.hikki.ui.component.ShapedIcon
import moe.rukamori.hikki.ui.component.StateContent
import moe.rukamori.hikki.ui.component.showMessage
import moe.rukamori.hikki.ui.model.NoteCardUiModel
import moe.rukamori.hikki.ui.model.NotesHomeUiModel
import moe.rukamori.hikki.ui.theme.MdSpacing
import moe.rukamori.hikki.viewmodel.LifecycleNotesEvent

@Composable
internal fun LifecycleNotesLayout(
    @StringRes titleRes: Int,
    state: ScreenState<NotesHomeUiModel>,
    events: Flow<LifecycleNotesEvent>,
    timeline: NoteTimeline,
    @StringRes emptyTitleRes: Int,
    @StringRes emptyMessageRes: Int,
    emptyIcon: ImageVector,
    @StringRes infoRes: Int,
    infoIcon: ImageVector,
    noteActions: (NoteCardUiModel) -> List<NoteAction>,
    onOpen: ((NoteCardUiModel) -> Unit)?,
    resultMessageRes: Int? = null,
    onResultMessageShown: () -> Unit = {},
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val model = (state as? ScreenState.Success)?.data
    var actionNoteId by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                is LifecycleNotesEvent.Message -> scope.showMessage(snackbarHostState, context.getString(event.messageRes))
            }
        }
    }
    ResultMessageEffect(snackbarHostState, resultMessageRes, onResultMessageShown)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(titleRes)) },
                subtitle = {
                    if (model != null) {
                        Text(pluralStringResource(R.plurals.notes_count, model.totalCount, model.totalCount))
                    }
                },
                navigationIcon = navigationIcon,
                actions = actions,
                scrollBehavior = scrollBehavior,
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
            emptyTitleRes = emptyTitleRes,
            emptyMessageRes = emptyMessageRes,
            emptyIcon = emptyIcon,
        ) { current ->
            val actionNote = actionNoteId?.let { id -> current.notes.firstOrNull { it.id == id } }
            NoteCollection(
                notes = current.notes,
                displayMode = current.displayMode,
                onOpen = { note -> if (onOpen != null) onOpen(note) else actionNoteId = note.id },
                onShowActions = { actionNoteId = it.id },
                contentPadding = PaddingValues(start = MdSpacing.sm, end = MdSpacing.sm, bottom = MdSpacing.xl),
                timeline = timeline,
                header = {
                    item(key = "info", contentType = "info", span = StaggeredGridItemSpan.FullLine) {
                        InfoBanner(textRes = infoRes, icon = infoIcon)
                    }
                },
            )
            if (actionNoteId != null && actionNote == null) {
                LaunchedEffect(actionNoteId) { actionNoteId = null }
            }
            if (actionNote != null) {
                NoteActionsSheet(
                    note = actionNote,
                    actions = noteActions(actionNote),
                    onDismiss = { actionNoteId = null },
                    timeline = timeline,
                )
            }
        }
    }
}

@Composable
private fun InfoBanner(
    @StringRes textRes: Int,
    icon: ImageVector,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = MdSpacing.xs, bottom = MdSpacing.xxs)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(MdSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MdSpacing.sm),
    ) {
        ShapedIcon(
            icon = icon,
            polygon = MaterialShapes.Sunny,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            size = 40.dp,
            iconSize = 20.dp,
        )
        Text(
            text = stringResource(textRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
    }
}
