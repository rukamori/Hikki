package moe.rukamori.hikki.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moe.rukamori.hikki.R
import moe.rukamori.hikki.ui.component.PreferenceGroup
import moe.rukamori.hikki.ui.component.PreferenceRow
import moe.rukamori.hikki.ui.component.StateContent
import moe.rukamori.hikki.ui.theme.MdSpacing
import moe.rukamori.hikki.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onOpenAppearance: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenTrash: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            LargeTopAppBar(title = { Text(stringResource(R.string.settings)) })
        },
    ) { innerPadding ->
        StateContent(
            state = state,
            modifier = Modifier.padding(innerPadding),
            emptyTitleRes = R.string.settings,
            emptyMessageRes = R.string.settings_empty_message,
        ) {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                contentPadding = PaddingValues(vertical = MdSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(MdSpacing.sm),
            ) {
                item(contentType = "settings_group") {
                    PreferenceGroup(title = stringResource(R.string.settings)) {
                        item {
                            PreferenceRow(
                                headlineContent = { Text(stringResource(R.string.appearance)) },
                                supportingContent = { Text(stringResource(R.string.appearance_summary)) },
                                leadingContent = { Icon(Icons.Outlined.ColorLens, contentDescription = null) },
                                trailingContent = { Icon(Icons.AutoMirrored.Outlined.NavigateNext, contentDescription = null) },
                                onClick = onOpenAppearance,
                            )
                        }
                        item {
                            PreferenceRow(
                                headlineContent = { Text(stringResource(R.string.trash)) },
                                supportingContent = { Text(stringResource(R.string.trash_summary)) },
                                leadingContent = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                                trailingContent = { Icon(Icons.AutoMirrored.Outlined.NavigateNext, contentDescription = null) },
                                onClick = onOpenTrash,
                            )
                        }
                        item {
                            PreferenceRow(
                                headlineContent = { Text(stringResource(R.string.about)) },
                                supportingContent = { Text(stringResource(R.string.about_summary)) },
                                leadingContent = { Icon(Icons.Outlined.Info, contentDescription = null) },
                                trailingContent = { Icon(Icons.AutoMirrored.Outlined.NavigateNext, contentDescription = null) },
                                onClick = onOpenAbout,
                            )
                        }
                    }
                }
                item(contentType = "about_group") {
                    PreferenceGroup(title = stringResource(R.string.about)) {
                        item {
                            PreferenceRow(
                                headlineContent = { Text(stringResource(R.string.version)) },
                                supportingContent = { Text(stringResource(R.string.version_name)) },
                                leadingContent = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                            )
                        }
                    }
                }
            }
        }
    }
}
