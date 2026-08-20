package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AnimeViolet,
    onPrimary = Color.White,
    primaryContainer = AnimeDarkCard,
    onPrimaryContainer = AnimeCyan,
    secondary = AnimeMagenta,
    onSecondary = Color.White,
    secondaryContainer = AnimeDarkCard,
    onSecondaryContainer = AnimeVioletLight,
    tertiary = AnimeCyan,
    onTertiary = Color.Black,
    background = AnimeDarkBg,
    onBackground = TextPrimaryDark,
    surface = AnimeDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = AnimeDarkCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = AnimeDarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = AnimeViolet,
    onPrimary = Color.White,
    primaryContainer = AnimeLightCard,
    onPrimaryContainer = AnimeViolet,
    secondary = AnimeMagenta,
    onSecondary = Color.White,
    secondaryContainer = AnimeLightCard,
    onSecondaryContainer = AnimeMagenta,
    tertiary = Color(0xFF007799),
    onTertiary = Color.White,
    background = AnimeLightBg,
    onBackground = TextPrimaryLight,
    surface = AnimeLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = AnimeLightCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = AnimeLightCardBorder
)

@Composable
fun AnimeKaiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // false to preserve custom cyber anime identity
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
