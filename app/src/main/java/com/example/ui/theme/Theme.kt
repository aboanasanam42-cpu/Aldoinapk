package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ClinicBlueMedium,
    onPrimary = ClinicWhite,
    primaryContainer = ClinicNavyLight,
    onPrimaryContainer = ClinicBlueLight,
    secondary = ClinicAccentCyan,
    onSecondary = ClinicNavyDark,
    background = ClinicNavyDark,
    surface = ClinicNavy,
    onBackground = ClinicWhite,
    onSurface = ClinicWhite,
    surfaceVariant = ClinicNavyLight,
    onSurfaceVariant = ClinicBlueLight
)

private val LightColorScheme = lightColorScheme(
    primary = ClinicNavy,
    onPrimary = ClinicWhite,
    primaryContainer = ClinicBlueLight,
    onPrimaryContainer = ClinicNavyDark,
    secondary = ClinicBlue,
    onSecondary = ClinicWhite,
    secondaryContainer = ClinicBlueLight,
    onSecondaryContainer = ClinicNavy,
    tertiary = ClinicAccentCyan,
    onTertiary = ClinicWhite,
    background = ClinicBlueSurface,
    surface = ClinicWhite,
    onBackground = ClinicTextPrimary,
    onSurface = ClinicTextPrimary,
    surfaceVariant = ClinicBlueLight,
    onSurfaceVariant = ClinicTextSecondary,
    outline = ClinicCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep consistent clinic identity
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
