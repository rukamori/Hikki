package moe.rukamori.hikki.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import moe.rukamori.hikki.domain.model.ThemeMode

private val HikkiSeed = Color(0xFF5D6B2F)

private val LightColors =
    lightColorScheme(
        primary = HikkiSeed,
        secondary = Color(0xFF56624A),
        tertiary = Color(0xFF386663),
        surface = Color(0xFFFFFBFE),
        background = Color(0xFFFFFBFE),
    )

private val DarkColors =
    darkColorScheme(
        primary = Color(0xFFC6D991),
        secondary = Color(0xFFBECBAD),
        tertiary = Color(0xFFA0CFCA),
        surface = Color(0xFF121410),
        background = Color(0xFF121410),
    )

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HikkiTheme(
    themeMode: ThemeMode = ThemeMode.System,
    dynamicColor: Boolean = true,
    pureBlack: Boolean = false,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme =
        when (themeMode) {
            ThemeMode.System -> systemDark
            ThemeMode.Light -> false
            ThemeMode.Dark -> true
        }
    val context = LocalContext.current
    val baseScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColors
            else -> LightColors
        }
    val colorScheme =
        remember(baseScheme, pureBlack, darkTheme) {
            if (darkTheme && pureBlack) {
                baseScheme.copy(surface = Color.Black, background = Color.Black)
            } else {
                baseScheme
            }
        }
    val shapes =
        remember {
            Shapes(
                extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
            )
        }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        shapes = shapes,
        content = content,
    )
}

val ColorScheme.noteCardContainer: Color
    get() = surfaceContainer
