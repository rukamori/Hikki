package moe.rukamori.hikki.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import moe.rukamori.hikki.domain.model.AppSettings
import moe.rukamori.hikki.domain.model.DisplayMode
import moe.rukamori.hikki.domain.model.EditorMode
import moe.rukamori.hikki.domain.model.NoteSort
import moe.rukamori.hikki.domain.model.ThemeMode
import moe.rukamori.hikki.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

private val Context.hikkiDataStore by preferencesDataStore("hikki_settings")

@Singleton
class DataStoreSettingsRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : SettingsRepository {
        override val settings: Flow<AppSettings> =
            context.hikkiDataStore.data.map { preferences ->
                AppSettings(
                    themeMode = preferences[ThemeModeKey].toEnum(ThemeMode.System),
                    dynamicColor = preferences[DynamicColorKey] ?: true,
                    pureBlack = preferences[PureBlackKey] ?: false,
                    displayMode = preferences[DisplayModeKey].toEnum(DisplayMode.List),
                    sort = preferences[SortKey].toEnum(NoteSort.Updated),
                    defaultEditorMode = preferences[EditorModeKey].toEnum(EditorMode.Edit),
                    autosaveDelayMillis = preferences[AutosaveDelayKey]?.coerceIn(250L, 5_000L) ?: 800L,
                )
            }

        override suspend fun setThemeMode(value: ThemeMode) {
            context.hikkiDataStore.edit { it[ThemeModeKey] = value.name }
        }

        override suspend fun setDynamicColor(value: Boolean) {
            context.hikkiDataStore.edit { it[DynamicColorKey] = value }
        }

        override suspend fun setPureBlack(value: Boolean) {
            context.hikkiDataStore.edit { it[PureBlackKey] = value }
        }

        override suspend fun setDisplayMode(value: DisplayMode) {
            context.hikkiDataStore.edit { it[DisplayModeKey] = value.name }
        }

        override suspend fun setSort(value: NoteSort) {
            context.hikkiDataStore.edit { it[SortKey] = value.name }
        }

        override suspend fun setDefaultEditorMode(value: EditorMode) {
            context.hikkiDataStore.edit { it[EditorModeKey] = value.name }
        }

        override suspend fun setAutosaveDelayMillis(value: Long) {
            context.hikkiDataStore.edit { it[AutosaveDelayKey] = value.coerceIn(250L, 5_000L) }
        }

        private inline fun <reified T : Enum<T>> String?.toEnum(default: T): T =
            this?.let { value -> runCatching { enumValueOf<T>(value) }.getOrNull() } ?: default

        private companion object {
            val ThemeModeKey = stringPreferencesKey("theme_mode")
            val DynamicColorKey = booleanPreferencesKey("dynamic_color")
            val PureBlackKey = booleanPreferencesKey("pure_black")
            val DisplayModeKey = stringPreferencesKey("display_mode")
            val SortKey = stringPreferencesKey("sort")
            val EditorModeKey = stringPreferencesKey("editor_mode")
            val AutosaveDelayKey = longPreferencesKey("autosave_delay")
        }
    }
