package app.olauncher.ui.productivity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import app.olauncher.ui.theme.*

@Composable
fun AccountabilityDialog(
    onDismiss: () -> Unit,
    onSave: (diet: Int, sugar: Boolean, workout: Boolean, clarity: Int) -> Unit
) {
    var diet by remember { mutableStateOf(false) }
    var sugar by remember { mutableStateOf(false) }
    var workout by remember { mutableStateOf(false) }
    var productive by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        TCard(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("REPORT CARD", style = TLauncherTypography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(24.dp))

                YesNoRow("Did you eat clean?", diet) { diet = it }
                HorizontalDivider(color = MaterialTheme.colorScheme.surface, thickness = 1.dp)
                
                YesNoRow("Did you resist sugar?", sugar) { sugar = it }
                HorizontalDivider(color = MaterialTheme.colorScheme.surface, thickness = 1.dp)

                YesNoRow("Did you train today?", workout) { workout = it }
                HorizontalDivider(color = MaterialTheme.colorScheme.surface, thickness = 1.dp)

                YesNoRow("Were you productive?", productive) { productive = it }

                Spacer(modifier = Modifier.height(32.dp))

                TChip(
                    text = "SUBMIT LOG",
                    onClick = { 
                        onSave(
                            if (diet) 5 else 1,
                            sugar,
                            workout,
                            if (productive) 5 else 1
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    selected = true
                )
            }
        }
    }
}

@Composable
fun YesNoRow(question: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(question, style = TLauncherTypography.bodyLarge, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
        TSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
