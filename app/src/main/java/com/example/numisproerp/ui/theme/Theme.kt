package com.numisproerp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.numisproerp.data.settings.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = IOSBlueDark,
    onPrimary = DarkOnPrimary,
    primaryContainer = Color(0xFF0A2540),
    onPrimaryContainer = IOSBlueDark,
    secondary = IOSTeal,
    onSecondary = DarkOnPrimary,
    tertiary = IOSPurple,
    onTertiary = DarkOnPrimary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnBackground,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = IOSRedDark,
    onError = DarkOnPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = IOSBlue,
    onPrimary = LightOnPrimary,
    primaryContainer = IOSBlueContainer,
    onPrimaryContainer = Color(0xFF003C7A),
    secondary = IOSTeal,
    onSecondary = LightOnPrimary,
    tertiary = IOSPurple,
    onTertiary = LightOnPrimary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnBackground,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    error = IOSRed,
    onError = LightOnPrimary
)

private val OlegSmileColorScheme = darkColorScheme(
    primary = OlegGold,
    onPrimary = Color(0xFF0B0B0D),
    primaryContainer = OlegPrimaryContainer,
    onPrimaryContainer = OlegGoldBright,
    secondary = OlegGoldBright,
    onSecondary = Color(0xFF0B0B0D),
    tertiary = OlegOrange,
    onTertiary = Color(0xFF0B0B0D),
    background = OlegBackground,
    onBackground = OlegOnSurface,
    surface = OlegSurface,
    onSurface = OlegOnSurface,
    surfaceVariant = OlegSurfaceVariant,
    onSurfaceVariant = OlegOnSurfaceVariant,
    error = OlegRed,
    onError = Color(0xFF0B0B0D)
)

private val IOSShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun NumisProERPTheme(
    appTheme: AppTheme = AppTheme.DEFAULT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.OLEG_SMILE -> OlegSmileColorScheme
        AppTheme.DEFAULT -> if (darkTheme) DarkColorScheme else LightColorScheme
    }

    CompositionLocalProvider(LocalAppTheme provides appTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = IOSShapes,
            content = content
        )
    }
}
