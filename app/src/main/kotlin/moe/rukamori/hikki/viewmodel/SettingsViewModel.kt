package moe.rukamori.hikki.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.DisplayMode
import moe.rukamori.hikki.domain.model.EditorMode
import moe.rukamori.hikki.domain.model.NoteSort
import moe.rukamori.hikki.domain.model.ScreenState
import moe.rukamori.hikki.domain.model.ThemeMode
import moe.rukamori.hikki.domain.usecase.SettingsUseCases
import moe.rukamori.hikki.ui.model.SettingsUiModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val settingsUseCases: SettingsUseCases,
    ) : ViewModel() {
        val state: StateFlow<ScreenState<SettingsUiModel>> =
            settingsUseCases.settings
                .map { ScreenState.Success(SettingsUiModel(it)) }
                .catch { emit(ScreenState.Error(R.string.error_settings_load)) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScreenState.Loading)

        fun setThemeMode(value: ThemeMode) {
            viewModelScope.launch { settingsUseCases.setThemeMode(value) }
        }

        fun setDynamicColor(value: Boolean) {
            viewModelScope.launch { settingsUseCases.setDynamicColor(value) }
        }

        fun setPureBlack(value: Boolean) {
            viewModelScope.launch { settingsUseCases.setPureBlack(value) }
        }

        fun setDisplayMode(value: DisplayMode) {
            viewModelScope.launch { settingsUseCases.setDisplayMode(value) }
        }

        fun setSort(value: NoteSort) {
            viewModelScope.launch { settingsUseCases.setSort(value) }
        }

        fun setDefaultEditorMode(value: EditorMode) {
            viewModelScope.launch { settingsUseCases.setDefaultEditorMode(value) }
        }

        fun setAutosaveDelayMillis(value: Long) {
            viewModelScope.launch { settingsUseCases.setAutosaveDelayMillis(value) }
        }
    }
