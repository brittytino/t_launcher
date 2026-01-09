package app.olauncher.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.olauncher.MainViewModel
import app.olauncher.ui.theme.*

@Composable
fun FocusModeScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var isFocusActive by remember { mutableStateOf(false) }
    var selectedDuration by remember { mutableIntStateOf(25) }
    var remainingTime by remember { mutableLongStateOf(25 * 60 * 1000L) }

    // Mock Timer Logic
    LaunchedEffect(isFocusActive) {
        if (isFocusActive) {
            remainingTime = selectedDuration * 60 * 1000L
            val startTime = System.currentTimeMillis()
            while (remainingTime > 0 && isFocusActive) {
                val elapsed = System.currentTimeMillis() - startTime
                remainingTime = (selectedDuration * 60 * 1000L) - elapsed
                kotlinx.coroutines.delay(1000)
            }
            if (remainingTime <= 0) {
                isFocusActive = false
            }
        }
    }

    TScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = TLauncherTheme.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            Text("FOCUS MODE", style = TLauncherTypography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(TLauncherTheme.spacing.extraLarge))

            // Main Focus Card
            TCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f), // Square card
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isFocusActive) {
                        // Timer View
                        val minutes = (remainingTime / 1000) / 60
                        val seconds = (remainingTime / 1000) % 60
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            style = TLauncherTypography.headlineLarge.copy(fontSize = 80.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Stay in the zone", 
                            style = TLauncherTypography.bodyLarge, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        // Start View
                        Text(
                            text = "$selectedDuration",
                            style = TLauncherTypography.headlineLarge.copy(fontSize = 96.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text("MINUTES", style = TLauncherTypography.labelSmall)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            if (!isFocusActive) {
                // Duration Slider / Selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DurationChip(15, selectedDuration == 15) { selectedDuration = 15 }
                    DurationChip(25, selectedDuration == 25) { selectedDuration = 25 }
                    DurationChip(45, selectedDuration == 45) { selectedDuration = 45 }
                    DurationChip(60, selectedDuration == 60) { selectedDuration = 60 }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                TChip(
                    text = "START FOCUS",
                    onClick = { isFocusActive = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    selected = true // Use accent color
                )
            } else {
                TChip(
                    text = "GIVE UP",
                    onClick = { isFocusActive = false },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    selected = false // Secondary/Destructive potentially
                )
            }
        }
    }
}

@Composable
fun DurationChip(minutes: Int, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                androidx.compose.foundation.shape.CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$minutes",
            style = TLauncherTypography.titleMedium,
            color = if (selected) Color.Black else MaterialTheme.colorScheme.onSurface
        )
    }
}
