package moe.rukamori.hikki.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.NoteLifecycle
import moe.rukamori.hikki.domain.model.ScreenState
import moe.rukamori.hikki.domain.usecase.NoteUseCases
import moe.rukamori.hikki.domain.usecase.SettingsUseCases
import moe.rukamori.hikki.ui.model.NotesHomeUiModel
import moe.rukamori.hikki.ui.model.toCardUiModel

abstract class LifecycleNotesViewModel(
    private val lifecycle: NoteLifecycle,
    private val noteUseCases: NoteUseCases,
    settingsUseCases: SettingsUseCases,
) : ViewModel() {
    private val _events = MutableSharedFlow<LifecycleNotesEvent>()
    val events = _events.asSharedFlow()

    val state: StateFlow<ScreenState<NotesHomeUiModel>> =
        settingsUseCases.settings
            .flatMapLatest { settings ->
                combine(
                    noteUseCases.observeNotes(lifecycle, settings.sort),
                    noteUseCases.observeFolders(),
                    noteUseCases.observeTags(),
                    noteUseCases.observeCategories(),
                ) { notes, folders, tags, categories ->
                    if (notes.isEmpty()) {
                        ScreenState.Empty
                    } else {
                        ScreenState.Success(
                            NotesHomeUiModel(
                                notes = notes.map { it.toCardUiModel() },
                                folders = folders,
                                tags = tags,
                                categories = categories,
                                displayMode = settings.displayMode,
                                sort = settings.sort,
                            ),
                        )
                    }
                }
            }
            .catch { emit(ScreenState.Error(R.string.error_notes_load)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)

    fun restore(noteId: Long) {
        viewModelScope.launch {
            runCatching { noteUseCases.restore(noteId) }
                .onSuccess { _events.emit(LifecycleNotesEvent.Message(R.string.note_restored)) }
                .onFailure { _events.emit(LifecycleNotesEvent.Message(R.string.error_note_update)) }
        }
    }

    fun moveToTrash(noteId: Long) {
        viewModelScope.launch {
            runCatching { noteUseCases.moveToTrash(noteId) }
                .onSuccess { _events.emit(LifecycleNotesEvent.Message(R.string.note_moved_to_trash)) }
                .onFailure { _events.emit(LifecycleNotesEvent.Message(R.string.error_note_update)) }
        }
    }

    fun permanentlyDelete(noteId: Long) {
        viewModelScope.launch {
            runCatching { noteUseCases.permanentlyDelete(noteId) }
                .onSuccess { _events.emit(LifecycleNotesEvent.Message(R.string.note_deleted)) }
                .onFailure { _events.emit(LifecycleNotesEvent.Message(R.string.error_note_delete)) }
        }
    }
}

sealed interface LifecycleNotesEvent {
    data class Message(@StringRes val messageRes: Int) : LifecycleNotesEvent
}
