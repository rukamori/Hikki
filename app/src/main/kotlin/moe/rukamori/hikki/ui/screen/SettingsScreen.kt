/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * GPL-3.0 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.hikki.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.AutoDelete
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialShapes
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moe.rukamori.hikki.BuildConfig
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.LibraryStats
import moe.rukamori.hikki.domain.model.ScreenState
import moe.rukamori.hikki.domain.model.TrashRetention
import moe.rukamori.hikki.ui.component.ConfirmDestructiveDialog
import moe.rukamori.hikki.ui.component.ConnectedChoiceGroup
import moe.rukamori.hikki.ui.component.RestoreBackupDialog
import moe.rukamori.hikki.ui.component.SettingsActionRow
import moe.rukamori.hikki.ui.component.SettingsControlRow
import moe.rukamori.hikki.ui.component.SettingsGroup
import moe.rukamori.hikki.ui.component.SettingsIcon
import moe.rukamori.hikki.ui.component.SettingsInfoRow
import moe.rukamori.hikki.ui.component.SettingsNavigationRow
import moe.rukamori.hikki.ui.component.StateContent
import moe.rukamori.hikki.ui.component.relativeTime
import moe.rukamori.hikki.ui.component.showMessage
import moe.rukamori.hikki.ui.model.DataMessage
import moe.rukamori.hikki.ui.model.DataOperation
import moe.rukamori.hikki.ui.theme.MdSpacing
import moe.rukamori.hikki.viewmodel.DataManagementViewModel
import moe.rukamori.hikki.viewmodel.SettingsViewModel

private const val BackupMimeType = "application/json"
private const val MarkdownArchiveMimeType = "application/zip"
private val RestoreMimeTypes = arrayOf("application/json", "text/plain", "application/octet-stream")

