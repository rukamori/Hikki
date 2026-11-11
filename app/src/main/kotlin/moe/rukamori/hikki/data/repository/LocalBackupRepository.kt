/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * GPL-3.0 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.hikki.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import moe.rukamori.hikki.BuildConfig
import moe.rukamori.hikki.R
import moe.rukamori.hikki.data.backup.BackupCollection
import moe.rukamori.hikki.data.backup.BackupFile
import moe.rukamori.hikki.data.backup.BackupFormat
import moe.rukamori.hikki.data.backup.BackupNote
import moe.rukamori.hikki.data.backup.BackupSettings
import moe.rukamori.hikki.data.backup.BackupTag
import moe.rukamori.hikki.data.backup.BackupVersion
import moe.rukamori.hikki.data.db.CategoryEntity
import moe.rukamori.hikki.data.db.FolderEntity
import moe.rukamori.hikki.data.db.HikkiDatabase
import moe.rukamori.hikki.data.db.NoteEntity
import moe.rukamori.hikki.data.db.NoteTagMap
import moe.rukamori.hikki.data.db.TagEntity
import moe.rukamori.hikki.domain.model.AppSettings
import moe.rukamori.hikki.domain.model.BackupSummary
import moe.rukamori.hikki.domain.model.InvalidBackupException
import moe.rukamori.hikki.domain.model.NoteLifecycle
import moe.rukamori.hikki.domain.model.RestoreMode
import moe.rukamori.hikki.domain.model.UnsupportedBackupException
import moe.rukamori.hikki.domain.repository.BackupRepository
import moe.rukamori.hikki.domain.repository.SettingsRepository
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalBackupRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val database: HikkiDatabase,
        private val settingsRepository: SettingsRepository,
    ) : BackupRepository {
        private val dao = database.dao
        private val json =
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
                explicitNulls = false
            }

        override suspend fun exportBackup(destination: Uri): BackupSummary =
            withContext(Dispatchers.IO) {
                val settings = settingsRepository.settings.first()
                val snapshot = readSnapshot()
                val tagsByNote = snapshot.noteTags.groupBy(NoteTagMap::noteId, NoteTagMap::tagId)
                val exportedAt = System.currentTimeMillis()
                val file =
                    BackupFile(
                        format = BackupFormat,
                        version = BackupVersion,
                        appVersion = BuildConfig.VERSION_NAME,
                        exportedAt = exportedAt,
                        settings = settings.toBackup(),
                        folders = snapshot.folders.map { BackupCollection(it.id, it.name, it.createdAt) },
                        categories = snapshot.categories.map { BackupCollection(it.id, it.name, it.createdAt) },
                        tags = snapshot.tags.map { BackupTag(it.id, it.name, it.colorSeed, it.createdAt) },
                        notes = snapshot.notes.map { it.toBackup(tagsByNote[it.id].orEmpty()) },
                    )
                val text = json.encodeToString(BackupFile.serializer(), file)
                val output = context.contentResolver.openOutputStream(destination) ?: throw IOException()
                output.bufferedWriter(Charsets.UTF_8).use { writer -> writer.write(text) }
                settingsRepository.setLastBackupAt(exportedAt)
                file.toSummary()
            }

        override suspend fun inspectBackup(source: Uri): BackupSummary =
            withContext(Dispatchers.IO) { readBackup(source).toSummary() }

        override suspend fun restoreBackup(
            source: Uri,
            mode: RestoreMode,
        ): BackupSummary =
            withContext(Dispatchers.IO) {
                val file = readBackup(source)
                val keepIds = mode == RestoreMode.Replace
                val restored =
                    database.withTransaction {
                        if (mode == RestoreMode.Replace) clearLibrary()
                        val folderIds = file.folders.mapNotNull { item -> restoreFolder(item, keepIds)?.let { item.id to it } }.toMap()
                        val categoryIds = file.categories.mapNotNull { item -> restoreCategory(item, keepIds)?.let { item.id to it } }.toMap()
                        val tagIds = file.tags.mapNotNull { item -> restoreTag(item, keepIds)?.let { item.id to it } }.toMap()
                        val usedNoteIds = HashSet<Long>()
                        var noteCount = 0
                        file.notes.forEach { note ->
                            val createdAt =
                                note.createdAt.takeIf { it > 0 }
                                    ?: note.updatedAt.takeIf { it > 0 }
                                    ?: System.currentTimeMillis()
                            if (mode == RestoreMode.Merge && dao.countMatchingNotes(note.title, note.content, createdAt) > 0) {
                                return@forEach
                            }
                            val requestedId = if (keepIds && note.id > 0 && note.id !in usedNoteIds) note.id else 0L
                            val lifecycle = note.lifecycle.toEnum(NoteLifecycle.Active)
                            val updatedAt = note.updatedAt.takeIf { it > 0 } ?: createdAt
                            val noteId =
                                dao.insert(
                                    NoteEntity(
                                        id = requestedId,
                                        title = note.title,
                                        content = note.content,
                                        folderId = note.folderId?.let(folderIds::get),
                                        categoryId = note.categoryId?.let(categoryIds::get),
                                        lifecycle = lifecycle,
                                        isPinned = note.isPinned,
                                        isFavorite = note.isFavorite,
                                        createdAt = createdAt,
                                        updatedAt = updatedAt,
                                        archivedAt = note.archivedAt,
                                        trashedAt = note.trashedAt ?: updatedAt.takeIf { lifecycle == NoteLifecycle.Trashed },
                                    ),
                                )
                            usedNoteIds += noteId
                            note.tagIds.distinct().mapNotNull(tagIds::get).forEach { tagId ->
                                dao.insert(NoteTagMap(noteId = noteId, tagId = tagId))
                            }
                            noteCount++
                        }
                        database.rebuildSearchIndex()
                        noteCount
                    }
                file.settings?.let { backup ->
                    settingsRepository.restorePreferences(backup.applyTo(settingsRepository.settings.first()))
                }
                file.toSummary().copy(notes = restored)
            }

        override suspend fun exportMarkdown(destination: Uri): Int =
            withContext(Dispatchers.IO) {
                val snapshot = readSnapshot()
                val folders = snapshot.folders.associateBy(FolderEntity::id)
                val categories = snapshot.categories.associateBy(CategoryEntity::id)
                val tags = snapshot.tags.associateBy(TagEntity::id)
                val tagsByNote = snapshot.noteTags.groupBy(NoteTagMap::noteId, NoteTagMap::tagId)
                val archiveDirectory = context.getString(R.string.archive).toSafeFileName()
                val notes = snapshot.notes.filter { it.lifecycle != NoteLifecycle.Trashed }
                val output = context.contentResolver.openOutputStream(destination) ?: throw IOException()
                ZipOutputStream(BufferedOutputStream(output)).use { zip ->
                    val usedPaths = HashSet<String>()
                    notes.forEach { note ->
                        val folder = note.folderId?.let(folders::get)
                        val directory =
                            buildString {
                                if (note.lifecycle == NoteLifecycle.Archived && archiveDirectory.isNotEmpty()) {
                                    append(archiveDirectory).append('/')
                                }
                                folder?.name?.toSafeFileName()?.takeIf(String::isNotEmpty)?.let { append(it).append('/') }
                            }
                        val baseName = note.title.toSafeFileName().ifEmpty { "note-${note.id}" }
                        var path = "$directory$baseName.md"
                        var suffix = 2
                        while (!usedPaths.add(path.lowercase())) {
                            path = "$directory$baseName ($suffix).md"
                            suffix++
                        }
                        val markdown =
                            note.toMarkdown(
                                folder = folder?.name,
                                category = note.categoryId?.let(categories::get)?.name,
                                tags = tagsByNote[note.id].orEmpty().mapNotNull { tags[it]?.name },
                            )
                        zip.putNextEntry(ZipEntry(path).apply { time = note.updatedAt })
                        zip.write(markdown.toByteArray(Charsets.UTF_8))
                        zip.closeEntry()
                    }
                }
                notes.size
            }

        private suspend fun readSnapshot(): Snapshot =
            database.withTransaction {
                Snapshot(
                    notes = dao.getAllNotes(),
                    folders = dao.getAllFolders(),
                    categories = dao.getAllCategories(),
                    tags = dao.getAllTags(),
                    noteTags = dao.getAllNoteTags(),
                )
            }

        private suspend fun clearLibrary() {
            dao.deleteAllNoteTags()
            dao.deleteAllNotes()
            dao.deleteAllFolders()
            dao.deleteAllCategories()
            dao.deleteAllTags()
        }

        private suspend fun restoreFolder(
            item: BackupCollection,
            keepId: Boolean,
        ): Long? {
            val name = item.name.trim().takeIf(String::isNotEmpty) ?: return null
            dao.getFolderByName(name)?.let { return it.id }
            val createdAt = item.createdAt.takeIf { it > 0 } ?: System.currentTimeMillis()
            return insertWithFallback(keepId, item.id) { id ->
                dao.insert(FolderEntity(id = id, name = name, createdAt = createdAt))
            } ?: dao.getFolderByName(name)?.id
        }

        private suspend fun restoreCategory(
            item: BackupCollection,
            keepId: Boolean,
        ): Long? {
            val name = item.name.trim().takeIf(String::isNotEmpty) ?: return null
            dao.getCategoryByName(name)?.let { return it.id }
            val createdAt = item.createdAt.takeIf { it > 0 } ?: System.currentTimeMillis()
            return insertWithFallback(keepId, item.id) { id ->
                dao.insert(CategoryEntity(id = id, name = name, createdAt = createdAt))
            } ?: dao.getCategoryByName(name)?.id
        }

        private suspend fun restoreTag(
            item: BackupTag,
            keepId: Boolean,
        ): Long? {
            val name = item.name.trim().takeIf(String::isNotEmpty) ?: return null
            dao.getTagByName(name)?.let { return it.id }
            val createdAt = item.createdAt.takeIf { it > 0 } ?: System.currentTimeMillis()
            return insertWithFallback(keepId, item.id) { id ->
                dao.insert(TagEntity(id = id, name = name, colorSeed = item.colorSeed, createdAt = createdAt))
            } ?: dao.getTagByName(name)?.id
        }

        private suspend fun insertWithFallback(
            keepId: Boolean,
            originalId: Long,
            insert: suspend (Long) -> Long,
        ): Long? {
            if (keepId && originalId > 0) {
                insert(originalId).takeIf { it > 0 }?.let { return it }
            }
            return insert(0L).takeIf { it > 0 }
        }

        private fun readBackup(source: Uri): BackupFile {
            val text = readText(source)
            val file =
                try {
                    json.decodeFromString(BackupFile.serializer(), text)
                } catch (error: IllegalArgumentException) {
                    throw InvalidBackupException(error)
                }
            if (file.format != BackupFormat) throw InvalidBackupException()
            if (file.version !in 1..BackupVersion) throw UnsupportedBackupException(file.version)
            return file
        }

        private fun readText(source: Uri): String {
            val input = context.contentResolver.openInputStream(source) ?: throw IOException()
            return input.use { stream ->
                val buffer = ByteArrayOutputStream()
                val chunk = ByteArray(DEFAULT_BUFFER_SIZE)
                var total = 0L
                while (true) {
                    val read = stream.read(chunk)
                    if (read < 0) break
                    total += read
                    if (total > MaxBackupBytes) throw InvalidBackupException()
                    buffer.write(chunk, 0, read)
                }
                buffer.toString(Charsets.UTF_8.name())
            }
        }

        private fun BackupFile.toSummary(): BackupSummary =
            BackupSummary(
                exportedAt = exportedAt,
                appVersion = appVersion,
                notes = notes.size,
                folders = folders.size,
                categories = categories.size,
                tags = tags.size,
            )

        private fun AppSettings.toBackup(): BackupSettings =
            BackupSettings(
                themeMode = themeMode.name,
                dynamicColor = dynamicColor,
                pureBlack = pureBlack,
                displayMode = displayMode.name,
                sort = sort.name,
                editorMode = defaultEditorMode.name,
                autosaveDelayMillis = autosaveDelayMillis,
                trashRetention = trashRetention.name,
            )

        private fun BackupSettings.applyTo(current: AppSettings): AppSettings =
            current.copy(
                themeMode = themeMode.toEnum(current.themeMode),
                dynamicColor = dynamicColor ?: current.dynamicColor,
                pureBlack = pureBlack ?: current.pureBlack,
                displayMode = displayMode.toEnum(current.displayMode),
                sort = sort.toEnum(current.sort),
                defaultEditorMode = editorMode.toEnum(current.defaultEditorMode),
                autosaveDelayMillis = autosaveDelayMillis ?: current.autosaveDelayMillis,
                trashRetention = trashRetention.toEnum(current.trashRetention),
            )

        private fun NoteEntity.toBackup(tagIds: List<Long>): BackupNote =
            BackupNote(
                id = id,
                title = title,
                content = content,
                folderId = folderId,
                categoryId = categoryId,
                tagIds = tagIds,
                lifecycle = lifecycle.name,
                isPinned = isPinned,
                isFavorite = isFavorite,
                createdAt = createdAt,
                updatedAt = updatedAt,
                archivedAt = archivedAt,
                trashedAt = trashedAt,
            )

        private fun NoteEntity.toMarkdown(
            folder: String?,
            category: String?,
            tags: List<String>,
        ): String =
            buildString {
                appendLine("---")
                if (title.isNotBlank()) appendLine("title: ${title.yamlQuoted()}")
                appendLine("created: ${Instant.ofEpochMilli(createdAt)}")
                appendLine("updated: ${Instant.ofEpochMilli(updatedAt)}")
                folder?.let { appendLine("folder: ${it.yamlQuoted()}") }
                category?.let { appendLine("category: ${it.yamlQuoted()}") }
                if (tags.isNotEmpty()) appendLine("tags: [${tags.joinToString(", ") { it.yamlQuoted() }}]")
                if (isPinned) appendLine("pinned: true")
                if (isFavorite) appendLine("favorite: true")
                if (lifecycle == NoteLifecycle.Archived) appendLine("archived: true")
                appendLine("---")
                appendLine()
                append(content)
                if (!content.endsWith('\n')) appendLine()
            }

        private fun String.yamlQuoted(): String {
            val escaped =
                replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace(LineBreaks, " ")
            return "\"$escaped\""
        }

        private fun String.toSafeFileName(): String =
            replace(UnsafeFileNameCharacters, "_")
                .trim()
                .trim('.')
                .take(MaxFileNameLength)
                .trim()

        private inline fun <reified T : Enum<T>> String?.toEnum(default: T): T =
            this?.let { value -> enumValues<T>().firstOrNull { it.name == value } } ?: default

        private class Snapshot(
            val notes: List<NoteEntity>,
            val folders: List<FolderEntity>,
            val categories: List<CategoryEntity>,
            val tags: List<TagEntity>,
            val noteTags: List<NoteTagMap>,
        )

        private companion object {
            const val MaxBackupBytes = 64L * 1024 * 1024
            const val MaxFileNameLength = 80
            val UnsafeFileNameCharacters = Regex("[\\\\/:*?\"<>|\\p{Cntrl}]")
            val LineBreaks = Regex("[\\r\\n]+")
        }
    }
