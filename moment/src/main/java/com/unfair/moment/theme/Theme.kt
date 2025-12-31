package com.unfair.moment.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Dark theme colors - pure black and white with gray shades
private val DarkColorScheme = darkColorScheme(
    primary = Color.White,                 // White for primary actions and highlights
    onPrimary = Color.Black,               // Black text on white primary
    primaryContainer = Color(0xFF303030),  // Dark gray for containers
    onPrimaryContainer = Color.White,      // White text on dark gray containers

    secondary = Color(0xFF808080),         // Medium gray for secondary elements
    onSecondary = Color.White,             // White text on gray secondary
    secondaryContainer = Color(0xFF404040), // Darker gray for secondary containers
    onSecondaryContainer = Color.White,    // White text on secondary containers

    tertiary = Color(0xFFC0C0C0),          // Light gray for tertiary elements
    onTertiary = Color.Black,              // Black text on light gray

    background = Color.Black,              // Pure black background
    onBackground = Color.White,            // White text on black background

    surface = Color(0xFF1C1C1C),          // Very dark gray surface
    onSurface = Color.White,              // White text on dark surface
    surfaceVariant = Color(0xFF2C2C2C),   // Medium dark gray for elevated surfaces
    onSurfaceVariant = Color(0xFFD0D0D0), // Light gray text for secondary content

    outline = Color(0xFF505050),          // Medium gray outline for borders
    outlineVariant = Color(0xFF303030),   // Dark gray outline variant

    error = Color(0xFFB0B0B0),            // Light gray for errors (monochrome)
    onError = Color.Black,                // Black text on light gray error
    errorContainer = Color(0xFF404040),   // Dark gray error container
    onErrorContainer = Color.White,       // White text on error container

    inverseSurface = Color.White,         // White surface for inverse
    inverseOnSurface = Color.Black,       // Black text on white inverse surface
    inversePrimary = Color.Black,         // Black primary for light backgrounds
)

// Light theme colors - pure white and black with gray shades
private val LightColorScheme = lightColorScheme(
    primary = Color.Black,                 // Black for primary actions and highlights
    onPrimary = Color.White,               // White text on black primary
    primaryContainer = Color(0xFFF0F0F0),  // Light gray for containers
    onPrimaryContainer = Color.Black,      // Black text on light gray containers

    secondary = Color(0xFF808080),         // Medium gray for secondary elements
    onSecondary = Color.White,             // White text on gray secondary
    secondaryContainer = Color(0xFFE0E0E0), // Light gray for secondary containers
    onSecondaryContainer = Color.Black,    // Black text on light secondary containers

    tertiary = Color(0xFF404040),          // Dark gray for tertiary elements
    onTertiary = Color.White,              // White text on dark gray

    background = Color.White,              // Pure white background
    onBackground = Color.Black,            // Black text on white background

    surface = Color(0xFFFAFAFA),          // Very light gray surface
    onSurface = Color.Black,              // Black text on light surface
    surfaceVariant = Color(0xFFF5F5F5),   // Light gray for elevated surfaces
    onSurfaceVariant = Color(0xFF606060), // Dark gray text for secondary content

    outline = Color(0xFF808080),          // Medium gray outline for borders
    outlineVariant = Color(0xFFC0C0C0),   // Light gray outline variant

    error = Color(0xFF505050),            // Dark gray for errors (monochrome)
    onError = Color.White,                // White text on dark gray error
    errorContainer = Color(0xFFE0E0E0),   // Light gray error container
    onErrorContainer = Color.Black,       // Black text on error container

    inverseSurface = Color.Black,         // Black surface for inverse
    inverseOnSurface = Color.White,       // White text on black inverse surface
    inversePrimary = Color.White,         // White primary for dark backgrounds
)

@Composable
fun MomentTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}