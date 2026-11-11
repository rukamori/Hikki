/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * GPL-3.0 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.hikki.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.InvalidBackupException
import moe.rukamori.hikki.domain.model.LibraryStats
import moe.rukamori.hikki.domain.model.RestoreMode
import moe.rukamori.hikki.domain.model.ScreenState
import moe.rukamori.hikki.domain.model.UnsupportedBackupException
import moe.rukamori.hikki.domain.usecase.BackupUseCases
import moe.rukamori.hikki.domain.usecase.NoteUseCases
import moe.rukamori.hikki.ui.model.DataMessage
import moe.rukamori.hikki.ui.model.DataOperation
import moe.rukamori.hikki.ui.model.DataOperationUiState
import moe.rukamori.hikki.ui.model.PendingRestore
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DataManagementViewModel
    @Inject
    constructor(
        private val noteUseCases: NoteUseCases,
        private val backupUseCases: BackupUseCases,
    ) : ViewModel() {
        val stats: StateFlow<ScreenState<LibraryStats>> =
            noteUseCases.observeLibraryStats()
                .map<LibraryStats, ScreenState<LibraryStats>> { ScreenState.Success(it) }
                .catch { emit(ScreenState.Error(R.string.error_library_stats)) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)

        private val operationState = MutableStateFlow(DataOperationUiState())
        val operation: StateFlow<DataOperationUiState> = operationState.asStateFlow()

        private val messageChannel = Channel<DataMessage>(Channel.BUFFERED)
        val messages: Flow<DataMessage> = messageChannel.receiveAsFlow()

        fun backupFileName(): String = "hikki-backup-${LocalDate.now()}.json"

        fun markdownArchiveName(): String = "hikki-notes-${LocalDate.now()}.zip"

        fun backup(destination: Uri) {
            launchOperation(DataOperation.Backup) {
                val summary = backupUseCases.exportBackup(destination)
                messageChannel.send(DataMessage.Count(R.plurals.backup_created, summary.notes))
            }
        }

        fun inspectBackup(source: Uri) {
            launchOperation(DataOperation.Inspect) {
                val summary = backupUseCases.inspectBackup(source)
                operationState.update { it.copy(pendingRestore = PendingRestore(source.toString(), summary)) }
            }
        }

        fun dismissRestore() {
            operationState.update { it.copy(pendingRestore = null) }
        }

        fun restore(mode: RestoreMode) {
            val pending = operationState.value.pendingRestore ?: return
            if (operationState.value.running != null) return
            operationState.update { it.copy(pendingRestore = null) }
            launchOperation(DataOperation.Restore) {
                val summary = backupUseCases.restoreBackup(Uri.parse(pending.uri), mode)
                messageChannel.send(DataMessage.Count(R.plurals.backup_restored, summary.notes))
            }
        }

        fun exportMarkdown(destination: Uri) {
            launchOperation(DataOperation.ExportMarkdown) {
                val count = backupUseCases.exportMarkdown(destination)
                messageChannel.send(DataMessage.Count(R.plurals.markdown_exported, count))
            }
        }

        fun emptyTrash() {
            launchOperation(DataOperation.EmptyTrash) {
                val count = noteUseCases.emptyTrash()
                messageChannel.send(DataMessage.Count(R.plurals.trash_emptied, count))
            }
        }

        private fun launchOperation(
            operation: DataOperation,
            block: suspend () -> Unit,
        ) {
            if (operationState.value.running != null) return
            operationState.update { it.copy(running = operation) }
            viewModelScope.launch {
                try {
                    block()
                } catch (error: CancellationException) {
                    throw error
                } catch (error: UnsupportedBackupException) {
                    messageChannel.send(DataMessage.Text(R.string.error_backup_unsupported))
                } catch (error: InvalidBackupException) {
                    messageChannel.send(DataMessage.Text(R.string.error_backup_invalid))
                } catch (error: Exception) {
                    messageChannel.send(DataMessage.Text(operation.errorRes))
                } finally {
                    operationState.update { it.copy(running = null) }
                }
            }
        }
    }
