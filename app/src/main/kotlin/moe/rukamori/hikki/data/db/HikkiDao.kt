package moe.rukamori.hikki.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import moe.rukamori.hikki.domain.model.NoteLifecycle

@Dao
interface HikkiDao {
    @Transaction
    @Query("SELECT * FROM note WHERE lifecycle = :lifecycle ORDER BY isPinned DESC, updatedAt DESC")
    fun observeNotes(lifecycle: NoteLifecycle): Flow<List<NoteWithRelations>>

    @Transaction
    @Query("SELECT * FROM note WHERE id = :noteId LIMIT 1")
    fun observeNote(noteId: Long): Flow<NoteWithRelations?>

    @Transaction
    @Query(
        """
        SELECT * FROM note
        WHERE lifecycle = :lifecycle
        AND id IN (SELECT rowid FROM note_search WHERE note_search MATCH :query)
        ORDER BY isPinned DESC, updatedAt DESC
        """,
    )
    fun searchNotes(
        query: String,
        lifecycle: NoteLifecycle,
    ): Flow<List<NoteWithRelations>>

    @Transaction
    @Query("SELECT * FROM note WHERE lifecycle = :lifecycle AND folderId = :folderId ORDER BY isPinned DESC, updatedAt DESC")
    fun observeNotesByFolder(
        folderId: Long,
        lifecycle: NoteLifecycle = NoteLifecycle.Active,
    ): Flow<List<NoteWithRelations>>

    @Transaction
    @Query("SELECT * FROM note WHERE lifecycle = :lifecycle AND categoryId = :categoryId ORDER BY isPinned DESC, updatedAt DESC")
    fun observeNotesByCategory(
        categoryId: Long,
        lifecycle: NoteLifecycle = NoteLifecycle.Active,
    ): Flow<List<NoteWithRelations>>

    @Transaction
    @Query(
        """
        SELECT * FROM note
        WHERE lifecycle = :lifecycle
        AND id IN (SELECT noteId FROM note_tag_map WHERE tagId = :tagId)
        ORDER BY isPinned DESC, updatedAt DESC
        """,
    )
    fun observeNotesByTag(
        tagId: Long,
        lifecycle: NoteLifecycle = NoteLifecycle.Active,
    ): Flow<List<NoteWithRelations>>

    @Query("SELECT * FROM folder ORDER BY name")
    fun observeFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM category ORDER BY name")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM tag ORDER BY name")
    fun observeTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM note WHERE id = :noteId LIMIT 1")
    suspend fun getNoteEntity(noteId: Long): NoteEntity?

    @Transaction
    @Query("SELECT * FROM note WHERE id = :noteId LIMIT 1")
    suspend fun getNote(noteId: Long): NoteWithRelations?

    @Query("SELECT * FROM folder WHERE id = :folderId LIMIT 1")
    suspend fun getFolder(folderId: Long): FolderEntity?

    @Query("SELECT * FROM category WHERE id = :categoryId LIMIT 1")
    suspend fun getCategory(categoryId: Long): CategoryEntity?

    @Query("SELECT * FROM tag WHERE id = :tagId LIMIT 1")
    suspend fun getTag(tagId: Long): TagEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(folder: FolderEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(search: NoteSearchEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(map: NoteTagMap)

    @Update
    suspend fun update(note: NoteEntity)

    @Update
    suspend fun update(folder: FolderEntity)

    @Update
    suspend fun update(category: CategoryEntity)

    @Update
    suspend fun update(tag: TagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(search: NoteSearchEntity)

    @Query("DELETE FROM note_search WHERE rowid = :noteId")
    suspend fun deleteSearch(noteId: Long)

    @Query("DELETE FROM note_tag_map WHERE noteId = :noteId")
    suspend fun clearNoteTags(noteId: Long)

    @Query("DELETE FROM note_tag_map WHERE noteId = :noteId AND tagId = :tagId")
    suspend fun removeNoteTag(
        noteId: Long,
        tagId: Long,
    )

    @Query("UPDATE note SET folderId = NULL WHERE folderId = :folderId")
    suspend fun clearFolderFromNotes(folderId: Long)

    @Query("UPDATE note SET categoryId = NULL WHERE categoryId = :categoryId")
    suspend fun clearCategoryFromNotes(categoryId: Long)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Delete
    suspend fun delete(folder: FolderEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Delete
    suspend fun delete(tag: TagEntity)
}
