package moe.rukamori.hikki.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import moe.rukamori.hikki.ui.theme.MdSpacing

private enum class PreferenceGroupPosition {
    Single,
    First,
    Middle,
    Last,
}

private val LocalPreferenceGroupPosition = compositionLocalOf<PreferenceGroupPosition?> { null }
private val PreferenceGroupLargeCorner = 28.dp
private val PreferenceGroupSmallCorner = 6.dp

private fun segmentedPreferenceItemShape(
    index: Int,
    count: Int,
): Shape {
    val large = PreferenceGroupLargeCorner
    val small = PreferenceGroupSmallCorner
    return when {
        count <= 1 -> RoundedCornerShape(large)
        index == 0 ->
            RoundedCornerShape(
                topStart = large,
                topEnd = large,
                bottomEnd = small,
                bottomStart = small,
            )
        index == count - 1 ->
            RoundedCornerShape(
                topStart = small,
                topEnd = small,
                bottomEnd = large,
                bottomStart = large,
            )
        else -> RoundedCornerShape(small)
    }
}

private fun preferenceItemShapeForPosition(position: PreferenceGroupPosition?): Shape =
    when (position) {
        null,
        PreferenceGroupPosition.Single,
        -> segmentedPreferenceItemShape(index = 0, count = 1)
        PreferenceGroupPosition.First -> segmentedPreferenceItemShape(index = 0, count = 2)
        PreferenceGroupPosition.Middle -> segmentedPreferenceItemShape(index = 1, count = 3)
        PreferenceGroupPosition.Last -> segmentedPreferenceItemShape(index = 1, count = 2)
    }

class PreferenceGroupScope internal constructor() {
    internal val items = mutableListOf<@Composable () -> Unit>()

    fun item(
        visible: Boolean = true,
        content: @Composable () -> Unit,
    ) {
        if (visible) {
            items.add(content)
        }
    }
}

@Composable
fun PreferenceGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: PreferenceGroupScope.() -> Unit,
) {
    val scope = PreferenceGroupScope().apply(content)
    val itemCount = scope.items.size
    if (itemCount == 0) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = MdSpacing.sm, vertical = MdSpacing.xs),
        )
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MdSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            scope.items.forEachIndexed { index, itemContent ->
                val position =
                    when {
                        itemCount == 1 -> PreferenceGroupPosition.Single
                        index == 0 -> PreferenceGroupPosition.First
                        index == itemCount - 1 -> PreferenceGroupPosition.Last
                        else -> PreferenceGroupPosition.Middle
                    }
                CompositionLocalProvider(LocalPreferenceGroupPosition provides position) {
                    itemContent()
                }
            }
        }
    }
}

@Composable
fun PreferenceRow(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    supportingContent: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) {
    val position = LocalPreferenceGroupPosition.current
    val shape = remember(position) { preferenceItemShapeForPosition(position) }
    val clickModifier =
        if (onClick == null) {
            Modifier
        } else {
            Modifier.clickable(onClick = onClick)
        }

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .then(clickModifier),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column {
            ListItem(
                headlineContent = headlineContent,
                supportingContent = supportingContent,
                leadingContent = leadingContent,
                trailingContent = trailingContent,
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .padding(horizontal = MdSpacing.xs, vertical = 2.dp),
            )
            if (content != null) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = MdSpacing.sm, end = MdSpacing.sm, bottom = MdSpacing.xs),
                ) {
                    content()
                }
            }
        }
    }
}
