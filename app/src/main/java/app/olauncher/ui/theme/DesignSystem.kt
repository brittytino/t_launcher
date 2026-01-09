package app.olauncher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

// --- Theme ---
private val DarkColorScheme = darkColorScheme(
    primary = ColorAccentSecondary, // Use the proper accent
    onPrimary = ColorTextPrimary,
    secondary = ColorAccentTertiary,
    onSecondary = ColorDeepBackground,
    background = ColorDeepBackground,
    onBackground = ColorTextPrimary,
    surface = ColorSurface,
    onSurface = ColorTextPrimary,
    surfaceVariant = ColorSurfaceElevated,
    onSurfaceVariant = ColorTextSecondary,
    error = ColorError
)

// We STRICTLY enforce Dark Mode as per requirements.
// Even if system is light, we return Dark Scheme.
// (Requirement: "Dark theme only")

@Composable
fun TLauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Ignored, always dark
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val spacing = TLauncherSpacing()

    CompositionLocalProvider(
        LocalTLauncherSpacing provides spacing
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TLauncherTypography,
            shapes = TLauncherShapes,
            content = content
        )
    }
}

// Accessor for Spacing
object TLauncherTheme {
    val spacing: TLauncherSpacing
        @Composable
        get() = LocalTLauncherSpacing.current
}
