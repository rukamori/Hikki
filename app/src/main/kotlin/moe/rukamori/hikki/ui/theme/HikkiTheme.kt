/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * GPL-3.0 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.hikki.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import moe.rukamori.hikki.domain.model.ThemeMode

private val LightColors =
    lightColorScheme(
        primary = Color(0xFF586422),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFDCEA9A),
        onPrimaryContainer = Color(0xFF414B0B),
        inversePrimary = Color(0xFFC0CE81),
        secondary = Color(0xFF5C6146),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE1E5C4),
        onSecondaryContainer = Color(0xFF44492F),
        tertiary = Color(0xFF3A6658),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFBCECDA),
        onTertiaryContainer = Color(0xFF214E41),
        background = Color(0xFFFBFAEE),
        onBackground = Color(0xFF1B1C15),
        surface = Color(0xFFFBFAEE),
        onSurface = Color(0xFF1B1C15),
        surfaceVariant = Color(0xFFE3E4D3),
        onSurfaceVariant = Color(0xFF46483B),
        inverseSurface = Color(0xFF303129),
        inverseOnSurface = Color(0xFFF2F1E6),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF93000A),
        outline = Color(0xFF76786A),
        outlineVariant = Color(0xFFC6C8B7),
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFFFBFAEE),
        surfaceDim = Color(0xFFDBDBCF),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF5F4E8),
        surfaceContainer = Color(0xFFEFEEE3),
        surfaceContainerHigh = Color(0xFFEAE9DD),
        surfaceContainerHighest = Color(0xFFE4E3D8),
    )

private val DarkColors =
    darkColorScheme(
        primary = Color(0xFFC0CE81),
        onPrimary = Color(0xFF2C3400),
        primaryContainer = Color(0xFF414B0B),
        onPrimaryContainer = Color(0xFFDCEA9A),
        inversePrimary = Color(0xFF586422),
        secondary = Color(0xFFC5C9A9),
        onSecondary = Color(0xFF2E331B),
        secondaryContainer = Color(0xFF44492F),
        onSecondaryContainer = Color(0xFFE1E5C4),
        tertiary = Color(0xFFA1D0BE),
        onTertiary = Color(0xFF05372B),
        tertiaryContainer = Color(0xFF214E41),
        onTertiaryContainer = Color(0xFFBCECDA),
        background = Color(0xFF13140D),
        onBackground = Color(0xFFE4E3D8),
        surface = Color(0xFF13140D),
        onSurface = Color(0xFFE4E3D8),
        surfaceVariant = Color(0xFF46483B),
        onSurfaceVariant = Color(0xFFC6C8B7),
        inverseSurface = Color(0xFFE4E3D8),
        inverseOnSurface = Color(0xFF303129),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        outline = Color(0xFF909283),
        outlineVariant = Color(0xFF46483B),
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFF393A31),
        surfaceDim = Color(0xFF13140D),
        surfaceContainerLowest = Color(0xFF0E0F08),
        surfaceContainerLow = Color(0xFF1B1C15),
        surfaceContainer = Color(0xFF1F2019),
        surfaceContainerHigh = Color(0xFF2A2B23),
        surfaceContainerHighest = Color(0xFF35352D),
    )

val supportsDynamicColor: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

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
            dynamicColor && supportsDynamicColor -> {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColors
            else -> LightColors
        }
    val colorScheme =
        remember(baseScheme, pureBlack, darkTheme) {
            if (darkTheme && pureBlack) {
                baseScheme.copy(
                    surface = Color.Black,
                    background = Color.Black,
                    surfaceDim = Color.Black,
                    surfaceContainerLowest = Color.Black,
                )
            } else {
                baseScheme
            }
        }
    val shapes =
        remember {
            Shapes(
                extraSmall = RoundedCornerShape(8.dp),
                small = RoundedCornerShape(12.dp),
                medium = RoundedCornerShape(16.dp),
                large = RoundedCornerShape(24.dp),
                extraLarge = RoundedCornerShape(32.dp),
            )
        }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        shapes = shapes,
        content = content,
    )
}

val ColorScheme.isDark: Boolean
    get() = surface.luminance() < 0.5f

@Composable
@ReadOnlyComposable
fun tagAccentColor(colorSeed: Long): Color {
    val hue = (((colorSeed % 360L) + 360L) % 360L).toFloat()
    val dark = MaterialTheme.colorScheme.isDark
    return Color.hsl(
        hue = hue,
        saturation = if (dark) 0.5f else 0.45f,
        lightness = if (dark) 0.72f else 0.4f,
    )
}
