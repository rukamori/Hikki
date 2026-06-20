package moe.rukamori.hikki.domain.usecase

import kotlinx.coroutines.flow.Flow
import moe.rukamori.hikki.domain.model.Category
import moe.rukamori.hikki.domain.model.Folder
import moe.rukamori.hikki.domain.model.Note
import moe.rukamori.hikki.domain.model.NoteLifecycle
import moe.rukamori.hikki.domain.model.NoteSort
import moe.rukamori.hikki.domain.model.Tag
import moe.rukamori.hikki.domain.repository.NotesRepository
import javax.inject.Inject

class NoteUseCases
    @Inject
    constructor(
        private val repository: NotesRepository,
    ) {
        fun observeNotes(
            lifecycle: NoteLifecycle,
            sort: NoteSort,
        ): Flow<List<Note>> = repository.observeNotes(lifecycle, sort)

        fun observeNote(noteId: Long): Flow<Note?> = repository.observeNote(noteId)

        fun searchNotes(
            query: String,
            lifecycle: NoteLifecycle,
            sort: NoteSort,
        ): Flow<List<Note>> = repository.searchNotes(query, lifecycle, sort)

        fun observeFolders(): Flow<List<Folder>> = repository.observeFolders()

        fun observeCategories(): Flow<List<Category>> = repository.observeCategories()

        fun observeTags(): Flow<List<Tag>> = repository.observeTags()

        suspend fun createNote(): Long = repository.createNote(title = "", content = "")

        suspend fun saveNote(
            noteId: Long,
            title: String,
            content: String,
        ) = repository.saveNote(noteId, title, content)

        suspend fun duplicateNote(noteId: Long): Long? = repository.duplicateNote(noteId)

        suspend fun setPinned(
            noteId: Long,
            pinned: Boolean,
        ) = repository.setPinned(noteId, pinned)

        suspend fun setFavorite(
            noteId: Long,
            favorite: Boolean,
        ) = repository.setFavorite(noteId, favorite)

        suspend fun archive(noteId: Long) = repository.archive(noteId)

        suspend fun restore(noteId: Long) = repository.restore(noteId)

        suspend fun moveToTrash(noteId: Long) = repository.moveToTrash(noteId)

        suspend fun permanentlyDelete(noteId: Long) = repository.permanentlyDelete(noteId)

        suspend fun assignFolder(
            noteId: Long,
            folderId: Long?,
        ) = repository.assignFolder(noteId, folderId)

        suspend fun assignCategory(
            noteId: Long,
            categoryId: Long?,
        ) = repository.assignCategory(noteId, categoryId)

        suspend fun replaceTags(
            noteId: Long,
            tagIds: Set<Long>,
        ) = repository.replaceTags(noteId, tagIds)

        suspend fun createFolder(name: String) = repository.createFolder(name)

        suspend fun renameFolder(
            folderId: Long,
            name: String,
        ) = repository.renameFolder(folderId, name)

        suspend fun deleteFolder(folderId: Long) = repository.deleteFolder(folderId)

        suspend fun createCategory(name: String) = repository.createCategory(name)

        suspend fun renameCategory(
            categoryId: Long,
            name: String,
        ) = repository.renameCategory(categoryId, name)

        suspend fun deleteCategory(categoryId: Long) = repository.deleteCategory(categoryId)

        suspend fun createTag(
            name: String,
            colorSeed: Long,
        ) = repository.createTag(name, colorSeed)

        suspend fun renameTag(
            tagId: Long,
            name: String,
        ) = repository.renameTag(tagId, name)

        suspend fun deleteTag(tagId: Long) = repository.deleteTag(tagId)
    }
