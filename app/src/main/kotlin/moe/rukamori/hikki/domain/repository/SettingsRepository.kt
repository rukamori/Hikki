package moe.rukamori.hikki.domain.repository

import kotlinx.coroutines.flow.Flow
import moe.rukamori.hikki.domain.model.AppSettings
import moe.rukamori.hikki.domain.model.DisplayMode
import moe.rukamori.hikki.domain.model.EditorMode
import moe.rukamori.hikki.domain.model.NoteSort
import moe.rukamori.hikki.domain.model.ThemeMode

interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun setThemeMode(value: ThemeMode)

    suspend fun setDynamicColor(value: Boolean)

    suspend fun setPureBlack(value: Boolean)

    suspend fun setDisplayMode(value: DisplayMode)

    suspend fun setSort(value: NoteSort)

    suspend fun setDefaultEditorMode(value: EditorMode)

    suspend fun setAutosaveDelayMillis(value: Long)
}
