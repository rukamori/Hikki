/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * GPL-3.0 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.hikki.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.DisplayMode
import moe.rukamori.hikki.domain.model.NoteLifecycle
import moe.rukamori.hikki.domain.model.NoteSort
import moe.rukamori.hikki.domain.model.ScreenState
import moe.rukamori.hikki.domain.usecase.NoteUseCases
import moe.rukamori.hikki.domain.usecase.SettingsUseCases
import moe.rukamori.hikki.ui.model.NoteFilter
import moe.rukamori.hikki.ui.model.NotesHomeUiModel
import moe.rukamori.hikki.ui.model.toCardUiModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val noteUseCases: NoteUseCases,
        private val settingsUseCases: SettingsUseCases,
    ) : ViewModel() {
        private val _events = MutableSharedFlow<HomeEvent>()
        val events = _events.asSharedFlow()

        private val filter = MutableStateFlow<NoteFilter>(NoteFilter.All)

        val state: StateFlow<ScreenState<NotesHomeUiModel>> =
            settingsUseCases.settings
                .flatMapLatest { settings ->
                    combine(
                        noteUseCases.observeNotes(NoteLifecycle.Active, settings.sort),
                        noteUseCases.observeFolders(),
                        noteUseCases.observeTags(),
                        noteUseCases.observeCategories(),
                        filter,
                    ) { notes, folders, tags, categories, requestedFilter ->
                        if (notes.isEmpty()) {
                            ScreenState.Empty
                        } else {
                            val activeFilter =
                                when (requestedFilter) {
                                    is NoteFilter.InFolder ->
                                        if (folders.any { it.id == requestedFilter.folderId }) requestedFilter else NoteFilter.All
                                    else -> requestedFilter
                                }
                            val cards = notes.map { it.toCardUiModel() }
                            val visible =
                                when (activeFilter) {
                                    NoteFilter.All -> cards
                                    NoteFilter.Favorites -> cards.filter { it.isFavorite }
                                    is NoteFilter.InFolder -> cards.filter { it.folderId == activeFilter.folderId }
                                }
                            ScreenState.Success(
                                NotesHomeUiModel(
                                    notes = visible,
                                    folders = folders,
                                    tags = tags,
                                    categories = categories,
                                    displayMode = settings.displayMode,
                                    sort = settings.sort,
                                    filter = activeFilter,
                                    totalCount = cards.size,
                                    favoriteCount = cards.count { it.isFavorite },
                                ),
                            )
                        }
                    }
                }
                .catch { emit(ScreenState.Error(R.string.error_notes_load)) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)

        fun setFilter(value: NoteFilter) {
            filter.value = value
        }

        fun createNote() {
            viewModelScope.launch {
                runCatching { noteUseCases.createNote() }
                    .onSuccess { noteId -> _events.emit(HomeEvent.OpenEditor(noteId)) }
                    .onFailure { _events.emit(HomeEvent.Message(R.string.error_note_create)) }
            }
        }

        fun duplicate(noteId: Long) {
            viewModelScope.launch {
                runCatching { noteUseCases.duplicateNote(noteId) }
                    .onSuccess { newId ->
                        if (newId != null) {
                            _events.emit(HomeEvent.OpenEditor(newId))
                        } else {
                            _events.emit(HomeEvent.Message(R.string.error_note_missing))
                        }
                    }
                    .onFailure { _events.emit(HomeEvent.Message(R.string.error_note_duplicate)) }
            }
        }

        fun togglePinned(
            noteId: Long,
            pinned: Boolean,
        ) {
            viewModelScope.launch {
                runCatching { noteUseCases.setPinned(noteId, pinned) }
                    .onFailure { _events.emit(HomeEvent.Message(R.string.error_note_update)) }
            }
        }

        fun toggleFavorite(
            noteId: Long,
            favorite: Boolean,
        ) {
            viewModelScope.launch {
                runCatching { noteUseCases.setFavorite(noteId, favorite) }
                    .onFailure { _events.emit(HomeEvent.Message(R.string.error_note_update)) }
            }
        }

        fun archive(noteId: Long) {
            viewModelScope.launch {
                runCatching { noteUseCases.archive(noteId) }
                    .onSuccess { _events.emit(HomeEvent.Message(R.string.note_archived)) }
                    .onFailure { _events.emit(HomeEvent.Message(R.string.error_note_update)) }
            }
        }

        fun moveToTrash(noteId: Long) {
            viewModelScope.launch {
                runCatching { noteUseCases.moveToTrash(noteId) }
                    .onSuccess { _events.emit(HomeEvent.Message(R.string.note_moved_to_trash)) }
                    .onFailure { _events.emit(HomeEvent.Message(R.string.error_note_update)) }
            }
        }

        fun setDisplayMode(displayMode: DisplayMode) {
            viewModelScope.launch { settingsUseCases.setDisplayMode(displayMode) }
        }

        fun setSort(sort: NoteSort) {
            viewModelScope.launch { settingsUseCases.setSort(sort) }
        }
    }

sealed interface HomeEvent {
    data class OpenEditor(val noteId: Long) : HomeEvent
    data class Message(@param:StringRes val messageRes: Int) : HomeEvent
}
