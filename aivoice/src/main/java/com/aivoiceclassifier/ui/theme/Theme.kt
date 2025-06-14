package com.aivoiceclassifier.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Modern color palette matching the UI mockups
private val Blue500 = Color(0xFF4285F4)
private val Blue600 = Color(0xFF1976D2)
private val Blue700 = Color(0xFF1565C0)
private val Blue50 = Color(0xFFE3F2FD)
private val Blue100 = Color(0xFFBBDEFB)

private val Grey50 = Color(0xFFFAFAFA)
private val Grey100 = Color(0xFFF5F5F5)
private val Grey200 = Color(0xFFEEEEEE)
private val Grey300 = Color(0xFFE0E0E0)
private val Grey400 = Color(0xFFBDBDBD)
private val Grey500 = Color(0xFF9E9E9E)
private val Grey600 = Color(0xFF757575)
private val Grey700 = Color(0xFF616161)
private val Grey800 = Color(0xFF424242)
private val Grey900 = Color(0xFF212121)

private val Red500 = Color(0xFFF44336)
private val Red50 = Color(0xFFFFEBEE)

private val DarkColorScheme = darkColorScheme(
    primary = Blue500,
    onPrimary = Color.White,
    primaryContainer = Blue700,
    onPrimaryContainer = Color.White,
    
    secondary = Grey600,
    onSecondary = Color.White,
    secondaryContainer = Grey700,
    onSecondaryContainer = Color.White,
    
    tertiary = Blue600,
    onTertiary = Color.White,
    tertiaryContainer = Blue700,
    onTertiaryContainer = Color.White,
    
    background = Grey900,
    onBackground = Color.White,
    
    surface = Grey800,
    onSurface = Color.White,
    surfaceVariant = Grey700,
    onSurfaceVariant = Grey300,
    
    outline = Grey600,
    outlineVariant = Grey700,
    
    error = Red500,
    onError = Color.White,
    errorContainer = Red500.copy(alpha = 0.2f),
    onErrorContainer = Red500,
    
    inverseSurface = Grey100,
    inverseOnSurface = Grey900,
    inversePrimary = Blue600
)

private val LightColorScheme = lightColorScheme(
    primary = Blue500,
    onPrimary = Color.White,
    primaryContainer = Blue50,
    onPrimaryContainer = Blue700,
    
    secondary = Grey600,
    onSecondary = Color.White,
    secondaryContainer = Grey100,
    onSecondaryContainer = Grey800,
    
    tertiary = Blue600,
    onTertiary = Color.White,
    tertiaryContainer = Blue100,
    onTertiaryContainer = Blue700,
    
    background = Color.White,
    onBackground = Grey900,
    
    surface = Color.White,
    onSurface = Grey900,
    surfaceVariant = Grey100,
    onSurfaceVariant = Grey600,
    
    outline = Grey400,
    outlineVariant = Grey300,
    
    error = Red500,
    onError = Color.White,
    errorContainer = Red50,
    onErrorContainer = Red500,
    
    inverseSurface = Grey800,
    inverseOnSurface = Color.White,
    inversePrimary = Blue500
)

@Composable
fun AiVoiceClassifierTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
} 