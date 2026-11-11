/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * GPL-3.0 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.hikki.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.BackupSummary
import moe.rukamori.hikki.domain.model.RestoreMode
import moe.rukamori.hikki.ui.theme.MdSpacing

@Composable
fun RestoreBackupDialog(
    summary: BackupSummary,
    onRestore: (RestoreMode) -> Unit,
    onDismiss: () -> Unit,
) {
    val motion = MaterialTheme.motionScheme
    var mode by rememberSaveable { mutableStateOf(RestoreMode.Merge) }
    val replacing = mode == RestoreMode.Replace
    val origin =
        if (summary.exportedAt > 0L) {
            val created = dateTime(summary.exportedAt)
            if (summary.appVersion.isNotBlank()) {
                stringResource(R.string.backup_origin_version, created, summary.appVersion)
            } else {
                stringResource(R.string.backup_origin, created)
            }
        } else {
            null
        }
    val contents =
        listOf(
            pluralStringResource(R.plurals.notes_count, summary.notes, summary.notes),
            pluralStringResource(R.plurals.folders_count, summary.folders, summary.folders),
            pluralStringResource(R.plurals.categories_count, summary.categories, summary.categories),
            pluralStringResource(R.plurals.tags_count, summary.tags, summary.tags),
        ).joinToString(separator = stringResource(R.string.list_separator))

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(imageVector = Icons.Outlined.Restore, contentDescription = null) },
        title = {
            Text(
                text = stringResource(R.string.restore_backup_title),
                textAlign = TextAlign.Center,
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(MdSpacing.sm),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(MdSpacing.xxs)) {
                    Text(
                        text = contents,
                        style = MaterialTheme.typography.titleSmallEmphasized,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (origin != null) {
                        Text(
                            text = origin,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                ) {
                    RestoreMode.entries.forEachIndexed { index, option ->
                        val selected = option == mode
                        SegmentedListItem(
                            selected = selected,
                            onClick = { mode = option },
                            shapes = ListItemDefaults.segmentedShapes(index = index, count = RestoreMode.entries.size),
                            modifier = Modifier.semantics { role = Role.RadioButton },
                            leadingContent = { RadioButton(selected = selected, onClick = null) },
                            supportingContent = { Text(stringResource(option.summaryRes)) },
                        ) {
                            Text(stringResource(option.titleRes))
                        }
                    }
                }
                AnimatedVisibility(
                    visible = replacing,
                    enter = expandVertically(motion.defaultSpatialSpec()) + fadeIn(motion.defaultEffectsSpec()),
                    exit = shrinkVertically(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
                ) {
                    Text(
                        text = stringResource(R.string.restore_replace_warning),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onRestore(mode) },
                shapes = ButtonDefaults.shapes(),
                colors =
                    if (replacing) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        )
                    } else {
                        ButtonDefaults.buttonColors()
                    },
            ) {
                Text(stringResource(if (replacing) R.string.restore_replace_confirm else R.string.restore))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

private val RestoreMode.titleRes: Int
    get() =
        when (this) {
            RestoreMode.Merge -> R.string.restore_mode_merge
            RestoreMode.Replace -> R.string.restore_mode_replace
        }

private val RestoreMode.summaryRes: Int
    get() =
        when (this) {
            RestoreMode.Merge -> R.string.restore_mode_merge_summary
            RestoreMode.Replace -> R.string.restore_mode_replace_summary
        }
