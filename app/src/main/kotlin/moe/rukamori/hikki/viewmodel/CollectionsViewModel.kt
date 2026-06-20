package moe.rukamori.hikki.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.ScreenState
import moe.rukamori.hikki.domain.usecase.NoteUseCases
import moe.rukamori.hikki.ui.model.CollectionsUiModel
import javax.inject.Inject

@HiltViewModel
class CollectionsViewModel
    @Inject
    constructor(
        private val noteUseCases: NoteUseCases,
    ) : ViewModel() {
        private val _events = MutableSharedFlow<CollectionsEvent>()
        val events = _events.asSharedFlow()

        val state: StateFlow<ScreenState<CollectionsUiModel>> =
            combine(
                noteUseCases.observeFolders(),
                noteUseCases.observeCategories(),
                noteUseCases.observeTags(),
            ) { folders, categories, tags ->
                if (folders.isEmpty() && categories.isEmpty() && tags.isEmpty()) {
                    ScreenState.Empty
                } else {
                    ScreenState.Success(CollectionsUiModel(folders, categories, tags))
                }
            }
                .catch { emit(ScreenState.Error(R.string.error_collections_load)) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)

        fun createFolder(name: String) {
            runCollectionAction(R.string.folder_created) { noteUseCases.createFolder(name) }
        }

        fun deleteFolder(folderId: Long) {
            runCollectionAction(R.string.folder_deleted) { noteUseCases.deleteFolder(folderId) }
        }

        fun createCategory(name: String) {
            runCollectionAction(R.string.category_created) { noteUseCases.createCategory(name) }
        }

        fun deleteCategory(categoryId: Long) {
            runCollectionAction(R.string.category_deleted) { noteUseCases.deleteCategory(categoryId) }
        }

        fun createTag(name: String) {
            val seed = name.trim().fold(0xFF5D6B2FL) { acc, char -> acc + char.code * 31L }
            runCollectionAction(R.string.tag_created) { noteUseCases.createTag(name, seed) }
        }

        fun deleteTag(tagId: Long) {
            runCollectionAction(R.string.tag_deleted) { noteUseCases.deleteTag(tagId) }
        }

        private fun runCollectionAction(
            @StringRes successMessage: Int,
            block: suspend () -> Unit,
        ) {
            viewModelScope.launch {
                runCatching { block() }
                    .onSuccess { _events.emit(CollectionsEvent.Message(successMessage)) }
                    .onFailure { _events.emit(CollectionsEvent.Message(R.string.error_collection_update)) }
            }
        }
    }

sealed interface CollectionsEvent {
    data class Message(@StringRes val messageRes: Int) : CollectionsEvent
}
