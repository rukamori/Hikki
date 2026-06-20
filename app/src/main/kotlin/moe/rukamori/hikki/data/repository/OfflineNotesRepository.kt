package moe.rukamori.hikki.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import moe.rukamori.hikki.data.db.CategoryEntity
import moe.rukamori.hikki.data.db.FolderEntity
import moe.rukamori.hikki.data.db.HikkiDatabase
import moe.rukamori.hikki.data.db.NoteEntity
import moe.rukamori.hikki.data.db.NoteSearchEntity
import moe.rukamori.hikki.data.db.NoteTagMap
import moe.rukamori.hikki.data.db.TagEntity
import moe.rukamori.hikki.data.db.toDomain
import moe.rukamori.hikki.domain.model.Category
import moe.rukamori.hikki.domain.model.Folder
import moe.rukamori.hikki.domain.model.Note
import moe.rukamori.hikki.domain.model.NoteLifecycle
import moe.rukamori.hikki.domain.model.NoteSort
import moe.rukamori.hikki.domain.model.Tag
import moe.rukamori.hikki.domain.repository.NotesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineNotesRepository
    @Inject
    constructor(
        private val database: HikkiDatabase,
    ) : NotesRepository {
        private val dao = database.dao

        override fun observeNotes(
            lifecycle: NoteLifecycle,
            sort: NoteSort,
        ): Flow<List<Note>> =
            dao.observeNotes(lifecycle)
                .map { notes -> notes.map { it.toDomain() }.sortedBy(sort) }

        override fun observeNote(noteId: Long): Flow<Note?> =
            dao.observeNote(noteId).map { it?.toDomain() }

        override fun searchNotes(
            query: String,
            lifecycle: NoteLifecycle,
            sort: NoteSort,
        ): Flow<List<Note>> {
            val ftsQuery = query.toFtsQuery()
            return if (ftsQuery.isBlank()) {
                observeNotes(lifecycle, sort)
            } else {
                dao.searchNotes(ftsQuery, lifecycle)
                    .map { notes -> notes.map { it.toDomain() }.sortedBy(sort) }
            }
        }

        override fun observeFolders(): Flow<List<Folder>> =
            dao.observeFolders().map { folders -> folders.map(FolderEntity::toDomain) }

        override fun observeCategories(): Flow<List<Category>> =
            dao.observeCategories().map { categories -> categories.map(CategoryEntity::toDomain) }

        override fun observeTags(): Flow<List<Tag>> =
            dao.observeTags().map { tags -> tags.map(TagEntity::toDomain) }

        override suspend fun createNote(
            title: String,
            content: String,
        ): Long =
            withContext(Dispatchers.IO) {
                val now = System.currentTimeMillis()
                database.withTransaction {
                    val id =
                        dao.insert(
                            NoteEntity(
                                title = title,
                                content = content,
                                createdAt = now,
                                updatedAt = now,
                            ),
                        )
                    dao.insert(NoteSearchEntity(rowId = id, title = title, content = content))
                    id
                }
            }

        override suspend fun saveNote(
            noteId: Long,
            title: String,
            content: String,
        ) {
            withExistingNote(noteId) { note ->
                val updated =
                    note.copy(
                        title = title,
                        content = content,
                        updatedAt = System.currentTimeMillis(),
                    )
                dao.update(updated)
                dao.upsert(NoteSearchEntity(rowId = noteId, title = title, content = content))
            }
        }

        override suspend fun duplicateNote(noteId: Long): Long? =
            withContext(Dispatchers.IO) {
                val source = dao.getNote(noteId) ?: return@withContext null
                val now = System.currentTimeMillis()
                database.withTransaction {
                    val newId =
                        dao.insert(
                            source.note.copy(
                                id = 0,
                                title = source.note.title,
                                lifecycle = NoteLifecycle.Active,
                                createdAt = now,
                                updatedAt = now,
                                archivedAt = null,
                                trashedAt = null,
                            ),
                        )
                    dao.insert(
                        NoteSearchEntity(
                            rowId = newId,
                            title = source.note.title,
                            content = source.note.content,
                        ),
                    )
                    source.tags.forEach { tag ->
                        dao.insert(NoteTagMap(noteId = newId, tagId = tag.id))
                    }
                    newId
                }
            }

        override suspend fun setPinned(
            noteId: Long,
            pinned: Boolean,
        ) {
            withExistingNote(noteId) { note ->
                dao.update(note.copy(isPinned = pinned, updatedAt = System.currentTimeMillis()))
            }
        }

        override suspend fun setFavorite(
            noteId: Long,
            favorite: Boolean,
        ) {
            withExistingNote(noteId) { note ->
                dao.update(note.copy(isFavorite = favorite, updatedAt = System.currentTimeMillis()))
            }
        }

        override suspend fun archive(noteId: Long) {
            withExistingNote(noteId) { note ->
                val now = System.currentTimeMillis()
                dao.update(
                    note.copy(
                        lifecycle = NoteLifecycle.Archived,
                        archivedAt = now,
                        trashedAt = null,
                        updatedAt = now,
                    ),
                )
            }
        }

        override suspend fun restore(noteId: Long) {
            withExistingNote(noteId) { note ->
                dao.update(
                    note.copy(
                        lifecycle = NoteLifecycle.Active,
                        archivedAt = null,
                        trashedAt = null,
                        updatedAt = System.currentTimeMillis(),
                    ),
                )
            }
        }

        override suspend fun moveToTrash(noteId: Long) {
            withExistingNote(noteId) { note ->
                val now = System.currentTimeMillis()
                dao.update(
                    note.copy(
                        lifecycle = NoteLifecycle.Trashed,
                        trashedAt = now,
                        updatedAt = now,
                    ),
                )
            }
        }

        override suspend fun permanentlyDelete(noteId: Long) {
            withContext(Dispatchers.IO) {
                database.withTransaction {
                    dao.getNoteEntity(noteId)?.let { note ->
                        dao.deleteSearch(noteId)
                        dao.delete(note)
                    }
                }
            }
        }

        override suspend fun assignFolder(
            noteId: Long,
            folderId: Long?,
        ) {
            withExistingNote(noteId) { note ->
                dao.update(note.copy(folderId = folderId, updatedAt = System.currentTimeMillis()))
            }
        }

        override suspend fun assignCategory(
            noteId: Long,
            categoryId: Long?,
        ) {
            withExistingNote(noteId) { note ->
                dao.update(note.copy(categoryId = categoryId, updatedAt = System.currentTimeMillis()))
            }
        }

        override suspend fun replaceTags(
            noteId: Long,
            tagIds: Set<Long>,
        ) {
            withContext(Dispatchers.IO) {
                database.withTransaction {
                    dao.clearNoteTags(noteId)
                    tagIds.forEach { tagId ->
                        dao.insert(NoteTagMap(noteId = noteId, tagId = tagId))
                    }
                }
            }
        }

        override suspend fun createFolder(name: String) {
            val trimmed = name.trim()
            if (trimmed.isBlank()) return
            withContext(Dispatchers.IO) {
                dao.insert(FolderEntity(name = trimmed, createdAt = System.currentTimeMillis()))
            }
        }

        override suspend fun renameFolder(
            folderId: Long,
            name: String,
        ) {
            val trimmed = name.trim()
            if (trimmed.isBlank()) return
            withContext(Dispatchers.IO) {
                dao.getFolder(folderId)?.let { dao.update(it.copy(name = trimmed)) }
            }
        }

        override suspend fun deleteFolder(folderId: Long) {
            withContext(Dispatchers.IO) {
                database.withTransaction {
                    dao.clearFolderFromNotes(folderId)
                    dao.getFolder(folderId)?.let { dao.delete(it) }
                }
            }
        }

        override suspend fun createCategory(name: String) {
            val trimmed = name.trim()
            if (trimmed.isBlank()) return
            withContext(Dispatchers.IO) {
                dao.insert(CategoryEntity(name = trimmed, createdAt = System.currentTimeMillis()))
            }
        }

        override suspend fun renameCategory(
            categoryId: Long,
            name: String,
        ) {
            val trimmed = name.trim()
            if (trimmed.isBlank()) return
            withContext(Dispatchers.IO) {
                dao.getCategory(categoryId)?.let { dao.update(it.copy(name = trimmed)) }
            }
        }

        override suspend fun deleteCategory(categoryId: Long) {
            withContext(Dispatchers.IO) {
                database.withTransaction {
                    dao.clearCategoryFromNotes(categoryId)
                    dao.getCategory(categoryId)?.let { dao.delete(it) }
                }
            }
        }

        override suspend fun createTag(
            name: String,
            colorSeed: Long,
        ) {
            val trimmed = name.trim()
            if (trimmed.isBlank()) return
            withContext(Dispatchers.IO) {
                dao.insert(
                    TagEntity(
                        name = trimmed,
                        colorSeed = colorSeed,
                        createdAt = System.currentTimeMillis(),
                    ),
                )
            }
        }

        override suspend fun renameTag(
            tagId: Long,
            name: String,
        ) {
            val trimmed = name.trim()
            if (trimmed.isBlank()) return
            withContext(Dispatchers.IO) {
                dao.getTag(tagId)?.let { dao.update(it.copy(name = trimmed)) }
            }
        }

        override suspend fun deleteTag(tagId: Long) {
            withContext(Dispatchers.IO) {
                dao.getTag(tagId)?.let { dao.delete(it) }
            }
        }

        private suspend fun withExistingNote(
            noteId: Long,
            block: suspend (NoteEntity) -> Unit,
        ) {
            withContext(Dispatchers.IO) {
                database.withTransaction {
                    dao.getNoteEntity(noteId)?.let { note -> block(note) }
                }
            }
        }

        private fun List<Note>.sortedBy(sort: NoteSort): List<Note> =
            when (sort) {
                NoteSort.Updated -> sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt })
                NoteSort.Created -> sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.createdAt })
                NoteSort.Title -> sortedWith(compareByDescending<Note> { it.isPinned }.thenBy { it.title.lowercase() })
                NoteSort.Pinned -> sortedWith(compareByDescending<Note> { it.isPinned }.thenByDescending { it.updatedAt })
                NoteSort.Favorite -> sortedWith(compareByDescending<Note> { it.isFavorite }.thenByDescending { it.updatedAt })
            }

        private fun String.toFtsQuery(): String =
            Regex("[\\p{L}\\p{N}_]+")
                .findAll(this)
                .map { match -> "${match.value}*" }
                .take(8)
                .joinToString(" OR ")
    }
