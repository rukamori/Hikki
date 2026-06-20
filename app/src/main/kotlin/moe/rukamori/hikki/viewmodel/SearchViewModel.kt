package moe.rukamori.hikki.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.NoteLifecycle
import moe.rukamori.hikki.domain.model.ScreenState
import moe.rukamori.hikki.domain.usecase.NoteUseCases
import moe.rukamori.hikki.domain.usecase.SettingsUseCases
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
        private val query = MutableStateFlow("")

        val state: StateFlow<ScreenState<SearchUiModel>> =
            combine(query, settingsUseCases.settings) { currentQuery, settings -> currentQuery to settings }
                .flatMapLatest { (currentQuery, settings) ->
                    combine(
                        noteUseCases.searchNotes(currentQuery, NoteLifecycle.Active, settings.sort),
                        noteUseCases.observeFolders(),
                        noteUseCases.observeTags(),
                        noteUseCases.observeCategories(),
                    ) { notes, folders, tags, categories ->
                        val model =
                            SearchUiModel(
                                query = currentQuery,
                                notes = notes.map { it.toCardUiModel() },
                                folders = folders,
                                tags = tags,
                                categories = categories,
                                sort = settings.sort,
                            )
                        if (currentQuery.isBlank() && notes.isEmpty()) ScreenState.Empty else ScreenState.Success(model)
                    }
                }
                .catch { emit(ScreenState.Error(R.string.error_search)) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)

        fun updateQuery(value: String) {
            query.value = value
        }

        fun togglePinned(
            noteId: Long,
            pinned: Boolean,
        ) {
            viewModelScope.launch { noteUseCases.setPinned(noteId, pinned) }
        }

        fun toggleFavorite(
            noteId: Long,
            favorite: Boolean,
        ) {
            viewModelScope.launch { noteUseCases.setFavorite(noteId, favorite) }
        }

        fun duplicate(noteId: Long) {
            viewModelScope.launch { noteUseCases.duplicateNote(noteId) }
        }

        fun archive(noteId: Long) {
            viewModelScope.launch { noteUseCases.archive(noteId) }
        }

        fun moveToTrash(noteId: Long) {
            viewModelScope.launch { noteUseCases.moveToTrash(noteId) }
        }
    }
