package moe.rukamori.hikki.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.ScreenState
import moe.rukamori.hikki.ui.component.CollectionDialog
import moe.rukamori.hikki.ui.component.EmptyState
import moe.rukamori.hikki.ui.component.ErrorState
import moe.rukamori.hikki.ui.component.LoadingState
import moe.rukamori.hikki.ui.theme.MdSpacing
import moe.rukamori.hikki.viewmodel.CollectionsEvent
import moe.rukamori.hikki.viewmodel.CollectionsViewModel

@Composable
fun FoldersScreen(
    onOpenTags: () -> Unit,
    viewModel: CollectionsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var dialog by remember { mutableStateOf<CollectionDialogKind?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CollectionsEvent.Message -> snackbarHostState.showSnackbar(context.getString(event.messageRes))
            }
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.folders)) },
                actions = {
                    TextButton(onClick = { dialog = CollectionDialogKind.Category }) {
                        Text(stringResource(R.string.create_category))
                    }
                    TextButton(onClick = onOpenTags) {
                        Text(stringResource(R.string.tags))
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { dialog = CollectionDialogKind.Folder }) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.create_folder),
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when (val current = state) {
            ScreenState.Loading -> LoadingState(Modifier.padding(innerPadding))
            ScreenState.Empty ->
                EmptyState(
                    titleRes = R.string.collections_empty_title,
                    messageRes = R.string.collections_empty_message,
                    actionRes = R.string.create_folder,
                    onAction = { dialog = CollectionDialogKind.Folder },
                    modifier = Modifier.padding(innerPadding),
                )
            is ScreenState.Error -> ErrorState(current.messageRes, Modifier.padding(innerPadding))
            is ScreenState.Success ->
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    contentPadding = PaddingValues(MdSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(MdSpacing.xs),
                ) {
                    item(contentType = "folders_header") {
                        Text(
                            text = stringResource(R.string.folders),
                        )
                    }
                    items(current.data.folders, key = { "folder_${it.id}" }, contentType = { "folder" }) { folder ->
                        ListItem(
                            headlineContent = { Text(folder.name) },
                            leadingContent = { Icon(Icons.Outlined.Folder, contentDescription = null) },
                            trailingContent = {
                                IconButton(onClick = { viewModel.deleteFolder(folder.id) }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = stringResource(R.string.delete_folder),
                                    )
                                }
                            },
                        )
                    }
                    item(contentType = "categories_header") {
                        Text(text = stringResource(R.string.categories))
                    }
                    items(current.data.categories, key = { "category_${it.id}" }, contentType = { "category" }) { category ->
                        ListItem(
                            headlineContent = { Text(category.name) },
                            leadingContent = { Icon(Icons.Outlined.Category, contentDescription = null) },
                            trailingContent = {
                                IconButton(onClick = { viewModel.deleteCategory(category.id) }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = stringResource(R.string.delete_category),
                                    )
                                }
                            },
                        )
                    }
                    item(contentType = "tags_link") {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.manage_tags)) },
                            leadingContent = { Icon(Icons.Outlined.Label, contentDescription = null) },
                            trailingContent = { Icon(Icons.Outlined.NavigateNext, contentDescription = null) },
                            modifier = Modifier.clickableListItem(onOpenTags),
                        )
                    }
                }
        }
    }

    dialog?.let { kind ->
        CollectionDialog(
            titleRes = kind.titleRes,
            labelRes = kind.labelRes,
            onDismiss = { dialog = null },
            onConfirm = { name ->
                when (kind) {
                    CollectionDialogKind.Folder -> viewModel.createFolder(name)
                    CollectionDialogKind.Category -> viewModel.createCategory(name)
                    CollectionDialogKind.Tag -> viewModel.createTag(name)
                }
            },
        )
    }
}

private enum class CollectionDialogKind(
    val titleRes: Int,
    val labelRes: Int,
) {
    Folder(R.string.create_folder, R.string.folder_name),
    Category(R.string.create_category, R.string.category_name),
    Tag(R.string.create_tag, R.string.tag_name),
}
