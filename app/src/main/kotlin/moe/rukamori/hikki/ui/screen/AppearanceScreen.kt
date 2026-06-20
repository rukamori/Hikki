package moe.rukamori.hikki.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.DisplayMode
import moe.rukamori.hikki.domain.model.EditorMode
import moe.rukamori.hikki.domain.model.NoteSort
import moe.rukamori.hikki.domain.model.ThemeMode
import moe.rukamori.hikki.ui.component.PreferenceGroup
import moe.rukamori.hikki.ui.component.PreferenceRow
import moe.rukamori.hikki.ui.component.StateContent
import moe.rukamori.hikki.ui.theme.MdSpacing
import moe.rukamori.hikki.viewmodel.SettingsViewModel

@Composable
fun AppearanceScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.appearance)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        StateContent(
            state = state,
            modifier = Modifier.padding(innerPadding),
            emptyTitleRes = R.string.appearance,
            emptyMessageRes = R.string.settings_empty_message,
        ) { model ->
            val settings = model.settings
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                contentPadding = PaddingValues(vertical = MdSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(MdSpacing.sm),
            ) {
                item(contentType = "theme_group") {
                    PreferenceGroup(title = stringResource(R.string.theme)) {
                        ThemeMode.entries.forEach { mode ->
                            item {
                                PreferenceRow(
                                    headlineContent = { Text(stringResource(mode.labelRes)) },
                                    trailingContent = {
                                        RadioButton(
                                            selected = settings.themeMode == mode,
                                            onClick = { viewModel.setThemeMode(mode) },
                                        )
                                    },
                                    onClick = { viewModel.setThemeMode(mode) },
                                )
                            }
                        }
                        item {
                            PreferenceRow(
                                headlineContent = { Text(stringResource(R.string.dynamic_color)) },
                                supportingContent = { Text(stringResource(R.string.dynamic_color_summary)) },
                                trailingContent = {
                                    Switch(
                                        checked = settings.dynamicColor,
                                        onCheckedChange = viewModel::setDynamicColor,
                                    )
                                },
                                onClick = { viewModel.setDynamicColor(!settings.dynamicColor) },
                            )
                        }
                        item {
                            PreferenceRow(
                                headlineContent = { Text(stringResource(R.string.pure_black)) },
                                supportingContent = { Text(stringResource(R.string.pure_black_summary)) },
                                trailingContent = {
                                    Switch(
                                        checked = settings.pureBlack,
                                        onCheckedChange = viewModel::setPureBlack,
                                    )
                                },
                                onClick = { viewModel.setPureBlack(!settings.pureBlack) },
                            )
                        }
                    }
                }
                item(contentType = "defaults_group") {
                    PreferenceGroup(title = stringResource(R.string.defaults)) {
                        DisplayMode.entries.forEach { displayMode ->
                            item {
                                PreferenceRow(
                                    headlineContent = { Text(stringResource(displayMode.labelRes)) },
                                    trailingContent = {
                                        RadioButton(
                                            selected = settings.displayMode == displayMode,
                                            onClick = { viewModel.setDisplayMode(displayMode) },
                                        )
                                    },
                                    onClick = { viewModel.setDisplayMode(displayMode) },
                                )
                            }
                        }
                        EditorMode.entries.forEach { editorMode ->
                            item {
                                PreferenceRow(
                                    headlineContent = { Text(stringResource(editorMode.labelRes)) },
                                    supportingContent = { Text(stringResource(R.string.default_editor_mode)) },
                                    trailingContent = {
                                        RadioButton(
                                            selected = settings.defaultEditorMode == editorMode,
                                            onClick = { viewModel.setDefaultEditorMode(editorMode) },
                                        )
                                    },
                                    onClick = { viewModel.setDefaultEditorMode(editorMode) },
                                )
                            }
                        }
                        item {
                            PreferenceRow(
                                headlineContent = { Text(stringResource(R.string.autosave_delay)) },
                                supportingContent = { Text(stringResource(R.string.autosave_delay_value, settings.autosaveDelayMillis)) },
                                content = {
                                    Slider(
                                        value = settings.autosaveDelayMillis.toFloat(),
                                        onValueChange = { viewModel.setAutosaveDelayMillis(it.toLong()) },
                                        valueRange = 250f..5_000f,
                                        steps = 18,
                                        modifier = Modifier.padding(horizontal = MdSpacing.xs),
                                    )
                                },
                            )
                        }
                    }
                }
                item(contentType = "sort_group") {
                    PreferenceGroup(title = stringResource(R.string.default_sort)) {
                        NoteSort.entries.forEach { sort ->
                            item {
                                PreferenceRow(
                                    headlineContent = { Text(stringResource(sort.labelRes)) },
                                    trailingContent = {
                                        RadioButton(
                                            selected = settings.sort == sort,
                                            onClick = { viewModel.setSort(sort) },
                                        )
                                    },
                                    onClick = { viewModel.setSort(sort) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
