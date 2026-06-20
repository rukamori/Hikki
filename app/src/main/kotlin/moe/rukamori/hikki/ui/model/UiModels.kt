package moe.rukamori.hikki.ui.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.TextFieldValue
import moe.rukamori.hikki.domain.model.AppSettings
import moe.rukamori.hikki.domain.model.Category
import moe.rukamori.hikki.domain.model.DisplayMode
import moe.rukamori.hikki.domain.model.EditorMode
import moe.rukamori.hikki.domain.model.Folder
import moe.rukamori.hikki.domain.model.Note
import moe.rukamori.hikki.domain.model.NoteSort
import moe.rukamori.hikki.domain.model.Tag

@Immutable
data class NoteCardUiModel(
    val id: Long,
    val title: String,
    val preview: String,
    val folderName: String?,
    val categoryName: String?,
    val tags: List<Tag>,
    val isPinned: Boolean,
    val isFavorite: Boolean,
    val updatedAt: Long,
    val wordCount: Int,
)

@Immutable
data class NotesHomeUiModel(
    val notes: List<NoteCardUiModel>,
    val folders: List<Folder>,
    val tags: List<Tag>,
    val categories: List<Category>,
    val displayMode: DisplayMode,
    val sort: NoteSort,
)

@Immutable
data class SearchUiModel(
    val query: String,
    val notes: List<NoteCardUiModel>,
    val folders: List<Folder>,
    val tags: List<Tag>,
    val categories: List<Category>,
    val sort: NoteSort,
)

@Immutable
data class CollectionsUiModel(
    val folders: List<Folder>,
    val categories: List<Category>,
    val tags: List<Tag>,
)

@Immutable
data class NoteEditorUiModel(
    val id: Long,
    val title: String,
    val content: TextFieldValue,
    val markdown: String,
    val folders: List<Folder>,
    val categories: List<Category>,
    val tags: List<Tag>,
    val selectedFolderId: Long?,
    val selectedCategoryId: Long?,
    val selectedTagIds: Set<Long>,
    val editorMode: EditorMode,
    val isPinned: Boolean,
    val isFavorite: Boolean,
    val isSaving: Boolean,
    val lastSavedAt: Long?,
    val wordCount: Int,
    val characterCount: Int,
    val readingMinutes: Int,
    val canUndo: Boolean,
    val canRedo: Boolean,
)

@Immutable
data class SettingsUiModel(
    val settings: AppSettings,
)

fun Note.toCardUiModel(): NoteCardUiModel =
    NoteCardUiModel(
        id = id,
        title = title,
        preview = content.previewText(),
        folderName = folder?.name,
        categoryName = category?.name,
        tags = tags,
        isPinned = isPinned,
        isFavorite = isFavorite,
        updatedAt = updatedAt,
        wordCount = content.wordCount(),
    )

fun String.wordCount(): Int =
    Regex("\\S+").findAll(this.trim()).count()

fun String.previewText(): String =
    lineSequence()
        .map { line -> line.trim() }
        .firstOrNull { line -> line.isNotBlank() }
        .orEmpty()
        .take(180)

fun String.readingMinutes(): Int =
    (wordCount().coerceAtLeast(1) + 219) / 220
