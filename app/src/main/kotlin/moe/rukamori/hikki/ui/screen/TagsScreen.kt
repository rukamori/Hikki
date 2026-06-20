package moe.rukamori.hikki.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moe.rukamori.hikki.R
import moe.rukamori.hikki.ui.component.CollectionDialog
import moe.rukamori.hikki.ui.component.StateContent
import moe.rukamori.hikki.ui.theme.MdSpacing
import moe.rukamori.hikki.viewmodel.CollectionsViewModel

@Composable
fun TagsScreen(
    onBack: () -> Unit,
    viewModel: CollectionsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.tags)) },
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
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.create_tag),
                )
            }
        },
    ) { innerPadding ->
        StateContent(
            state = state,
            modifier = Modifier.fillMaxSize(),
            emptyTitleRes = R.string.tags_empty_title,
            emptyMessageRes = R.string.tags_empty_message,
            emptyActionRes = R.string.create_tag,
            onEmptyAction = { showCreate = true },
        ) { model ->
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                contentPadding = PaddingValues(MdSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(MdSpacing.xs),
            ) {
                items(model.tags, key = { it.id }, contentType = { "tag" }) { tag ->
                    ListItem(
                        headlineContent = { Text(tag.name) },
                        leadingContent = { Icon(Icons.AutoMirrored.Outlined.Label, contentDescription = null) },
                        trailingContent = {
                            IconButton(onClick = { viewModel.deleteTag(tag.id) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = stringResource(R.string.delete_tag),
                                )
                            }
                        },
                    )
                }
            }
        }
    }

    if (showCreate) {
        CollectionDialog(
            titleRes = R.string.create_tag,
            labelRes = R.string.tag_name,
            onDismiss = { showCreate = false },
            onConfirm = viewModel::createTag,
        )
    }
}
