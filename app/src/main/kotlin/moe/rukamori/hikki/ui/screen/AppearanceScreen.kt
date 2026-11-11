package moe.rukamori.hikki.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Preview
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.VerticalSplit
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material.icons.outlined.ViewModule
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.AppSettings
import moe.rukamori.hikki.domain.model.DisplayMode
import moe.rukamori.hikki.domain.model.EditorMode
import moe.rukamori.hikki.domain.model.NoteSort
import moe.rukamori.hikki.domain.model.ThemeMode
import moe.rukamori.hikki.ui.component.ConnectedChoiceGroup
import moe.rukamori.hikki.ui.component.SettingsControlRow
import moe.rukamori.hikki.ui.component.SettingsGroup
import moe.rukamori.hikki.ui.component.SettingsInfoRow
import moe.rukamori.hikki.ui.component.SettingsSwitchRow
import moe.rukamori.hikki.ui.component.ShapedIcon
import moe.rukamori.hikki.ui.component.StateContent
import moe.rukamori.hikki.ui.theme.MdSpacing
import moe.rukamori.hikki.ui.theme.supportsDynamicColor
import moe.rukamori.hikki.viewmodel.SettingsViewModel
import kotlin.math.roundToLong

private const val AutosaveMin = 250f
private const val AutosaveMax = 5_000f
private const val AutosaveSteps = 18

@Composable
fun AppearanceScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.appearance)) },
                subtitle = { Text(stringResource(R.string.appearance_summary)) },
                navigationIcon = {
                    IconButton(onClick = onBack, shapes = IconButtonDefaults.shapes()) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        StateContent(
            state = state,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            emptyTitleRes = R.string.appearance,
            emptyMessageRes = R.string.settings_empty_message,
        ) { model ->
            AppearanceContent(settings = model.settings, viewModel = viewModel)
        }
    }
}

