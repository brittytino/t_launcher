package de.brittytino.android.launcher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import de.brittytino.android.launcher.preferences.LauncherPreferences
import de.brittytino.android.launcher.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.isSystemInDarkTheme
import de.brittytino.android.launcher.preferences.theme.ColorTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import android.app.Activity
import androidx.core.view.WindowCompat

@Composable
fun SettingsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val themePreference = LauncherPreferences.theme().colorTheme()
    val effectiveDarkTheme = when (themePreference) {
        ColorTheme.LIGHT -> false
        ColorTheme.DARK -> true
        ColorTheme.DEFAULT -> true
        else -> darkTheme
    }

    val colorScheme = if (effectiveDarkTheme) {
        darkColorScheme(
            primary = Color(0xFFBB86FC),
            secondary = Color(0xFF03DAC6),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White,
            error = Color(0xFFCF6679)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC6),
            background = Color(0xFFF0F0F0),
            surface = Color(0xFFFFFFFF),
            onPrimary = Color.White,
            onSecondary = Color.Black,
            onBackground = Color.Black,
            onSurface = Color.Black,
            error = Color(0xFFB00020)
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                val insetsController = WindowCompat.getInsetsController(it, view)
                insetsController.isAppearanceLightStatusBars = !effectiveDarkTheme
                insetsController.isAppearanceLightNavigationBars = !effectiveDarkTheme
            }
        }
    }

    // Map selected launcher font to a Compose FontFamily so Compose UI reflects the chosen font
    val selected = LauncherPreferences.theme().font()
    val fontFamily = when (selected) {
        de.brittytino.android.launcher.preferences.theme.Font.INTER -> FontFamily(Font(R.font.inter))
        de.brittytino.android.launcher.preferences.theme.Font.MONTSERRAT -> FontFamily(Font(R.font.montserrat))
        de.brittytino.android.launcher.preferences.theme.Font.LATO -> FontFamily(Font(R.font.lato))
        de.brittytino.android.launcher.preferences.theme.Font.NOTO_SANS -> FontFamily(Font(R.font.noto_sans))
        else -> FontFamily.Default
    }

    // Build a typography that uses the selected family for main text styles
    val baseTypo = Typography()
    val typography = baseTypo.copy(
        titleLarge = baseTypo.titleLarge.copy(fontFamily = fontFamily),
        titleMedium = baseTypo.titleMedium.copy(fontFamily = fontFamily),
        bodyLarge = baseTypo.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = baseTypo.bodyMedium.copy(fontFamily = fontFamily),
        labelLarge = baseTypo.labelLarge.copy(fontFamily = fontFamily)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

@Composable
fun SettingsScaffold(content: @Composable ColumnScope.() -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(0.dp), // Items handle their own padding usually, or add here
            content = content
        )
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Color.Gray)
            Spacer(Modifier.width(16.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = textColor)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
        if (action != null) {
            action()
        } else if (onClick != null) {
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun SettingsToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange
    )
}

@Composable
fun IconArrow() {
    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
}
