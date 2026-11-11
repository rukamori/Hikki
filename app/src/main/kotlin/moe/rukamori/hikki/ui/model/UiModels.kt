/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * GPL-3.0 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.hikki.ui.model

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.AppSettings
import moe.rukamori.hikki.domain.model.BackupSummary
import moe.rukamori.hikki.domain.model.Category
import moe.rukamori.hikki.domain.model.DisplayMode
import moe.rukamori.hikki.domain.model.EditorMode
import moe.rukamori.hikki.domain.model.Folder
import moe.rukamori.hikki.domain.model.Note
import moe.rukamori.hikki.domain.model.NoteLifecycle
import moe.rukamori.hikki.domain.model.NoteSort
import moe.rukamori.hikki.domain.model.Tag

@Immutable
data class NoteCardUiModel(
    val id: Long,
    val title: String,
    val preview: String,
    val folderId: Long?,
    val folderName: String?,
    val categoryName: String?,
    val tags: List<Tag>,
    val isPinned: Boolean,
    val isFavorite: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val archivedAt: Long?,
    val trashedAt: Long?,
    val wordCount: Int,
)

@Immutable
sealed interface NoteFilter {
    @Immutable
    data object All : NoteFilter

    @Immutable
    data object Favorites : NoteFilter

    @Immutable
    data class InFolder(val folderId: Long) : NoteFilter
}

@Immutable
data class NotesHomeUiModel(
    val notes: List<NoteCardUiModel>,
    val folders: List<Folder>,
    val tags: List<Tag>,
    val categories: List<Category>,
    val displayMode: DisplayMode,
    val sort: NoteSort,
    val filter: NoteFilter = NoteFilter.All,
    val totalCount: Int = notes.size,
    val favoriteCount: Int = notes.count { it.isFavorite },
)

@Immutable
sealed interface SearchScope {
    @Immutable
    data object Everywhere : SearchScope

    @Immutable
    data class InFolder(val folderId: Long) : SearchScope

    @Immutable
    data class WithTag(val tagId: Long) : SearchScope
}

@Immutable
data class SearchUiModel(
    val query: String,
    val notes: List<NoteCardUiModel>,
    val folders: List<Folder>,
    val tags: List<Tag>,
    val categories: List<Category>,
    val sort: NoteSort,
    val scope: SearchScope = SearchScope.Everywhere,
)

@Immutable
data class CollectionsUiModel(
    val folders: List<Folder>,
    val categories: List<Category>,
    val tags: List<Tag>,
    val folderNoteCounts: Map<Long, Int> = emptyMap(),
    val categoryNoteCounts: Map<Long, Int> = emptyMap(),
    val tagNoteCounts: Map<Long, Int> = emptyMap(),
)

@Immutable
data class NoteEditorUiModel(
    val id: Long,
    val lifecycle: NoteLifecycle,
    val title: String,
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

enum class DataOperation(@param:StringRes val errorRes: Int) {
    Backup(R.string.error_backup_write),
    Inspect(R.string.error_backup_read),
    Restore(R.string.error_backup_restore),
    ExportMarkdown(R.string.error_export_markdown),
    EmptyTrash(R.string.error_empty_trash),
}

@Immutable
data class PendingRestore(
    val uri: String,
    val summary: BackupSummary,
)

@Immutable
data class DataOperationUiState(
    val running: DataOperation? = null,
    val pendingRestore: PendingRestore? = null,
)

@Immutable
sealed interface DataMessage {
    @Immutable
    data class Text(@param:StringRes val messageRes: Int) : DataMessage

    @Immutable
    data class Count(
        @param:PluralsRes val messageRes: Int,
        val count: Int,
    ) : DataMessage
}

fun Note.toCardUiModel(): NoteCardUiModel =
    NoteCardUiModel(
        id = id,
        title = title,
        preview = content.previewText(),
        folderId = folder?.id,
        folderName = folder?.name,
        categoryName = category?.name,
        tags = tags,
        isPinned = isPinned,
        isFavorite = isFavorite,
        createdAt = createdAt,
        updatedAt = updatedAt,
        archivedAt = archivedAt,
        trashedAt = trashedAt,
        wordCount = content.wordCount(),
    )

fun String.wordCount(): Int =
    Regex("\\S+").findAll(this.trim()).count()

private val LeadingMarkdownSyntax = Regex("^(#{1,6}\\s+|>\\s?|[-*+]\\s+\\[[ xX]\\]\\s+|[-*+]\\s+|\\d+[.)]\\s+|\\|)")
private val InlineMarkdownSyntax = Regex("(\\*\\*|__|~~|`+|!?\\[([^\\]]*)\\]\\([^)]*\\)|\\|)")
private val HorizontalRuleLine = Regex("^([-*_]\\s*){3,}$")
private val TableDividerLine = Regex("^\\|?\\s*:?-{3,}.*$")

fun String.previewText(): String =
    lineSequence()
        .map { line -> line.trim() }
        .filter { line -> line.isNotBlank() && !line.startsWith("```") }
        .filterNot { line -> HorizontalRuleLine.matches(line) || TableDividerLine.matches(line) }
        .map { line ->
            line
                .replace(LeadingMarkdownSyntax, "")
                .replace(InlineMarkdownSyntax) { match -> match.groupValues[2] }
                .replace(Regex("\\s+"), " ")
                .trim()
        }
        .filter { line -> line.isNotBlank() }
        .take(6)
        .joinToString(separator = " ")
        .take(280)

fun String.readingMinutes(): Int =
    (wordCount().coerceAtLeast(1) + 219) / 220
