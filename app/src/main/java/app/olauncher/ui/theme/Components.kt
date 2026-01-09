package app.olauncher.ui.theme

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// --- Atoms ---

@Composable
fun TCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = TLauncherShapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            content = { content() }
        )
    } else {
        Card(
            modifier = modifier,
            shape = TLauncherShapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            content = { content() }
        )
    }
}

@Composable
fun TSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = ColorTextPrimary,
            checkedTrackColor = ColorAccentSecondary,
            uncheckedThumbColor = ColorTextSecondary,
            uncheckedTrackColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun TChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text, style = MaterialTheme.typography.labelSmall) },
        leadingIcon = if (icon != null) {
            { Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp)) }
        } else null,
        modifier = modifier,
        shape = TLauncherShapes.extraSmall,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ColorAccentSecondary,
            selectedLabelColor = ColorTextPrimary,
            selectedLeadingIconColor = ColorTextPrimary,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = ColorTextSecondary,
            iconColor = ColorTextSecondary
        )
    )
}

@Composable
fun TScaffold(
    content: @Composable () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        content()
    }
}
