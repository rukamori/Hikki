package moe.rukamori.hikki.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import moe.rukamori.hikki.R
import moe.rukamori.hikki.ui.component.PreferenceGroup
import moe.rukamori.hikki.ui.component.PreferenceRow
import moe.rukamori.hikki.ui.theme.MdSpacing

private const val DeveloperAvatarUrl = "https://avatars.githubusercontent.com/u/107134739?v=4"

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
            item(contentType = "identity") {
                AboutIdentityCard()
            }
            item(contentType = "developer") {
                PreferenceGroup(title = stringResource(R.string.developer)) {
                    item {
                        PreferenceRow(
                            headlineContent = { Text(stringResource(R.string.developer_name)) },
                            supportingContent = { Text(stringResource(R.string.developer_summary)) },
                            leadingContent = {
                                AsyncImage(
                                    model = DeveloperAvatarUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier =
                                        Modifier
                                            .size(56.dp)
                                            .clip(CircleShape),
                                )
                            },
                        )
                    }
                }
            }
            item(contentType = "details") {
                PreferenceGroup {
                    item {
                        PreferenceRow(
                            headlineContent = { Text(stringResource(R.string.version)) },
                            supportingContent = { Text(stringResource(R.string.version_name)) },
                            leadingContent = { Icon(Icons.Outlined.Info, contentDescription = null) },
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

@Composable
private fun AboutIdentityCard(modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = MdSpacing.sm),
        shape = RoundedCornerShape(32.dp),
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(MdSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(MdSpacing.sm),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MdSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Image(
                        painter = painterResource(R.mipmap.ic_launcher),
                        contentDescription = null,
                        modifier = Modifier.padding(MdSpacing.xs),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(MdSpacing.xxs),
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(R.string.about_hikki_tagline),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
