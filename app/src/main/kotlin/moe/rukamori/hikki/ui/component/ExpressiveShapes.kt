/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * GPL-3.0 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package moe.rukamori.hikki.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toPath
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon

@Composable
fun ShapedIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    polygon: RoundedPolygon = MaterialShapes.Cookie9Sided,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    size: Dp = 48.dp,
    iconSize: Dp = 24.dp,
) {
    val shape = polygon.toShape()
    Box(
        modifier =
            modifier
                .size(size)
                .clip(shape)
                .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
fun ExpressiveArt(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    polygon: RoundedPolygon = MaterialShapes.SoftBurst,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    size: Dp = 128.dp,
) {
    val motion = MaterialTheme.motionScheme
    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entrance.animateTo(1f, motion.slowSpatialSpec())
    }
    val shape = polygon.toShape()
    Box(
        modifier =
            modifier
                .size(size)
                .graphicsLayer {
                    val progress = entrance.value
                    scaleX = 0.6f + 0.4f * progress
                    scaleY = 0.6f + 0.4f * progress
                    rotationZ = -45f * (1f - progress)
                    alpha = progress.coerceIn(0f, 1f)
                },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(size)
                .clip(shape)
                .background(containerColor),
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(size * 0.38f),
        )
    }
}

class MorphShape(
    private val morph: Morph,
    private val progress: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = morph.toPath(progress = progress, path = Path())
        path.transform(Matrix().apply { scale(size.width, size.height) })
        return Outline.Generic(path)
    }
}
