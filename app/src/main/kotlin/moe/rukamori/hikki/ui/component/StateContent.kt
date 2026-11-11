/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * Proprietary License
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package moe.rukamori.hikki.ui.component

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.ScreenState
import moe.rukamori.hikki.ui.theme.MdSpacing

@Composable
fun <T> StateContent(
    state: ScreenState<T>,
    modifier: Modifier = Modifier,
    @StringRes emptyTitleRes: Int = R.string.empty_notes_title,
    @StringRes emptyMessageRes: Int = R.string.empty_notes_message,
    emptyIcon: ImageVector = Icons.Outlined.Create,
    onEmptyAction: (() -> Unit)? = null,
    @StringRes emptyActionRes: Int = R.string.create_note,
    content: @Composable (T) -> Unit,
) {
    val motion = MaterialTheme.motionScheme
    AnimatedContent(
        targetState = state,
        contentKey = { it::class },
        transitionSpec = {
            fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec())
        },
        modifier = modifier,
        label = "screen_state",
    ) { current ->
        when (current) {
            ScreenState.Loading -> LoadingState()
            ScreenState.Empty ->
                EmptyState(
                    titleRes = emptyTitleRes,
                    messageRes = emptyMessageRes,
                    icon = emptyIcon,
                    actionRes = emptyActionRes,
                    onAction = onEmptyAction,
                )
            is ScreenState.Error -> ErrorState(messageRes = current.messageRes)
            is ScreenState.Success -> content(current.data)
        }
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        LoadingIndicator(Modifier.size(64.dp))
    }
}

@Composable
fun EmptyState(
    @StringRes titleRes: Int,
    @StringRes messageRes: Int,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Create,
    @StringRes actionRes: Int = R.string.create_note,
    actionIcon: ImageVector = Icons.Outlined.Add,
    onAction: (() -> Unit)? = null,
    message: String? = null,
) {
    StatusLayout(
        art = {
            ExpressiveArt(
                icon = icon,
                polygon = MaterialShapes.SoftBurst,
            )
        },
        title = stringResource(titleRes),
        message = message ?: stringResource(messageRes),
        modifier = modifier,
        action =
            onAction?.let { action ->
                {
                    Button(
                        onClick = action,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.heightIn(min = ButtonDefaults.MediumContainerHeight),
                        contentPadding =
                            ButtonDefaults.contentPaddingFor(
                                ButtonDefaults.MediumContainerHeight,
                                hasStartIcon = true,
                            ),
                    ) {
                        Icon(
                            imageVector = actionIcon,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.iconSizeFor(ButtonDefaults.MediumContainerHeight)),
                        )
                        Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(ButtonDefaults.MediumContainerHeight)))
                        Text(
                            text = stringResource(actionRes),
                            style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight),
                        )
                    }
                }
            },
    )
}

@Composable
fun ErrorState(
    @StringRes messageRes: Int,
    modifier: Modifier = Modifier,
) {
    StatusLayout(
        art = {
            ExpressiveArt(
                icon = Icons.Outlined.ErrorOutline,
                polygon = MaterialShapes.Cookie7Sided,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            )
        },
        title = stringResource(R.string.error_unknown),
        message = stringResource(messageRes),
        modifier = modifier,
    )
}

@Composable
private fun StatusLayout(
    art: @Composable () -> Unit,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MdSpacing.lg, vertical = MdSpacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            art()
            Spacer(Modifier.height(MdSpacing.md))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmallEmphasized,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(MdSpacing.xs))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (action != null) {
                Spacer(Modifier.height(MdSpacing.md))
                action()
            }
        }
    }
}
