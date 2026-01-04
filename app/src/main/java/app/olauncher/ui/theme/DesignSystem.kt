package app.olauncher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Colors ---
// Muted, calm, authoritative palette. Avoid saturation.
// --- Colors ---
// Premium, Rich, "Wow" Palette
val ColorBlack = Color(0xFF000000)
val ColorWhite = Color(0xFFFFFFFF)
val ColorDarkBackground = Color(0xFF050505) // Deepest Black
val ColorSurface = Color(0xFF0F1115) // Rich Gunmetal
val ColorSurfaceVariant = Color(0xFF1A1D24) // Lighter Gunmetal
val ColorPrimary = Color(0xFF00C853) // Android Green / Neon Accent
val ColorSecondary = Color(0xFF009688) // Teal Accent
val ColorError = Color(0xFFFF3D00) // Vibrant Red
val ColorTextPrimary = Color(0xFFEEEEEE)
val ColorTextSecondary = Color(0xFFB0BEC5)

// --- Theme ---
private val DarkColorScheme = darkColorScheme(
    primary = ColorPrimary,
    onPrimary = ColorBlack,
    secondary = ColorSecondary,
    onSecondary = ColorBlack,
    background = ColorDarkBackground,
    onBackground = ColorTextPrimary,
    surface = ColorSurface,
    onSurface = ColorTextPrimary,
    surfaceVariant = ColorSurfaceVariant,
    onSurfaceVariant = ColorTextSecondary,
    error = ColorError
)

// Force Dark Theme mostly, but provide Light just in case (mapped to premium dark anyway for "Wow" factor? No, let's respect system but make Light premium too)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32), // Darker Green for light mode
    onPrimary = ColorWhite,
    secondary = Color(0xFF00695C),
    onSecondary = ColorWhite,
    background = Color(0xFFF5F5F5),
    onBackground = ColorBlack,
    surface = ColorWhite,
    onSurface = ColorBlack,
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF616161),
    error = Color(0xFFD32F2F)
)

// --- Spacing ---
@Immutable
data class TLauncherSpacing(
    val none: Dp = 0.dp,
    val xs: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val screenPadding: Dp = 16.dp
)

val LocalTLauncherSpacing = staticCompositionLocalOf { TLauncherSpacing() }

// --- Typography ---
val TLauncherTypography = androidx.compose.material3.Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle( 
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = ColorTextSecondary
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun TLauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val spacing = TLauncherSpacing()

    CompositionLocalProvider(
        LocalTLauncherSpacing provides spacing
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TLauncherTypography,
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
