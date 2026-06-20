package moe.rukamori.hikki.domain.usecase

import kotlinx.coroutines.flow.Flow
import moe.rukamori.hikki.domain.model.AppSettings
import moe.rukamori.hikki.domain.model.DisplayMode
import moe.rukamori.hikki.domain.model.EditorMode
import moe.rukamori.hikki.domain.model.NoteSort
import moe.rukamori.hikki.domain.model.ThemeMode
import moe.rukamori.hikki.domain.repository.SettingsRepository
import javax.inject.Inject

class SettingsUseCases
    @Inject
    constructor(
        private val repository: SettingsRepository,
    ) {
        val settings: Flow<AppSettings> = repository.settings

        suspend fun setThemeMode(value: ThemeMode) = repository.setThemeMode(value)

        suspend fun setDynamicColor(value: Boolean) = repository.setDynamicColor(value)

        suspend fun setPureBlack(value: Boolean) = repository.setPureBlack(value)

        suspend fun setDisplayMode(value: DisplayMode) = repository.setDisplayMode(value)

        suspend fun setSort(value: NoteSort) = repository.setSort(value)

        suspend fun setDefaultEditorMode(value: EditorMode) = repository.setDefaultEditorMode(value)

        suspend fun setAutosaveDelayMillis(value: Long) = repository.setAutosaveDelayMillis(value)
    }
