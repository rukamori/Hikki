package moe.rukamori.hikki.data.db

import moe.rukamori.hikki.domain.model.Category
import moe.rukamori.hikki.domain.model.Folder
import moe.rukamori.hikki.domain.model.Note
import moe.rukamori.hikki.domain.model.Tag

fun NoteWithRelations.toDomain(): Note =
    Note(
        id = note.id,
        title = note.title,
        content = note.content,
        folder = folder?.toDomain(),
        category = category?.toDomain(),
        tags = tags.map(TagEntity::toDomain),
        lifecycle = note.lifecycle,
        isPinned = note.isPinned,
        isFavorite = note.isFavorite,
        createdAt = note.createdAt,
        updatedAt = note.updatedAt,
        archivedAt = note.archivedAt,
        trashedAt = note.trashedAt,
    )

fun FolderEntity.toDomain(): Folder =
    Folder(
        id = id,
        name = name,
        createdAt = createdAt,
    )

fun CategoryEntity.toDomain(): Category =
    Category(
        id = id,
        name = name,
        createdAt = createdAt,
    )

fun TagEntity.toDomain(): Tag =
    Tag(
        id = id,
        name = name,
        colorSeed = colorSeed,
        createdAt = createdAt,
    )
