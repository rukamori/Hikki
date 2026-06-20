package moe.rukamori.hikki.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import moe.rukamori.hikki.R
import moe.rukamori.hikki.domain.model.DisplayMode
import moe.rukamori.hikki.ui.component.StateContent
import moe.rukamori.hikki.ui.theme.MdSpacing
import moe.rukamori.hikki.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onOpenNote: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        StateContent(
            state = state,
            modifier = Modifier.padding(innerPadding),
            emptyTitleRes = R.string.search_empty_title,
            emptyMessageRes = R.string.search_empty_message,
        ) { model ->
            Column(Modifier.padding(innerPadding)) {
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = model.query,
                            onQueryChange = viewModel::updateQuery,
                            onSearch = viewModel::updateQuery,
                            expanded = false,
                            onExpandedChange = {},
                            placeholder = { Text(stringResource(R.string.search_notes)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = null,
                                )
                            },
                            trailingIcon = {
                                if (model.query.isNotBlank()) {
                                    IconButton(onClick = { viewModel.updateQuery("") }) {
                                        Icon(
                                            imageVector = Icons.Outlined.Clear,
                                            contentDescription = stringResource(R.string.clear_search),
                                        )
                                    }
                                }
                            },
                        )
                    },
                    expanded = false,
                    onExpandedChange = {},
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(MdSpacing.sm),
                ) {}
                NoteListContent(
                    notes = model.notes,
                    displayMode = DisplayMode.List,
                    onOpenNote = onOpenNote,
                    onPin = viewModel::togglePinned,
                    onFavorite = viewModel::toggleFavorite,
                    onDuplicate = viewModel::duplicate,
                    onArchive = viewModel::archive,
                    onTrash = viewModel::moveToTrash,
                )
            }
        }
    }
}
