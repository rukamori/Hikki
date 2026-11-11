/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * Proprietary License
 */

package moe.rukamori.hikki.ui.screen

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import moe.rukamori.hikki.R
import moe.rukamori.hikki.ui.component.CollectionDialog
import moe.rukamori.hikki.ui.component.ConfirmDestructiveDialog
import moe.rukamori.hikki.viewmodel.CollectionsViewModel

internal enum class CollectionKind(
    @param:StringRes val createTitleRes: Int,
    @param:StringRes val renameTitleRes: Int,
    @param:StringRes val labelRes: Int,
    @param:StringRes val deleteTitleRes: Int,
    @param:StringRes val deleteMessageRes: Int,
    val icon: ImageVector,
    val createIcon: ImageVector,
) {
    Folder(
        createTitleRes = R.string.create_folder,
        renameTitleRes = R.string.rename_folder,
        labelRes = R.string.folder_name,
        deleteTitleRes = R.string.delete_folder_title,
        deleteMessageRes = R.string.delete_folder_message,
        icon = Icons.Outlined.Folder,
        createIcon = Icons.Outlined.CreateNewFolder,
    ),
    Category(
        createTitleRes = R.string.create_category,
        renameTitleRes = R.string.rename_category,
        labelRes = R.string.category_name,
        deleteTitleRes = R.string.delete_category_title,
        deleteMessageRes = R.string.delete_category_message,
        icon = Icons.Outlined.Category,
        createIcon = Icons.Outlined.Category,
    ),
    Tag(
        createTitleRes = R.string.create_tag,
        renameTitleRes = R.string.rename_tag,
        labelRes = R.string.tag_name,
        deleteTitleRes = R.string.delete_tag_title,
        deleteMessageRes = R.string.delete_tag_message,
        icon = Icons.AutoMirrored.Outlined.Label,
        createIcon = Icons.AutoMirrored.Outlined.Label,
    ),
}

internal sealed interface CollectionRequest {
    val kind: CollectionKind

    data class Create(override val kind: CollectionKind) : CollectionRequest

    data class Rename(
        override val kind: CollectionKind,
        val id: Long,
        val name: String,
    ) : CollectionRequest

    data class Delete(
        override val kind: CollectionKind,
        val id: Long,
        val name: String,
    ) : CollectionRequest
}

@Composable
internal fun CollectionRequestDialog(
    request: CollectionRequest,
    viewModel: CollectionsViewModel,
    onDismiss: () -> Unit,
) {
    val kind = request.kind
    when (request) {
        is CollectionRequest.Create ->
            CollectionDialog(
                titleRes = kind.createTitleRes,
                labelRes = kind.labelRes,
                icon = kind.createIcon,
                onDismiss = onDismiss,
                onConfirm = { name ->
                    when (kind) {
                        CollectionKind.Folder -> viewModel.createFolder(name)
                        CollectionKind.Category -> viewModel.createCategory(name)
                        CollectionKind.Tag -> viewModel.createTag(name)
                    }
                },
            )
        is CollectionRequest.Rename ->
            CollectionDialog(
                titleRes = kind.renameTitleRes,
                labelRes = kind.labelRes,
                icon = Icons.Outlined.DriveFileRenameOutline,
                onDismiss = onDismiss,
                initialValue = request.name,
                confirmRes = R.string.rename,
                onConfirm = { name ->
                    when (kind) {
                        CollectionKind.Folder -> viewModel.renameFolder(request.id, name)
                        CollectionKind.Category -> viewModel.renameCategory(request.id, name)
                        CollectionKind.Tag -> viewModel.renameTag(request.id, name)
                    }
                },
            )
        is CollectionRequest.Delete ->
            ConfirmDestructiveDialog(
                icon = Icons.Outlined.Delete,
                title = stringResource(kind.deleteTitleRes),
                message = stringResource(kind.deleteMessageRes, request.name),
                confirmRes = R.string.delete,
                onConfirm = {
                    when (kind) {
                        CollectionKind.Folder -> viewModel.deleteFolder(request.id)
                        CollectionKind.Category -> viewModel.deleteCategory(request.id)
                        CollectionKind.Tag -> viewModel.deleteTag(request.id)
                    }
                },
                onDismiss = onDismiss,
            )
    }
}

@Composable
internal fun CollectionItemMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.rename)) },
            leadingIcon = { Icon(Icons.Outlined.DriveFileRenameOutline, contentDescription = null) },
            onClick = {
                onDismiss()
                onRename()
            },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) },
            leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            onClick = {
                onDismiss()
                onDelete()
            },
        )
    }
}
