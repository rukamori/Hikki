/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * GPL-3.0 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package moe.rukamori.hikki.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import moe.rukamori.hikki.ui.theme.MdSpacing

class SettingsGroupScope internal constructor() {
    internal val rows = mutableListOf<@Composable (ListItemShapes) -> Unit>()

    fun row(
        visible: Boolean = true,
        content: @Composable (shapes: ListItemShapes) -> Unit,
    ) {
        if (visible) rows += content
    }
}

@Composable
fun SettingsGroup(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: SettingsGroupScope.() -> Unit,
) {
    val scope = SettingsGroupScope().apply(content)
    if (scope.rows.isEmpty()) return
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = MdSpacing.sm),
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmallEmphasized,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = MdSpacing.xs, top = MdSpacing.xs, bottom = MdSpacing.xs),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
            scope.rows.forEachIndexed { index, row ->
                row(ListItemDefaults.segmentedShapes(index = index, count = scope.rows.size))
            }
        }
    }
}

@Composable
fun SettingsIcon(
    icon: ImageVector,
    polygon: RoundedPolygon = MaterialShapes.Cookie9Sided,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    ShapedIcon(
        icon = icon,
        polygon = polygon,
        containerColor = containerColor,
        contentColor = contentColor,
        size = 44.dp,
        iconSize = 22.dp,
    )
}

@Composable
fun SettingsNavigationRow(
    shapes: ListItemShapes,
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    polygon: RoundedPolygon = MaterialShapes.Cookie9Sided,
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = shapes,
        modifier = modifier,
        leadingContent = { SettingsIcon(icon = icon, polygon = polygon) },
        supportingContent = summary?.let { text -> { Text(text) } },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
            )
        },
    ) {
        Text(title)
    }
}

@Composable
fun SettingsSwitchRow(
    shapes: ListItemShapes,
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true,
    polygon: RoundedPolygon = MaterialShapes.Cookie9Sided,
) {
    SegmentedListItem(
        checked = checked,
        onCheckedChange = onCheckedChange,
        shapes = shapes,
        modifier = modifier,
        enabled = enabled,
        leadingContent = { SettingsIcon(icon = icon, polygon = polygon) },
        supportingContent = summary?.let { text -> { Text(text) } },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
            )
        },
    ) {
        Text(title)
    }
}

@Composable
fun SettingsInfoRow(
    shapes: ListItemShapes,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val supporting: (@Composable () -> Unit)? = summary?.let { text -> { Text(text) } }
    if (onClick != null) {
        SegmentedListItem(
            onClick = onClick,
            shapes = shapes,
            modifier = modifier,
            leadingContent = leading,
            supportingContent = supporting,
            trailingContent = trailing,
        ) {
            Text(title)
        }
    } else {
        SegmentedListItem(
            shapes = shapes,
            modifier = modifier,
            leadingContent = leading,
            supportingContent = supporting,
            trailingContent = trailing,
        ) {
            Text(title)
        }
    }
}

@Composable
fun SettingsControlRow(
    shapes: ListItemShapes,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    icon: ImageVector? = null,
    polygon: RoundedPolygon = MaterialShapes.Cookie9Sided,
    control: @Composable () -> Unit,
) {
    SegmentedListItem(
        shapes = shapes,
        modifier = modifier,
        leadingContent = icon?.let { vector -> { SettingsIcon(icon = vector, polygon = polygon) } },
        supportingContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(MdSpacing.sm),
                modifier = Modifier.padding(top = MdSpacing.xxs),
            ) {
                if (summary != null) Text(summary)
                control()
            }
        },
    ) {
        Text(title)
    }
}
