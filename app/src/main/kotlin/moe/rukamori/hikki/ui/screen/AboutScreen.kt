/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * GPL-3.0 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.hikki.ui.screen

import android.content.ActivityNotFoundException
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import kotlinx.coroutines.delay
import moe.rukamori.hikki.BuildConfig
import moe.rukamori.hikki.R
import moe.rukamori.hikki.ui.component.AppIcon
import moe.rukamori.hikki.ui.component.DeveloperCard
import moe.rukamori.hikki.ui.component.MorphShape
import moe.rukamori.hikki.ui.component.SettingsGroup
import moe.rukamori.hikki.ui.component.SettingsIcon
import moe.rukamori.hikki.ui.component.SettingsInfoRow
import moe.rukamori.hikki.ui.component.showMessage
import moe.rukamori.hikki.ui.theme.MdSpacing

private const val GitHubBaseUrl = "https://github.com"
private const val GitHubAvatarSize = 256
private const val RevealStaggerMillis = 70L

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val uriHandler = LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val sourceUrl = stringResource(R.string.source_code_url)
    val githubUsername = stringResource(R.string.developer_github_username)
    val profileUrl = "$GitHubBaseUrl/$githubUsername"
    val avatarUrl = "$GitHubBaseUrl/$githubUsername.png?size=$GitHubAvatarSize"
    val linkError = stringResource(R.string.error_open_link)
    val openLink: (String) -> Unit = { url ->
        try {
            uriHandler.openUri(url)
        } catch (error: IllegalArgumentException) {
            scope.showMessage(snackbarHostState, linkError)
        } catch (error: ActivityNotFoundException) {
            scope.showMessage(snackbarHostState, linkError)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.about)) },
                subtitle = { Text(stringResource(R.string.about_summary)) },
                navigationIcon = {
                    IconButton(onClick = onBack, shapes = IconButtonDefaults.shapes()) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier =
                    Modifier
                        .widthIn(max = 760.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(top = MdSpacing.xs, bottom = MdSpacing.xl),
                verticalArrangement = Arrangement.spacedBy(MdSpacing.md),
            ) {
                Reveal(index = 0) {
                    IdentityHero(
                        onOpenSource = { openLink(sourceUrl) },
                        modifier = Modifier.padding(horizontal = MdSpacing.sm),
                    )
                }
                Reveal(index = 1) {
                    DeveloperCard(
                        role = stringResource(R.string.developer),
                        name = stringResource(R.string.developer_name),
                        summary = stringResource(R.string.developer_summary),
                        avatarUrl = avatarUrl,
                        profileLabel = stringResource(R.string.developer_profile),
                        onOpenProfile = { openLink(profileUrl) },
                        modifier = Modifier.padding(horizontal = MdSpacing.sm),
                    )
                }
                Reveal(index = 2) {
                    SettingsGroup(title = stringResource(R.string.project_details)) {
                        row { shapes ->
                            SettingsInfoRow(
                                shapes = shapes,
                                title = stringResource(R.string.version),
                                summary = BuildConfig.VERSION_NAME,
                                leading = { SettingsIcon(icon = Icons.Outlined.Info, polygon = MaterialShapes.Sunny) },
                            )
                        }
                        row { shapes ->
                            SettingsInfoRow(
                                shapes = shapes,
                                title = stringResource(R.string.source_code),
                                summary = sourceUrl,
                                leading = { SettingsIcon(icon = Icons.Outlined.Code, polygon = MaterialShapes.Clover4Leaf) },
                                trailing = { Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null) },
                                onClick = { openLink(sourceUrl) },
                            )
                        }
                        row { shapes ->
                            SettingsInfoRow(
                                shapes = shapes,
                                title = stringResource(R.string.license),
                                summary = stringResource(R.string.license_summary),
                                leading = { SettingsIcon(icon = Icons.Outlined.Gavel, polygon = MaterialShapes.Cookie6Sided) },
                            )
                        }
                        row { shapes ->
                            SettingsInfoRow(
                                shapes = shapes,
                                title = stringResource(R.string.markdown_renderer),
                                summary = stringResource(R.string.markdown_renderer_summary),
                                leading = { SettingsIcon(icon = Icons.Outlined.Description, polygon = MaterialShapes.Flower) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Reveal(
    index: Int,
    content: @Composable () -> Unit,
) {
    val motion = MaterialTheme.motionScheme
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * RevealStaggerMillis)
        progress.animateTo(1f, motion.slowSpatialSpec())
    }
    Box(
        Modifier.graphicsLayer {
            val value = progress.value
            alpha = value.coerceIn(0f, 1f)
            translationY = (1f - value) * 32.dp.toPx()
        },
    ) {
        content()
    }
}

@Composable
private fun IdentityHero(
    onOpenSource: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    val morph = remember { Morph(MaterialShapes.Circle, MaterialShapes.Cookie9Sided) }
    val logoProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { logoProgress.animateTo(1f, motion.slowSpatialSpec()) }
    val progress = logoProgress.value

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = MdSpacing.md, vertical = MdSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppIcon(
            shape = MorphShape(morph, progress.coerceIn(0f, 1f)),
            modifier =
                Modifier
                    .size(112.dp)
                    .graphicsLayer { rotationZ = -60f * (1f - progress) },
        )
        Spacer(Modifier.height(MdSpacing.sm))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMediumEmphasized,
        )
        Spacer(Modifier.height(MdSpacing.xxs))
        Text(
            text = stringResource(R.string.version_value, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier =
                Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = MdSpacing.sm, vertical = MdSpacing.xxs),
        )
        Spacer(Modifier.height(MdSpacing.sm))
        Text(
            text = stringResource(R.string.about_hikki_tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(MdSpacing.md))
        Button(
            onClick = onOpenSource,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier.heightIn(min = ButtonDefaults.MediumContainerHeight),
            contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight, hasStartIcon = true),
        ) {
            Icon(
                imageVector = Icons.Outlined.Code,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.iconSizeFor(ButtonDefaults.MediumContainerHeight)),
            )
            Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(ButtonDefaults.MediumContainerHeight)))
            Text(
                text = stringResource(R.string.source_code),
                style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight),
            )
        }
    }
}
