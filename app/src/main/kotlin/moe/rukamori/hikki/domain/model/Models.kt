package moe.rukamori.hikki.domain.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import moe.rukamori.hikki.R

enum class NoteLifecycle {
    Active,
    Archived,
    Trashed,
}

enum class NoteSort(@StringRes val labelRes: Int) {
    Updated(R.string.sort_updated),
    Created(R.string.sort_created),
    Title(R.string.sort_title),
    Pinned(R.string.sort_pinned),
    Favorite(R.string.sort_favorite),
}

enum class DisplayMode(@StringRes val labelRes: Int) {
    List(R.string.display_list),
    Grid(R.string.display_grid),
}

enum class EditorMode(@StringRes val labelRes: Int) {
    Edit(R.string.editor_mode_edit),
    Split(R.string.editor_mode_split),
    Preview(R.string.editor_mode_preview),
}

enum class ThemeMode(@StringRes val labelRes: Int) {
    System(R.string.theme_system),
    Light(R.string.theme_light),
    Dark(R.string.theme_dark),
}

@Immutable
data class Note(
    val id: Long,
    val title: String,
    val content: String,
    val folder: Folder?,
    val category: Category?,
    val tags: List<Tag>,
    val lifecycle: NoteLifecycle,
    val isPinned: Boolean,
    val isFavorite: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val archivedAt: Long?,
    val trashedAt: Long?,
)

@Immutable
data class Folder(
    val id: Long,
    val name: String,
    val createdAt: Long,
)

@Immutable
data class Category(
    val id: Long,
    val name: String,
    val createdAt: Long,
)

@Immutable
data class Tag(
    val id: Long,
    val name: String,
    val colorSeed: Long,
    val createdAt: Long,
)

@Immutable
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val dynamicColor: Boolean = true,
    val pureBlack: Boolean = false,
    val displayMode: DisplayMode = DisplayMode.List,
    val sort: NoteSort = NoteSort.Updated,
    val defaultEditorMode: EditorMode = EditorMode.Edit,
    val autosaveDelayMillis: Long = 800L,
)

@Immutable
data class NoteSearchFilter(
    val query: String = "",
    val folderId: Long? = null,
    val tagId: Long? = null,
    val categoryId: Long? = null,
    val lifecycle: NoteLifecycle = NoteLifecycle.Active,
)

sealed interface ScreenState<out T> {
    data object Loading : ScreenState<Nothing>
    data class Success<T>(val data: T) : ScreenState<T>
    data object Empty : ScreenState<Nothing>
    data class Error(@StringRes val messageRes: Int) : ScreenState<Nothing>
}
