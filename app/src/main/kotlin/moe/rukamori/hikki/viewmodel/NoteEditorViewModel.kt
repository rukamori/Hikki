/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * GPL-3.0 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.hikki.viewmodel

import androidx.annotation.StringRes
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.placeCursorAtEnd
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.Category
import moe.rukamori.hikki.domain.model.EditorMode
import moe.rukamori.hikki.domain.model.Folder
import moe.rukamori.hikki.domain.model.Note
import moe.rukamori.hikki.domain.model.ScreenState
import moe.rukamori.hikki.domain.model.Tag
import moe.rukamori.hikki.domain.usecase.NoteUseCases
import moe.rukamori.hikki.domain.usecase.SettingsUseCases
import moe.rukamori.hikki.ui.model.NoteEditorUiModel
import moe.rukamori.hikki.ui.model.readingMinutes
import moe.rukamori.hikki.ui.model.wordCount
import javax.inject.Inject

@HiltViewModel
class NoteEditorViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val noteUseCases: NoteUseCases,
        private val settingsUseCases: SettingsUseCases,
    ) : ViewModel() {
        private val noteId: Long = checkNotNull(savedStateHandle["noteId"])
        private val editorDraft = MutableStateFlow(EditorDraft(noteId = noteId))
        private val history = ArrayDeque<TextSnapshot>()
        private val redoHistory = ArrayDeque<TextSnapshot>()
        private var initializedNoteId: Long? = null
        private var autosaveJob: Job? = null
        private var autosaveDelayMillis: Long = 800L
        private var persisted: Pair<String, String>? = null
        private var observedContent = TextSnapshot(text = "", selectionStart = 0, selectionEnd = 0)

        val titleState = TextFieldState()
        val contentState = TextFieldState()

        private val _events = MutableSharedFlow<EditorEvent>()
        val events = _events.asSharedFlow()

        val state: StateFlow<ScreenState<NoteEditorUiModel>> =
            combine(
                combine(
                    noteUseCases.observeNote(noteId),
                    noteUseCases.observeFolders(),
                    noteUseCases.observeCategories(),
                    noteUseCases.observeTags(),
                ) { note, folders, categories, tags ->
                    NoteEditorSources(
                        note = note,
                        folders = folders,
                        categories = categories,
                        tags = tags,
                    )
                },
                settingsUseCases.settings,
                editorDraft,
            ) { sources, settings, _ ->
                val note = sources.note
                if (note == null) {
                    return@combine ScreenState.Error(R.string.error_note_missing)
                }
                autosaveDelayMillis = settings.autosaveDelayMillis
                initializeDraftIfNeeded(note, settings.defaultEditorMode)
                val current = editorDraft.value
                ScreenState.Success(
                    NoteEditorUiModel(
                        id = note.id,
                        lifecycle = note.lifecycle,
                        title = current.title,
                        markdown = current.content,
                        folders = sources.folders,
                        categories = sources.categories,
                        tags = sources.tags,
                        selectedFolderId = current.folderId,
                        selectedCategoryId = current.categoryId,
                        selectedTagIds = current.tagIds,
                        editorMode = current.editorMode,
                        isPinned = current.isPinned,
                        isFavorite = current.isFavorite,
                        isSaving = current.isSaving,
                        lastSavedAt = current.lastSavedAt,
                        wordCount = current.content.wordCount(),
                        characterCount = current.content.length,
                        readingMinutes = current.content.readingMinutes(),
                        canUndo = history.isNotEmpty(),
                        canRedo = redoHistory.isNotEmpty(),
                    ),
                )
            }
                .catch { emit(ScreenState.Error(R.string.error_note_load)) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)

        init {
            viewModelScope.launch {
                snapshotFlow { titleState.text.toString() }.collect { title ->
                    if (title == editorDraft.value.title) return@collect
                    editorDraft.update { it.copy(title = title) }
                    scheduleAutosave()
                }
            }
            viewModelScope.launch {
                snapshotFlow { contentState.toSnapshot() }.collect { snapshot ->
                    val previous = observedContent
                    observedContent = snapshot
                    if (snapshot.text == editorDraft.value.content) return@collect
                    pushHistory(previous)
                    redoHistory.clear()
                    editorDraft.update { it.copy(content = snapshot.text) }
                    scheduleAutosave()
                }
            }
        }

        fun setMode(value: EditorMode) {
            editorDraft.update { it.copy(editorMode = value) }
            viewModelScope.launch { settingsUseCases.setDefaultEditorMode(value) }
        }

        fun togglePinned() {
            val next = !editorDraft.value.isPinned
            editorDraft.update { it.copy(isPinned = next) }
            viewModelScope.launch {
                runCatching { noteUseCases.setPinned(noteId, next) }
                    .onFailure { _events.emit(EditorEvent.Message(R.string.error_note_update)) }
            }
        }

        fun toggleFavorite() {
            val next = !editorDraft.value.isFavorite
            editorDraft.update { it.copy(isFavorite = next) }
            viewModelScope.launch {
                runCatching { noteUseCases.setFavorite(noteId, next) }
                    .onFailure { _events.emit(EditorEvent.Message(R.string.error_note_update)) }
            }
        }

        fun assignFolder(folderId: Long?) {
            editorDraft.update { it.copy(folderId = folderId) }
            viewModelScope.launch {
                runCatching { noteUseCases.assignFolder(noteId, folderId) }
                    .onFailure { _events.emit(EditorEvent.Message(R.string.error_note_update)) }
            }
        }

        fun assignCategory(categoryId: Long?) {
            editorDraft.update { it.copy(categoryId = categoryId) }
            viewModelScope.launch {
                runCatching { noteUseCases.assignCategory(noteId, categoryId) }
                    .onFailure { _events.emit(EditorEvent.Message(R.string.error_note_update)) }
            }
        }

        fun toggleTag(tagId: Long) {
            val next =
                editorDraft.value.tagIds.toMutableSet().apply {
                    if (!add(tagId)) remove(tagId)
                }
            editorDraft.update { it.copy(tagIds = next) }
            viewModelScope.launch {
                runCatching { noteUseCases.replaceTags(noteId, next) }
                    .onFailure { _events.emit(EditorEvent.Message(R.string.error_note_update)) }
            }
        }

        fun insertMarkdown(format: MarkdownFormat) {
            contentState.edit { format.applyTo(this) }
        }

        fun undo() {
            val previous = history.removeLastOrNull() ?: return
            redoHistory.addLast(contentState.toSnapshot())
            applySnapshot(previous)
        }

        fun redo() {
            val next = redoHistory.removeLastOrNull() ?: return
            history.addLast(contentState.toSnapshot())
            applySnapshot(next)
        }

        fun flushAutosave() {
            autosaveJob?.cancel()
            viewModelScope.launch {
                withContext(NonCancellable) { saveNow(showFeedback = false) }
            }
        }

        fun restore() {
            viewModelScope.launch {
                runCatching { noteUseCases.restore(noteId) }
                    .onSuccess { _events.emit(EditorEvent.Message(R.string.note_restored)) }
                    .onFailure { _events.emit(EditorEvent.Message(R.string.error_note_update)) }
            }
        }

        fun archiveAndClose() {
            viewModelScope.launch {
                saveNow(showFeedback = false)
                runCatching { noteUseCases.archive(noteId) }
                    .onSuccess { _events.emit(EditorEvent.Close(R.string.note_archived)) }
                    .onFailure { _events.emit(EditorEvent.Message(R.string.error_note_update)) }
            }
        }

        fun moveToTrashAndClose() {
            viewModelScope.launch {
                saveNow(showFeedback = false)
                runCatching { noteUseCases.moveToTrash(noteId) }
                    .onSuccess { _events.emit(EditorEvent.Close(R.string.note_moved_to_trash)) }
                    .onFailure { _events.emit(EditorEvent.Message(R.string.error_note_update)) }
            }
        }

        private fun initializeDraftIfNeeded(
            note: Note,
            defaultEditorMode: EditorMode,
        ) {
            if (initializedNoteId == note.id) return
            initializedNoteId = note.id
            persisted = note.title to note.content
            editorDraft.value =
                EditorDraft(
                    noteId = note.id,
                    title = note.title,
                    content = note.content,
                    folderId = note.folder?.id,
                    categoryId = note.category?.id,
                    tagIds = note.tags.map { it.id }.toSet(),
                    editorMode = defaultEditorMode,
                    isPinned = note.isPinned,
                    isFavorite = note.isFavorite,
                    lastSavedAt = note.updatedAt,
                )
            titleState.edit {
                replace(0, length, note.title)
                placeCursorAtEnd()
            }
            contentState.edit {
                replace(0, length, note.content)
                selection = TextRange(0)
            }
        }

        private fun scheduleAutosave() {
            autosaveJob?.cancel()
            editorDraft.update { it.copy(isSaving = true) }
            autosaveJob =
                viewModelScope.launch {
                    delay(autosaveDelayMillis)
                    saveNow(showFeedback = false)
                }
        }

        private suspend fun saveNow(showFeedback: Boolean) {
            val draft = editorDraft.value
            val snapshot = draft.title to draft.content
            if (snapshot == persisted) {
                editorDraft.update { it.copy(isSaving = false) }
                if (showFeedback) _events.emit(EditorEvent.Message(R.string.note_saved))
                return
            }
            runCatching {
                noteUseCases.saveNote(
                    noteId = draft.noteId,
                    title = draft.title,
                    content = draft.content,
                )
            }
                .onSuccess {
                    persisted = snapshot
                    editorDraft.update {
                        it.copy(
                            isSaving = false,
                            lastSavedAt = System.currentTimeMillis(),
                        )
                    }
                    if (showFeedback) _events.emit(EditorEvent.Message(R.string.note_saved))
                }
                .onFailure {
                    editorDraft.update { it.copy(isSaving = false) }
                    _events.emit(EditorEvent.Message(R.string.error_note_save))
                }
        }

        private fun pushHistory(snapshot: TextSnapshot) {
            history.addLast(snapshot)
            while (history.size > MaxHistorySize) {
                history.removeFirst()
            }
        }

        private fun applySnapshot(snapshot: TextSnapshot) {
            editorDraft.update { it.copy(content = snapshot.text) }
            contentState.edit {
                replace(0, length, snapshot.text)
                selection = TextRange(snapshot.selectionStart, snapshot.selectionEnd)
            }
            scheduleAutosave()
        }

        private fun TextFieldState.toSnapshot(): TextSnapshot =
            TextSnapshot(
                text = text.toString(),
                selectionStart = selection.start,
                selectionEnd = selection.end,
            )

        private companion object {
            const val MaxHistorySize = 80
        }
    }

