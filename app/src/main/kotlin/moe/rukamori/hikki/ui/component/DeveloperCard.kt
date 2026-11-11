package moe.rukamori.hikki.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import moe.rukamori.hikki.ui.theme.MdSpacing

@Composable
fun DeveloperCard(
    role: String,
    name: String,
    summary: String,
    avatarUrl: String,
    profileLabel: String,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(MdSpacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DeveloperAvatar(avatarUrl = avatarUrl, excited = pressed)
            Spacer(Modifier.size(MdSpacing.sm))
            Column(Modifier.weight(1f)) {
                Text(
                    text = role,
                    style = MaterialTheme.typography.labelLargeEmphasized,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.size(MdSpacing.md))
        FilledTonalButton(
            onClick = onOpenProfile,
            shapes = ButtonDefaults.shapes(),
            interactionSource = interactionSource,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = ButtonDefaults.MediumContainerHeight),
            contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight, hasStartIcon = true),
        ) {
            Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.iconSizeFor(ButtonDefaults.MediumContainerHeight)),
            )
            Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(ButtonDefaults.MediumContainerHeight)))
            Text(
                text = profileLabel,
                style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight),
            )
        }
    }
}

@Composable
private fun DeveloperAvatar(
    avatarUrl: String,
    excited: Boolean,
) {
    val motion = MaterialTheme.motionScheme
    val context = LocalPlatformContext.current
    val avatarRequest =
        remember(context, avatarUrl) {
            ImageRequest
                .Builder(context)
                .data(avatarUrl)
                .crossfade(true)
                .build()
        }
    val burstShape = remember { MaterialShapes.SoftBurst.toShape() }
    val entrance = remember { Animatable(0f) }
    val spin = remember { Animatable(0f) }

    LaunchedEffect(Unit) { entrance.animateTo(1f, motion.slowSpatialSpec()) }
    LaunchedEffect(excited) { spin.animateTo(if (excited) 1f else 0f, motion.defaultSpatialSpec()) }

    Box(
        modifier =
            Modifier
                .size(88.dp)
                .graphicsLayer {
                    val value = entrance.value
                    scaleX = 0.6f + 0.4f * value
                    scaleY = 0.6f + 0.4f * value
                    alpha = value.coerceIn(0f, 1f)
                },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = -90f * (1f - entrance.value) + 45f * spin.value
                    val pop = 1f + 0.08f * spin.value
                    scaleX = pop
                    scaleY = pop
                }
                .clip(burstShape)
                .background(MaterialTheme.colorScheme.tertiaryContainer),
        )
        Box(
            modifier =
                Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp),
            )
            AsyncImage(
                model = avatarRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
