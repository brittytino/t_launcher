package app.olauncher.ui.overlay

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.olauncher.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import kotlinx.coroutines.delay

@Composable
fun DistractionOverlay(
    packageName: String,
    onContinue: () -> Unit,
    onCancel: () -> Unit
) {
    var timeLeft by remember { mutableIntStateOf(10) }
    var progress by remember { mutableFloatStateOf(1f) }
    
    // Animate progress circle
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "Progress"
    )

    LaunchedEffect(Unit) {
        // Start countdown
        while (timeLeft > 0) {
            progress = (timeLeft - 1) / 10f // Animate to next step
            delay(1000)
            timeLeft--
        }
    }

    // Full screen overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            
            Text(
                "Mindful Pause",
                style = TLauncherTypography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Is opening this app intentional?",
                style = TLauncherTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            // Circular Countdown
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                val primaryColor = MaterialTheme.colorScheme.primary
                Canvas(modifier = Modifier.size(200.dp)) {
                    // Track
                    drawArc(
                        color = Color.DarkGray,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Progress
                    drawArc(
                        color = if (timeLeft > 0) primaryColor else Color.Green,
                        startAngle = -90f,
                        sweepAngle = 360f * (timeLeft / 10f), // Immediate float for accuracy or animated
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                
                if (timeLeft > 0) {
                    Text(
                        text = "$timeLeft",
                        style = TLauncherTypography.headlineLarge.copy(fontSize = 64.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.Green,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            if (timeLeft > 0) {
                TChip(
                    text = "I'll do something else",
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    selected = false
                )
            } else {
                TChip(
                    text = "Continue to App",
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    selected = true
                )
            }
        }
    }
}
