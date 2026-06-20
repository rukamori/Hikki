package moe.rukamori.hikki.domain.repository

import kotlinx.coroutines.flow.Flow
import moe.rukamori.hikki.domain.model.Category
import moe.rukamori.hikki.domain.model.Folder
import moe.rukamori.hikki.domain.model.Note
import moe.rukamori.hikki.domain.model.NoteLifecycle
import moe.rukamori.hikki.domain.model.NoteSort
import moe.rukamori.hikki.domain.model.Tag

interface NotesRepository {
    fun observeNotes(
        lifecycle: NoteLifecycle,
        sort: NoteSort,
    ): Flow<List<Note>>

    fun observeNote(noteId: Long): Flow<Note?>

    fun searchNotes(
        query: String,
        lifecycle: NoteLifecycle,
        sort: NoteSort,
    ): Flow<List<Note>>

    fun observeFolders(): Flow<List<Folder>>

    fun observeCategories(): Flow<List<Category>>

    fun observeTags(): Flow<List<Tag>>

    suspend fun createNote(
        title: String,
        content: String,
    ): Long

    suspend fun saveNote(
        noteId: Long,
        title: String,
        content: String,
    )

    suspend fun duplicateNote(noteId: Long): Long?

    suspend fun setPinned(
        noteId: Long,
        pinned: Boolean,
    )

    suspend fun setFavorite(
        noteId: Long,
        favorite: Boolean,
    )

    suspend fun archive(noteId: Long)

    suspend fun restore(noteId: Long)

    suspend fun moveToTrash(noteId: Long)

    suspend fun permanentlyDelete(noteId: Long)

    suspend fun assignFolder(
        noteId: Long,
        folderId: Long?,
    )

    suspend fun assignCategory(
        noteId: Long,
        categoryId: Long?,
    )

    suspend fun replaceTags(
        noteId: Long,
        tagIds: Set<Long>,
    )

    suspend fun createFolder(name: String)

    suspend fun renameFolder(
        folderId: Long,
        name: String,
    )

    suspend fun deleteFolder(folderId: Long)

    suspend fun createCategory(name: String)

    suspend fun renameCategory(
        categoryId: Long,
        name: String,
    )

    suspend fun deleteCategory(categoryId: Long)

    suspend fun createTag(
        name: String,
        colorSeed: Long,
    )

    suspend fun renameTag(
        tagId: Long,
        name: String,
    )

    suspend fun deleteTag(tagId: Long)
}
