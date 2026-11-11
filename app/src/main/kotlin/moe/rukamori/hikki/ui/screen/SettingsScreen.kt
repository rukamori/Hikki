package moe.rukamori.hikki.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moe.rukamori.hikki.BuildConfig
import moe.rukamori.hikki.R
import moe.rukamori.hikki.ui.component.SettingsGroup
import moe.rukamori.hikki.ui.component.SettingsNavigationRow
import moe.rukamori.hikki.ui.component.StateContent
import moe.rukamori.hikki.ui.theme.MdSpacing
import moe.rukamori.hikki.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onOpenAppearance: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenTrash: () -> Unit,
    onOpenArchive: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                subtitle = { Text(stringResource(R.string.version_value, BuildConfig.VERSION_NAME)) },
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
            emptyTitleRes = R.string.settings,
            emptyMessageRes = R.string.settings_empty_message,
        ) { model ->
            val settings = model.settings
            val appearanceSummary =
                stringResource(
                    R.string.meta_separator,
                    stringResource(settings.themeMode.labelRes),
                    stringResource(settings.displayMode.labelRes),
                )
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .widthIn(max = 760.dp),
                    contentPadding = PaddingValues(top = MdSpacing.xs, bottom = MdSpacing.xl),
                    verticalArrangement = Arrangement.spacedBy(MdSpacing.md),
                ) {
                    item(key = "personalize", contentType = "group") {
                        SettingsGroup(title = stringResource(R.string.appearance)) {
                            row { shapes ->
                                SettingsNavigationRow(
                                    shapes = shapes,
                                    icon = Icons.Outlined.Palette,
                                    title = stringResource(R.string.appearance),
                                    summary = appearanceSummary,
                                    onClick = onOpenAppearance,
                                    polygon = MaterialShapes.Flower,
                                )
                            }
                        }
                    }
                    item(key = "workspace", contentType = "group") {
                        SettingsGroup(title = stringResource(R.string.workspace)) {
                            row { shapes ->
                                SettingsNavigationRow(
                                    shapes = shapes,
                                    icon = Icons.Outlined.Archive,
                                    title = stringResource(R.string.archive),
                                    summary = stringResource(R.string.archive_summary),
                                    onClick = onOpenArchive,
                                    polygon = MaterialShapes.Clover4Leaf,
                                )
                            }
                            row { shapes ->
                                SettingsNavigationRow(
                                    shapes = shapes,
                                    icon = Icons.Outlined.Delete,
                                    title = stringResource(R.string.trash),
                                    summary = stringResource(R.string.trash_summary),
                                    onClick = onOpenTrash,
                                    polygon = MaterialShapes.Cookie6Sided,
                                )
                            }
                        }
                    }
                    item(key = "about", contentType = "group") {
                        SettingsGroup(title = stringResource(R.string.about)) {
                            row { shapes ->
                                SettingsNavigationRow(
                                    shapes = shapes,
                                    icon = Icons.Outlined.Info,
                                    title = stringResource(R.string.about),
                                    summary = stringResource(R.string.about_summary),
                                    onClick = onOpenAbout,
                                    polygon = MaterialShapes.Sunny,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
