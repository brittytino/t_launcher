package app.olauncher.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class TLauncherSpacing(
    val none: Dp = 0.dp,
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp, // Standard padding
    val large: Dp = 24.dp,  // Section separation
    val extraLarge: Dp = 32.dp,
    val sectionHeader: Dp = 48.dp // Space before headers
)

val LocalTLauncherSpacing = staticCompositionLocalOf { TLauncherSpacing() }