@Composable
private fun AppearanceContent(
    settings: AppSettings,
    viewModel: SettingsViewModel,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp),
            contentPadding = PaddingValues(top = MdSpacing.xs, bottom = MdSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(MdSpacing.md),
        ) {
            item(key = "palette", contentType = "palette") {
                PalettePreview(modifier = Modifier.padding(horizontal = MdSpacing.sm))
            }
            item(key = "theme", contentType = "group") {
                SettingsGroup(title = stringResource(R.string.theme)) {
                    row { shapes ->
                        SettingsControlRow(
                            shapes = shapes,
                            title = stringResource(R.string.theme),
                            icon = Icons.Outlined.BrightnessAuto,
                            polygon = MaterialShapes.Sunny,
                        ) {
                            ConnectedChoiceGroup(
                                options = ThemeMode.entries,
                                selected = settings.themeMode,
                                onSelect = viewModel::setThemeMode,
                                label = { stringResource(it.labelRes) },
                                icon = { mode ->
                                    when (mode) {
                                        ThemeMode.System -> Icons.Outlined.BrightnessAuto
                                        ThemeMode.Light -> Icons.Outlined.WbSunny
                                        ThemeMode.Dark -> Icons.Outlined.Brightness4
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    row { shapes ->
                        SettingsSwitchRow(
                            shapes = shapes,
                            icon = Icons.Outlined.ColorLens,
                            title = stringResource(R.string.dynamic_color),
                            summary =
                                stringResource(
                                    if (supportsDynamicColor) R.string.dynamic_color_summary else R.string.dynamic_color_unavailable,
                                ),
                            checked = supportsDynamicColor && settings.dynamicColor,
                            onCheckedChange = viewModel::setDynamicColor,
                            enabled = supportsDynamicColor,
                            polygon = MaterialShapes.Flower,
                        )
                    }
                    row { shapes ->
                        SettingsSwitchRow(
                            shapes = shapes,
                            icon = Icons.Outlined.Brightness4,
                            title = stringResource(R.string.pure_black),
                            summary = stringResource(R.string.pure_black_summary),
                            checked = settings.pureBlack,
                            onCheckedChange = viewModel::setPureBlack,
                            enabled = settings.themeMode != ThemeMode.Light,
                            polygon = MaterialShapes.Cookie7Sided,
                        )
                    }
                }
            }
            item(key = "layout", contentType = "group") {
                SettingsGroup(title = stringResource(R.string.notes_layout)) {
                    row { shapes ->
                        SettingsControlRow(
                            shapes = shapes,
                            title = stringResource(R.string.notes_layout),
                            icon = Icons.Outlined.ViewModule,
                            polygon = MaterialShapes.Clover4Leaf,
                        ) {
                            ConnectedChoiceGroup(
                                options = DisplayMode.entries,
                                selected = settings.displayMode,
                                onSelect = viewModel::setDisplayMode,
                                label = { stringResource(it.labelRes) },
                                icon = { mode ->
                                    when (mode) {
                                        DisplayMode.List -> Icons.Outlined.ViewAgenda
                                        DisplayMode.Grid -> Icons.Outlined.ViewModule
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
            item(key = "sort", contentType = "group") {
                SortGroup(selected = settings.sort, onSelect = viewModel::setSort)
            }
            item(key = "editor", contentType = "group") {
                SettingsGroup(title = stringResource(R.string.editor_defaults)) {
                    row { shapes ->
                        SettingsControlRow(
                            shapes = shapes,
                            title = stringResource(R.string.default_editor_mode),
                            icon = Icons.Outlined.Edit,
                            polygon = MaterialShapes.Cookie9Sided,
                        ) {
                            ConnectedChoiceGroup(
                                options = EditorMode.entries,
                                selected = settings.defaultEditorMode,
                                onSelect = viewModel::setDefaultEditorMode,
                                label = { stringResource(it.labelRes) },
                                icon = { mode ->
                                    when (mode) {
                                        EditorMode.Edit -> Icons.Outlined.Edit
                                        EditorMode.Split -> Icons.Outlined.VerticalSplit
                                        EditorMode.Preview -> Icons.Outlined.Preview
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    row { shapes ->
                        AutosaveRow(
                            shapes = shapes,
                            delayMillis = settings.autosaveDelayMillis,
                            onCommit = viewModel::setAutosaveDelayMillis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SortGroup(
    selected: NoteSort,
    onSelect: (NoteSort) -> Unit,
) {
    SettingsGroup(
        title = stringResource(R.string.default_sort),
        modifier = Modifier.selectableGroup(),
    ) {
        NoteSort.entries.forEach { sort ->
            row { shapes ->
                val isSelected = sort == selected
                SettingsInfoRow(
                    shapes = shapes,
                    title = stringResource(sort.labelRes),
                    modifier =
                        Modifier.semantics {
                            role = Role.RadioButton
                            this.selected = isSelected
                        },
                    trailing = { RadioButton(selected = isSelected, onClick = null) },
                    onClick = { onSelect(sort) },
                )
            }
        }
    }
}

@Composable
private fun AutosaveRow(
    shapes: ListItemShapes,
    delayMillis: Long,
    onCommit: (Long) -> Unit,
) {
    val sliderState =
        remember(delayMillis) {
            SliderState(
                value = delayMillis.toFloat().coerceIn(AutosaveMin, AutosaveMax),
                steps = AutosaveSteps,
                trackRange = AutosaveMin..AutosaveMax,
            )
        }
    SettingsControlRow(
        shapes = shapes,
        title = stringResource(R.string.autosave_delay),
        summary =
            stringResource(
                R.string.meta_separator,
                stringResource(R.string.autosave_delay_value, sliderState.value.roundToLong()),
                stringResource(R.string.autosave_delay_summary),
            ),
        icon = Icons.Outlined.Timer,
        polygon = MaterialShapes.Cookie4Sided,
    ) {
        Slider(
            state = sliderState,
            onValueChange = { sliderState.value = it },
            onValueChangeFinished = {
                val committed = sliderState.value.roundToLong()
                if (committed != delayMillis) onCommit(committed)
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PalettePreview(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(colors.surfaceContainerHigh)
                .padding(MdSpacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ShapedIcon(
                icon = Icons.Outlined.Palette,
                polygon = MaterialShapes.SoftBurst,
                size = 56.dp,
                iconSize = 28.dp,
            )
            Spacer(Modifier.size(MdSpacing.sm))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmallEmphasized,
            )
        }
        Spacer(Modifier.height(MdSpacing.md))
        Row(horizontalArrangement = Arrangement.spacedBy(MdSpacing.xs)) {
            listOf(
                MaterialShapes.Cookie9Sided to colors.primary,
                MaterialShapes.Clover4Leaf to colors.secondary,
                MaterialShapes.Sunny to colors.tertiary,
                MaterialShapes.Cookie6Sided to colors.primaryContainer,
                MaterialShapes.Flower to colors.secondaryContainer,
                MaterialShapes.Pentagon to colors.tertiaryContainer,
            ).forEach { (polygon, color) ->
                Swatch(polygon = polygon, color = color, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Swatch(
    polygon: RoundedPolygon,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val animated by animateColorAsState(color, MaterialTheme.motionScheme.slowEffectsSpec(), label = "swatch")
    val shape = polygon.toShape()
    Box(
        modifier =
            modifier
                .widthIn(max = 56.dp)
                .aspectRatio(1f)
                .clip(shape)
                .background(animated),
    )
}
