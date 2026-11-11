/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * GPL-3.0 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.hikki.data.backup

import kotlinx.serialization.Serializable

internal const val BackupFormat = "hikki-backup"
internal const val BackupVersion = 1

@Serializable
internal data class BackupFile(
    val format: String,
    val version: Int,
    val appVersion: String = "",
    val exportedAt: Long = 0L,
    val settings: BackupSettings? = null,
    val folders: List<BackupCollection> = emptyList(),
    val categories: List<BackupCollection> = emptyList(),
    val tags: List<BackupTag> = emptyList(),
    val notes: List<BackupNote> = emptyList(),
)

@Serializable
internal data class BackupSettings(
    val themeMode: String? = null,
    val dynamicColor: Boolean? = null,
    val pureBlack: Boolean? = null,
    val displayMode: String? = null,
    val sort: String? = null,
    val editorMode: String? = null,
    val autosaveDelayMillis: Long? = null,
    val trashRetention: String? = null,
)

@Serializable
internal data class BackupCollection(
    val id: Long,
    val name: String,
    val createdAt: Long = 0L,
)

@Serializable
internal data class BackupTag(
    val id: Long,
    val name: String,
    val colorSeed: Long = 0L,
    val createdAt: Long = 0L,
)

@Serializable
internal data class BackupNote(
    val id: Long,
    val title: String = "",
    val content: String = "",
    val folderId: Long? = null,
    val categoryId: Long? = null,
    val tagIds: List<Long> = emptyList(),
    val lifecycle: String = "Active",
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val archivedAt: Long? = null,
    val trashedAt: Long? = null,
)
