package com.tecsup.murocampus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColoresMuroCampus = lightColorScheme(
    primary = Color(0xFF005290),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9F3FF),
    onPrimaryContainer = Color(0xFF003B64),
    secondary = Color(0xFF01ACF1),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9EAF7),
    background = Color(0xFFF5F8FA),
    surface = Color.White,
    surfaceVariant = Color(0xFFEAF2F7),
    onSurfaceVariant = Color(0xFF455A64),
)

@Composable
fun MuroCampusTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ColoresMuroCampus, content = content)
}
