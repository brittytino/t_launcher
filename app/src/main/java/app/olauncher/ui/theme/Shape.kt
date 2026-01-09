package app.olauncher.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Soft, large rounded corners are a key part of the "Calm" aesthetic.

val TLauncherShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),  // Chips, small tags
    small = RoundedCornerShape(12.dp),      // Inner card elements
    medium = RoundedCornerShape(16.dp),     // Standard cards
    large = RoundedCornerShape(24.dp),      // Large containers, bottom sheets
    extraLarge = RoundedCornerShape(32.dp)  // Dialogs, specialized surfaces
)
