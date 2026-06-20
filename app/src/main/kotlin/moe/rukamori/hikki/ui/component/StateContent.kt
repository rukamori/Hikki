package moe.rukamori.hikki.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    emptyTitleRes: Int = R.string.empty_notes_title,
    emptyMessageRes: Int = R.string.empty_notes_message,
    onEmptyAction: (() -> Unit)? = null,
    emptyActionRes: Int = R.string.create_note,
    content: @Composable (T) -> Unit,
) {
    when (state) {
        ScreenState.Loading -> LoadingState(modifier)
        ScreenState.Empty ->
            EmptyState(
                titleRes = emptyTitleRes,
                messageRes = emptyMessageRes,
                actionRes = emptyActionRes,
                onAction = onEmptyAction,
                modifier = modifier,
            )
        is ScreenState.Error ->
            ErrorState(
                messageRes = state.messageRes,
                modifier = modifier,
            )
        is ScreenState.Success -> content(state.data)
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyState(
    @StringRes titleRes: Int,
    @StringRes messageRes: Int,
    modifier: Modifier = Modifier,
    @StringRes actionRes: Int = R.string.create_note,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(PaddingValues(MdSpacing.lg)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.Inbox,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(MdSpacing.sm))
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(messageRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (onAction != null) {
            Spacer(Modifier.height(MdSpacing.md))
            Button(onClick = onAction) {
                Text(stringResource(actionRes))
            }
        }
    }
}

@Composable
fun ErrorState(
    @StringRes messageRes: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(MdSpacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.height(MdSpacing.sm))
        Text(
            text = stringResource(messageRes),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}
