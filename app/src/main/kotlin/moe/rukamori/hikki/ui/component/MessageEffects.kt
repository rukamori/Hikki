package moe.rukamori.hikki.ui.component

import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ResultMessageEffect(
    snackbarHostState: SnackbarHostState,
    @StringRes messageRes: Int?,
    onShown: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentOnShown by rememberUpdatedState(onShown)
    LaunchedEffect(messageRes) {
        if (messageRes != null) {
            val text = context.getString(messageRes)
            currentOnShown()
            scope.showMessage(snackbarHostState, text)
        }
    }
}

fun CoroutineScope.showMessage(
    snackbarHostState: SnackbarHostState,
    text: String,
) {
    launch {
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(text)
    }
}
