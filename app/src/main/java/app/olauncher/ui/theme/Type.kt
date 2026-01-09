package app.olauncher.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// We will stick to Default font for now, but style it strictly.
// If user provided a font, we would use it here.

val TLauncherTypography = Typography(
    // Large Section Titles (e.g. "Focus Mode")
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = (-1).sp,
        color = ColorTextPrimary
    ),
    // Standard Headers (e.g. "Digital Wellbeing")
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = (-0.5).sp,
        color = ColorTextPrimary
    ),
    // Card Titles (e.g. "Ignore Apps")
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        letterSpacing = 0.sp,
        color = ColorTextPrimary
    ),
    // Normal Body Text
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = ColorTextPrimary
    ),
    // Secondary Description Text
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = ColorTextSecondary
    ),
    // Small Helpers / Labels
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 1.sp,
        color = ColorTextTertiary
    )
)
