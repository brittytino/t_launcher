package app.olauncher.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.olauncher.MainViewModel
import app.olauncher.data.local.LogType
import app.olauncher.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun FocusTimer(viewModel: MainViewModel? = null) {
    // Deprecated in favor of FocusModeScreen, but kept for Productivity Panel embed
    var timeLeft by remember { mutableLongStateOf(25 * 60 * 1000L) }
    var initialTime by remember { mutableLongStateOf(25 * 60 * 1000L) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) { 
        if (isRunning) {
            val startTime = System.currentTimeMillis()
            val targetTime = startTime + timeLeft + 1000L
            while (isRunning && timeLeft > 0) {
                timeLeft = targetTime - System.currentTimeMillis()
                delay(100) 
                if (timeLeft <= 0) {
                    timeLeft = 0
                    isRunning = false
                    viewModel?.logSystemEvent(LogType.FOCUS_SESSION, "Completed 25m focus")
                }
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
         Text("DEEP WORK", style = TLauncherTypography.labelSmall, color = MaterialTheme.colorScheme.primary, letterSpacing = 3.sp)
         Spacer(modifier = Modifier.height(16.dp))
        
         Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
             // Background Track
             val trackColor = MaterialTheme.colorScheme.surfaceVariant
             Canvas(modifier = Modifier.fillMaxSize()) {
                 drawCircle(
                     color = trackColor,
                     radius = size.minDimension / 2,
                     style = Stroke(width = 16f)
                 )
             }
             
             // Progress Arc
             val progress = timeLeft.toFloat() / initialTime.toFloat()
             CircularProgressIndicator(
                 progress = { progress },
                 modifier = Modifier.fillMaxSize(),
                 color = MaterialTheme.colorScheme.primary,
                 strokeWidth = 6.dp,
                 trackColor = Color.Transparent,
                 strokeCap = StrokeCap.Round
             )
             
             Column(horizontalAlignment = Alignment.CenterHorizontally) {
                 Text(
                     text = formatTime(timeLeft),
                     style = TLauncherTypography.headlineLarge.copy(fontSize = 40.sp, fontWeight = FontWeight.Bold),
                     color = MaterialTheme.colorScheme.onSurface
                 )
                 Text(
                     text = if (isRunning) "FOCUSING" else "READY",
                     style = TLauncherTypography.labelSmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant
                 )
             }
         }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Replaced Buttons with TChips
             TChip(
                 text = if (isRunning) "PAUSE" else "START",
                 onClick = { isRunning = !isRunning },
                 modifier = Modifier.height(48.dp).weight(1f),
                 selected = !isRunning, // Primary when not running to encourage start
                 icon = Icons.Default.PlayArrow
             )
             
             TChip(
                 text = "RESET",
                 onClick = { 
                    isRunning = false
                    timeLeft = 25 * 60 * 1000L 
                 },
                 modifier = Modifier.height(48.dp).weight(1f),
                 selected = false,
                 icon = Icons.Default.Refresh
             )
        }
    }
}

@Composable
fun BreathingExercise(viewModel: MainViewModel? = null) {
    var phase by remember { mutableStateOf("Ready") }
    var isActive by remember { mutableStateOf(false) }
    var startTime by remember { mutableLongStateOf(0L) }
    
    val sizeScale by animateFloatAsState(
        targetValue = when (phase) {
            "Inhale" -> 1.5f
            "Hold" -> 1.5f
            "Exhale" -> 1.0f
            else -> 1.0f
        },
        animationSpec = tween(durationMillis = if (phase == "Inhale") 4000 else if (phase == "Exhale") 8000 else 0),
        label = "breath"
    )

    LaunchedEffect(isActive) {
        if (isActive) {
            startTime = System.currentTimeMillis()
            while (isActive) {
                phase = "Inhale"
                delay(4000)
                phase = "Hold"
                delay(7000)
                phase = "Exhale"
                delay(8000)
            }
        } else {
            if (startTime > 0 && (System.currentTimeMillis() - startTime > 60000)) {
                 viewModel?.logSystemEvent(LogType.BREATHING_SESSION, "Breathing session > 1m")
            }
            phase = "Ready"
            startTime = 0
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("BREATHE", style = TLauncherTypography.labelSmall, color = MaterialTheme.colorScheme.secondary, letterSpacing = 3.sp)
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(200.dp)
        ) {
            // Outer Glow
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(sizeScale)
                    .background(
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                        shape = CircleShape
                    )
            )
            // Core
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(sizeScale * 0.85f)
                    .background(
                        MaterialTheme.colorScheme.secondary,
                        shape = CircleShape
                    )
            )
            
            Text(
                text = phase.uppercase(),
                style = TLauncherTypography.titleMedium,
                color = if (phase == "Ready") MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSecondary
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        TChip(
            text = if (isActive) "STOP" else "START",
            onClick = { isActive = !isActive },
            modifier = Modifier.height(48.dp).fillMaxWidth(0.5f),
            selected = isActive // Active state
        )
    }
}

fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / 1000) / 60
    return String.format("%02d:%02d", minutes, seconds)
}
