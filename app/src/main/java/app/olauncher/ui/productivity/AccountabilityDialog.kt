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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import app.olauncher.ui.theme.TLauncherTypography

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
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("REPORT CARD", style = TLauncherTypography.headlineMedium, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(24.dp))

                YesNoRow("Did you eat clean or garbage?", diet) { diet = it }
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                
                YesNoRow("Did you resist sugar?", sugar) { sugar = it }
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                YesNoRow("Did you train today?", workout) { workout = it }
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                YesNoRow("Were you useful today?", productive) { productive = it }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { 
                        // Map Boolean to Int (5=Yes, 1=No) for legacy schema compatibility
                        onSave(
                            if (diet) 5 else 1,
                            sugar,
                            workout,
                            if (productive) 5 else 1
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("SUBMIT LOG", style = TLauncherTypography.labelLarge)
                }
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
        Text(question, style = TLauncherTypography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
             colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
fun RatingBar(rating: Int, onRatingChanged: (Int) -> Unit) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        (1..5).forEach { index ->
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (index <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onRatingChanged(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = index.toString(),
                    color = if (index <= rating) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
