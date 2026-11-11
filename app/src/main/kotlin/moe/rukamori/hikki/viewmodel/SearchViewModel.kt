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
import moe.rukamori.hikki.domain.model.NoteLifecycle
import moe.rukamori.hikki.domain.model.ScreenState
import moe.rukamori.hikki.domain.usecase.NoteUseCases
import moe.rukamori.hikki.domain.usecase.SettingsUseCases
import moe.rukamori.hikki.ui.model.SearchScope
import moe.rukamori.hikki.ui.model.SearchUiModel
import moe.rukamori.hikki.ui.model.toCardUiModel
import javax.inject.Inject

@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        private val noteUseCases: NoteUseCases,
        settingsUseCases: SettingsUseCases,
    ) : ViewModel() {
        private val _events = MutableSharedFlow<SearchEvent>()
        val events = _events.asSharedFlow()

        private val query = MutableStateFlow("")
        private val scope = MutableStateFlow<SearchScope>(SearchScope.Everywhere)

        val state: StateFlow<ScreenState<SearchUiModel>> =
            combine(query, settingsUseCases.settings) { currentQuery, settings -> currentQuery to settings }
                .flatMapLatest { (currentQuery, settings) ->
                    combine(
                        noteUseCases.searchNotes(currentQuery, NoteLifecycle.Active, settings.sort),
                        noteUseCases.observeFolders(),
                        noteUseCases.observeTags(),
                        noteUseCases.observeCategories(),
                        scope,
                    ) { notes, folders, tags, categories, requestedScope ->
                        val activeScope =
                            when (requestedScope) {
                                is SearchScope.InFolder ->
                                    if (folders.any { it.id == requestedScope.folderId }) requestedScope else SearchScope.Everywhere
                                is SearchScope.WithTag ->
                                    if (tags.any { it.id == requestedScope.tagId }) requestedScope else SearchScope.Everywhere
                                SearchScope.Everywhere -> requestedScope
                            }
                        val visible =
                            notes
                                .filter { note ->
                                    when (activeScope) {
                                        SearchScope.Everywhere -> true
                                        is SearchScope.InFolder -> note.folder?.id == activeScope.folderId
                                        is SearchScope.WithTag -> note.tags.any { it.id == activeScope.tagId }
                                    }
                                }
                                .map { it.toCardUiModel() }
                        if (currentQuery.isBlank() && notes.isEmpty() && activeScope == SearchScope.Everywhere) {
                            ScreenState.Empty
                        } else {
                            ScreenState.Success(
                                SearchUiModel(
                                    query = currentQuery,
                                    notes = visible,
                                    folders = folders,
                                    tags = tags,
                                    categories = categories,
                                    sort = settings.sort,
                                    scope = activeScope,
                                ),
                            )
                        }
                    }
                }
                .catch { emit(ScreenState.Error(R.string.error_search)) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)

        fun updateQuery(value: String) {
            query.value = value
        }

        fun setScope(value: SearchScope) {
            scope.value = value
        }

        fun togglePinned(
            noteId: Long,
            pinned: Boolean,
        ) {
            viewModelScope.launch {
                runCatching { noteUseCases.setPinned(noteId, pinned) }
                    .onFailure { _events.emit(SearchEvent.Message(R.string.error_note_update)) }
            }
        }

        fun toggleFavorite(
            noteId: Long,
            favorite: Boolean,
        ) {
            viewModelScope.launch {
                runCatching { noteUseCases.setFavorite(noteId, favorite) }
                    .onFailure { _events.emit(SearchEvent.Message(R.string.error_note_update)) }
            }
        }

        fun duplicate(noteId: Long) {
            viewModelScope.launch {
                runCatching { noteUseCases.duplicateNote(noteId) }
                    .onSuccess { newId ->
                        _events.emit(SearchEvent.Message(if (newId != null) R.string.note_duplicated else R.string.error_note_missing))
                    }
                    .onFailure { _events.emit(SearchEvent.Message(R.string.error_note_duplicate)) }
            }
        }

        fun archive(noteId: Long) {
            viewModelScope.launch {
                runCatching { noteUseCases.archive(noteId) }
                    .onSuccess { _events.emit(SearchEvent.Message(R.string.note_archived)) }
                    .onFailure { _events.emit(SearchEvent.Message(R.string.error_note_update)) }
            }
        }

        fun moveToTrash(noteId: Long) {
            viewModelScope.launch {
                runCatching { noteUseCases.moveToTrash(noteId) }
                    .onSuccess { _events.emit(SearchEvent.Message(R.string.note_moved_to_trash)) }
                    .onFailure { _events.emit(SearchEvent.Message(R.string.error_note_update)) }
            }
        }
    }

sealed interface SearchEvent {
    data class Message(@param:StringRes val messageRes: Int) : SearchEvent
}
