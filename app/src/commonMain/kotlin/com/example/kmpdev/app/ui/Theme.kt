package com.example.kmpdev.app.ui

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

val BrandPrimary = Color(0xFF1E88E5)
val BrandSecondary = Color(0xFF43A047)
val BrandBackground = Color(0xFFF5F5F5)

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    secondary = BrandSecondary,
    background = BrandBackground
)

private val DarkColorScheme = darkColorScheme(primary = BrandPrimary, secondary = BrandSecondary)

@Composable
fun RTTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = RTTypography,
        content = {
            CompositionLocalProvider(LocalIndication provides NoRipple) {
                content()
            }
        }
    )
}