@Composable
fun SettingsScreen(
    onOpenAppearance: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenTrash: () -> Unit,
    onOpenArchive: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    dataViewModel: DataManagementViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val statsState by dataViewModel.stats.collectAsStateWithLifecycle()
    val operation by dataViewModel.operation.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var confirmEmptyTrash by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(dataViewModel) {
        dataViewModel.messages.collect { message ->
            val text =
                when (message) {
                    is DataMessage.Text -> context.getString(message.messageRes)
                    is DataMessage.Count -> context.resources.getQuantityString(message.messageRes, message.count, message.count)
                }
            scope.showMessage(snackbarHostState, text)
        }
    }

    val backupLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(BackupMimeType)) { uri ->
            if (uri != null) dataViewModel.backup(uri)
        }
    val restoreLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) dataViewModel.inspectBackup(uri)
        }
    val markdownLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(MarkdownArchiveMimeType)) { uri ->
            if (uri != null) dataViewModel.exportMarkdown(uri)
        }

    val stats = (statsState as? ScreenState.Success)?.data
    val running = operation.running
    val idle = running == null

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                subtitle = { Text(stringResource(R.string.version_value, BuildConfig.VERSION_NAME)) },
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
            emptyTitleRes = R.string.settings,
            emptyMessageRes = R.string.settings_empty_message,
        ) { model ->
            val settings = model.settings
            val appearanceSummary =
                stringResource(
                    R.string.meta_separator,
                    stringResource(settings.themeMode.labelRes),
                    stringResource(settings.displayMode.labelRes),
                )
            val backupSummary =
                settings.lastBackupAt?.let { stringResource(R.string.last_backup, relativeTime(it)) }
                    ?: stringResource(R.string.backup_never)
            val trashedCount = stats?.trashedNotes ?: 0
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .widthIn(max = 760.dp),
                    contentPadding = PaddingValues(top = MdSpacing.xs, bottom = MdSpacing.xl),
                    verticalArrangement = Arrangement.spacedBy(MdSpacing.md),
                ) {
                    item(key = "library", contentType = "group") {
                        SettingsGroup(title = stringResource(R.string.library)) {
                            row { shapes ->
                                SettingsInfoRow(
                                    shapes = shapes,
                                    title = libraryTitle(statsState),
                                    summary = librarySummary(statsState),
                                    leading = { SettingsIcon(icon = Icons.Outlined.Inventory2, polygon = MaterialShapes.Pill) },
                                )
                            }
                        }
                    }
                    item(key = "personalize", contentType = "group") {
                        SettingsGroup(title = stringResource(R.string.appearance)) {
                            row { shapes ->
                                SettingsNavigationRow(
                                    shapes = shapes,
                                    icon = Icons.Outlined.Palette,
                                    title = stringResource(R.string.appearance),
                                    summary = appearanceSummary,
                                    onClick = onOpenAppearance,
                                    polygon = MaterialShapes.Flower,
                                )
                            }
                        }
                    }
                    item(key = "backup", contentType = "group") {
                        SettingsGroup(title = stringResource(R.string.backup_and_restore)) {
                            row { shapes ->
                                SettingsActionRow(
                                    shapes = shapes,
                                    icon = Icons.Outlined.Backup,
                                    title = stringResource(R.string.backup_now),
                                    summary = backupSummary,
                                    onClick = { backupLauncher.launch(dataViewModel.backupFileName()) },
                                    enabled = idle,
                                    busy = running == DataOperation.Backup,
                                    polygon = MaterialShapes.Cookie4Sided,
                                )
                            }
                            row { shapes ->
                                SettingsActionRow(
                                    shapes = shapes,
                                    icon = Icons.Outlined.Restore,
                                    title = stringResource(R.string.restore_backup),
                                    summary = stringResource(R.string.restore_backup_summary),
                                    onClick = { restoreLauncher.launch(RestoreMimeTypes) },
                                    enabled = idle,
                                    busy = running == DataOperation.Inspect || running == DataOperation.Restore,
                                    polygon = MaterialShapes.Clover8Leaf,
                                )
                            }
                            row { shapes ->
                                SettingsActionRow(
                                    shapes = shapes,
                                    icon = Icons.Outlined.FolderZip,
                                    title = stringResource(R.string.export_markdown),
                                    summary = stringResource(R.string.export_markdown_summary),
                                    onClick = { markdownLauncher.launch(dataViewModel.markdownArchiveName()) },
                                    enabled = idle,
                                    busy = running == DataOperation.ExportMarkdown,
                                    polygon = MaterialShapes.Gem,
                                )
                            }
                        }
                    }
                    item(key = "workspace", contentType = "group") {
                        SettingsGroup(title = stringResource(R.string.workspace)) {
                            row { shapes ->
                                SettingsNavigationRow(
                                    shapes = shapes,
                                    icon = Icons.Outlined.Archive,
                                    title = stringResource(R.string.archive),
                                    summary = stringResource(R.string.archive_summary),
                                    onClick = onOpenArchive,
                                    polygon = MaterialShapes.Clover4Leaf,
                                )
                            }
                            row { shapes ->
                                SettingsNavigationRow(
                                    shapes = shapes,
                                    icon = Icons.Outlined.Delete,
                                    title = stringResource(R.string.trash),
                                    summary = stringResource(R.string.trash_summary),
                                    onClick = onOpenTrash,
                                    polygon = MaterialShapes.Cookie6Sided,
                                )
                            }
                            row { shapes ->
                                SettingsControlRow(
                                    shapes = shapes,
                                    title = stringResource(R.string.auto_empty_trash),
                                    summary = stringResource(R.string.auto_empty_trash_summary),
                                    icon = Icons.Outlined.AutoDelete,
                                    polygon = MaterialShapes.Arch,
                                ) {
                                    ConnectedChoiceGroup(
                                        options = TrashRetention.entries,
                                        selected = settings.trashRetention,
                                        onSelect = viewModel::setTrashRetention,
                                        label = { stringResource(it.labelRes) },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                            row { shapes ->
                                SettingsActionRow(
                                    shapes = shapes,
                                    icon = Icons.Outlined.DeleteSweep,
                                    title = stringResource(R.string.empty_trash),
                                    summary =
                                        if (trashedCount > 0) {
                                            pluralStringResource(R.plurals.trash_contains, trashedCount, trashedCount)
                                        } else {
                                            stringResource(R.string.trash_empty_title)
                                        },
                                    onClick = { confirmEmptyTrash = true },
                                    enabled = idle && trashedCount > 0,
                                    busy = running == DataOperation.EmptyTrash,
                                    destructive = true,
                                    polygon = MaterialShapes.Burst,
                                )
                            }
                        }
                    }
                    item(key = "about", contentType = "group") {
                        SettingsGroup(title = stringResource(R.string.about)) {
                            row { shapes ->
                                SettingsNavigationRow(
                                    shapes = shapes,
                                    icon = Icons.Outlined.Info,
                                    title = stringResource(R.string.about),
                                    summary = stringResource(R.string.about_summary),
                                    onClick = onOpenAbout,
                                    polygon = MaterialShapes.Sunny,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    operation.pendingRestore?.let { pending ->
        RestoreBackupDialog(
            summary = pending.summary,
            onRestore = dataViewModel::restore,
            onDismiss = dataViewModel::dismissRestore,
        )
    }

    if (confirmEmptyTrash) {
        val count = stats?.trashedNotes ?: 0
        ConfirmDestructiveDialog(
            icon = Icons.Outlined.DeleteSweep,
            title = stringResource(R.string.empty_trash_title),
            message = pluralStringResource(R.plurals.empty_trash_message, count, count),
            confirmRes = R.string.empty_trash,
            onConfirm = dataViewModel::emptyTrash,
            onDismiss = { confirmEmptyTrash = false },
        )
    }
}

@Composable
private fun libraryTitle(state: ScreenState<LibraryStats>): String =
    when (state) {
        is ScreenState.Success -> pluralStringResource(R.plurals.notes_count, state.data.activeNotes, state.data.activeNotes)
        is ScreenState.Error -> stringResource(R.string.library)
        else -> stringResource(R.string.library_loading)
    }

@Composable
private fun librarySummary(state: ScreenState<LibraryStats>): String? =
    when (state) {
        is ScreenState.Success -> {
            val stats = state.data
            listOf(
                pluralStringResource(R.plurals.archived_count, stats.archivedNotes, stats.archivedNotes),
                pluralStringResource(R.plurals.trash_contains, stats.trashedNotes, stats.trashedNotes),
                pluralStringResource(R.plurals.folders_count, stats.folders, stats.folders),
                pluralStringResource(R.plurals.categories_count, stats.categories, stats.categories),
                pluralStringResource(R.plurals.tags_count, stats.tags, stats.tags),
            ).joinToString(separator = stringResource(R.string.list_separator))
        }
        is ScreenState.Error -> stringResource(state.messageRes)
        else -> null
    }