sealed interface EditorEvent {
    data class Message(@param:StringRes val messageRes: Int) : EditorEvent
    data class Close(@param:StringRes val messageRes: Int) : EditorEvent
}

enum class MarkdownFormat {
    Heading,
    Bold,
    Italic,
    Strike,
    InlineCode,
    CodeBlock,
    Quote,
    BulletList,
    OrderedList,
    TaskList,
    Link,
    Image,
    Table,
    Rule,
}

private data class EditorDraft(
    val noteId: Long,
    val title: String = "",
    val content: String = "",
    val folderId: Long? = null,
    val categoryId: Long? = null,
    val tagIds: Set<Long> = emptySet(),
    val editorMode: EditorMode = EditorMode.Edit,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isSaving: Boolean = false,
    val lastSavedAt: Long? = null,
)

private data class TextSnapshot(
    val text: String,
    val selectionStart: Int,
    val selectionEnd: Int,
)

private data class NoteEditorSources(
    val note: Note?,
    val folders: List<Folder>,
    val categories: List<Category>,
    val tags: List<Tag>,
)

private fun MarkdownFormat.applyTo(buffer: TextFieldBuffer) {
    val start = buffer.selection.min
    val end = buffer.selection.max
    val selected = buffer.asCharSequence().substring(start, end)

    fun replace(replacement: String) {
        buffer.replace(start, end, replacement)
        buffer.selection = TextRange(start + replacement.length)
    }

    fun wrap(prefix: String, suffix: String = prefix) = replace(prefix + selected.ifBlank { "text" } + suffix)

    when (this) {
        MarkdownFormat.Heading -> replace("## ${selected.ifBlank { "Heading" }}")
        MarkdownFormat.Bold -> wrap("**")
        MarkdownFormat.Italic -> wrap("*")
        MarkdownFormat.Strike -> wrap("~~")
        MarkdownFormat.InlineCode -> wrap("`")
        MarkdownFormat.CodeBlock -> replace("```\n${selected.ifBlank { "code" }}\n```")
        MarkdownFormat.Quote -> replace(selected.lines().joinToString("\n") { "> ${it.ifBlank { "Quote" }}" })
        MarkdownFormat.BulletList -> replace(selected.lines().joinToString("\n") { "- ${it.ifBlank { "Item" }}" })
        MarkdownFormat.OrderedList -> replace(selected.lines().mapIndexed { index, line -> "${index + 1}. ${line.ifBlank { "Item" }}" }.joinToString("\n"))
        MarkdownFormat.TaskList -> replace(selected.lines().joinToString("\n") { "- [ ] ${it.ifBlank { "Task" }}" })
        MarkdownFormat.Link -> replace("[${selected.ifBlank { "Link" }}](https://)")
        MarkdownFormat.Image -> replace("![${selected.ifBlank { "Image" }}](https://)")
        MarkdownFormat.Table -> replace("| Column | Column |\n| --- | --- |\n| ${selected.ifBlank { "Value" }} | Value |")
        MarkdownFormat.Rule -> replace("\n---\n")
    }
}
