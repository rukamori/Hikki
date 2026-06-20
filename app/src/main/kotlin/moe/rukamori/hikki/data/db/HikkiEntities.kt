package moe.rukamori.hikki.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.Embedded
import moe.rukamori.hikki.domain.model.NoteLifecycle

@Entity(
    tableName = "folder",
    indices = [Index(value = ["name"], unique = true)],
)
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
)

@Entity(
    tableName = "category",
    indices = [Index(value = ["name"], unique = true)],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
)

@Entity(
    tableName = "tag",
    indices = [Index(value = ["name"], unique = true)],
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorSeed: Long,
    val createdAt: Long,
)

@Entity(
    tableName = "note",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("folderId"),
        Index("categoryId"),
        Index("lifecycle"),
        Index("updatedAt"),
        Index("createdAt"),
        Index("isPinned"),
        Index("isFavorite"),
    ],
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val folderId: Long? = null,
    val categoryId: Long? = null,
    val lifecycle: NoteLifecycle = NoteLifecycle.Active,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val archivedAt: Long? = null,
    val trashedAt: Long? = null,
)

@Entity(
    tableName = "note_tag_map",
    primaryKeys = ["noteId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("noteId"), Index("tagId")],
)
data class NoteTagMap(
    val noteId: Long,
    val tagId: Long,
)

@Fts4(contentEntity = NoteEntity::class)
@Entity(tableName = "note_search")
data class NoteSearchEntity(
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    val rowId: Long,
    val title: String,
    val content: String,
)

data class NoteWithRelations(
    @Embedded val note: NoteEntity,
    @Relation(parentColumn = "folderId", entityColumn = "id")
    val folder: FolderEntity?,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: CategoryEntity?,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteTagMap::class,
            parentColumn = "noteId",
            entityColumn = "tagId",
        ),
    )
    val tags: List<TagEntity>,
)
