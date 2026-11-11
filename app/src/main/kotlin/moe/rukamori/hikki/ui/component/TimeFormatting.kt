package moe.rukamori.hikki.ui.component

import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext

@Composable
@ReadOnlyComposable
fun relativeTime(timestamp: Long): String =
    DateUtils.getRelativeTimeSpanString(
        timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE,
    ).toString()

@Composable
@ReadOnlyComposable
fun clockTime(timestamp: Long): String =
    DateUtils.formatDateTime(
        LocalContext.current,
        timestamp,
        DateUtils.FORMAT_SHOW_TIME,
    )
