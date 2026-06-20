package moe.rukamori.hikki.viewmodel

import androidx.annotation.StringRes
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
        private var suppressHistory = false

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
                        title = current.title,
                        content = current.content,
                        markdown = current.content.text,
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
                        wordCount = current.content.text.wordCount(),
                        characterCount = current.content.text.length,
                        readingMinutes = current.content.text.readingMinutes(),
                        canUndo = history.isNotEmpty(),
                        canRedo = redoHistory.isNotEmpty(),
                    ),
                )
            }
                .catch { emit(ScreenState.Error(R.string.error_note_load)) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)

        fun updateTitle(value: String) {
            editorDraft.update { it.copy(title = value) }
            scheduleAutosave()
        }

        fun updateContent(value: TextFieldValue) {
            val old = editorDraft.value.content
            if (!suppressHistory && old != value) {
                pushHistory(old)
                redoHistory.clear()
            }
            editorDraft.update { it.copy(content = value) }
            scheduleAutosave()
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
            val current = editorDraft.value.content
            val replacement = format.apply(current)
            updateContent(replacement)
        }

        fun undo() {
            val previous = history.removeLastOrNull() ?: return
            redoHistory.addLast(editorDraft.value.content.toSnapshot())
            applySnapshot(previous)
        }

        fun redo() {
            val next = redoHistory.removeLastOrNull() ?: return
            history.addLast(editorDraft.value.content.toSnapshot())
            applySnapshot(next)
        }

        fun flushAutosave() {
            autosaveJob?.cancel()
            viewModelScope.launch { saveNow(showFeedback = false) }
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
            editorDraft.value =
                EditorDraft(
                    noteId = note.id,
                    title = note.title,
                    content = TextFieldValue(note.content),
                    folderId = note.folder?.id,
                    categoryId = note.category?.id,
                    tagIds = note.tags.map { it.id }.toSet(),
                    editorMode = defaultEditorMode,
                    isPinned = note.isPinned,
                    isFavorite = note.isFavorite,
                    lastSavedAt = note.updatedAt,
                )
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
            runCatching {
                noteUseCases.saveNote(
                    noteId = draft.noteId,
                    title = draft.title,
                    content = draft.content.text,
                )
            }
                .onSuccess {
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

        private fun pushHistory(value: TextFieldValue) {
            history.addLast(value.toSnapshot())
            while (history.size > MaxHistorySize) {
                history.removeFirst()
            }
        }

        private fun applySnapshot(snapshot: TextSnapshot) {
            suppressHistory = true
            editorDraft.update {
                it.copy(
                    content =
                        TextFieldValue(
                            text = snapshot.text,
                            selection = TextRange(snapshot.selectionStart, snapshot.selectionEnd),
                        ),
                )
            }
            suppressHistory = false
            scheduleAutosave()
        }

        private fun TextFieldValue.toSnapshot(): TextSnapshot =
            TextSnapshot(
                text = text,
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
    val content: TextFieldValue = TextFieldValue(),
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

private fun MarkdownFormat.apply(value: TextFieldValue): TextFieldValue {
    val selection = value.selection
    val start = minOf(selection.start, selection.end)
    val end = maxOf(selection.start, selection.end)
    val selected = value.text.substring(start, end)

    fun replace(replacement: String): TextFieldValue {
        val text = value.text.replaceRange(start, end, replacement)
        val cursor = start + replacement.length
        return TextFieldValue(text = text, selection = TextRange(cursor))
    }

    fun wrap(prefix: String, suffix: String = prefix): TextFieldValue =
        replace(prefix + selected.ifBlank { "text" } + suffix)

    return when (this) {
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
