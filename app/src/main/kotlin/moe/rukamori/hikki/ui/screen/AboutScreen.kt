package moe.rukamori.hikki.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import moe.rukamori.hikki.R
import moe.rukamori.hikki.ui.component.PreferenceGroup
import moe.rukamori.hikki.ui.component.PreferenceRow
import moe.rukamori.hikki.ui.theme.MdSpacing

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.about)) },
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
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            contentPadding = PaddingValues(vertical = MdSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(MdSpacing.sm),
        ) {
            item(contentType = "about_group") {
                PreferenceGroup(title = stringResource(R.string.about)) {
                    item {
                        PreferenceRow(
                            headlineContent = { Text(stringResource(R.string.app_name)) },
                            supportingContent = { Text(stringResource(R.string.about_hikki_summary)) },
                            leadingContent = { Icon(Icons.AutoMirrored.Outlined.Notes, contentDescription = null) },
                            trailingContent = { AssistChip(onClick = {}, label = { Text(stringResource(R.string.version_name)) }) },
                        )
                    }
                    item {
                        PreferenceRow(
                            headlineContent = { Text(stringResource(R.string.markdown_renderer)) },
                            supportingContent = { Text(stringResource(R.string.markdown_renderer_summary)) },
                            leadingContent = { Icon(Icons.Outlined.Description, contentDescription = null) },
                        )
                    }
                }
            }
        }
    }
}
