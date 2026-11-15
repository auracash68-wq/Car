package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RacingColorScheme = darkColorScheme(
    primary = RacingYellow,
    onPrimary = DarkText,
    secondary = ElectricBlue,
    onSecondary = TextWhite,
    tertiary = NeonBlue,
    background = NavyDeep,
    onBackground = TextWhite,
    surface = NavyPanel,
    onSurface = TextWhite,
    surfaceVariant = NavyCard,
    onSurfaceVariant = TextSilver,
    error = RacingRed,
    onError = TextWhite
)

@Composable
fun CarRacing2DTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RacingColorScheme,
        typography = Typography,
        content = content
    )
}
