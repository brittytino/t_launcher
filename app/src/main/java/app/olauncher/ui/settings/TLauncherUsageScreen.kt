package app.olauncher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.olauncher.MainViewModel
import app.olauncher.data.local.SystemLogEntity
import app.olauncher.data.local.LogType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TLauncherUsageScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val logs by viewModel.surveillanceLogs.observeAsState(emptyList())

    // ROBUST FIX:
    // 1. Box fills ENTIRE screen (including behind notch) -> Draws Background.
    // 2. Scaffold has statusBarsPadding -> Pushes ALL content (TopBar + Body) down.
    // 3. Status Bar area shows Box's background (matches app theme).
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            modifier = Modifier.statusBarsPadding(),
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "SYSTEM SURVEILLANCE", 
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface 
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent, // Transparent to blend with Box
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    )
                )
            },
            containerColor = Color.Transparent // Transparent so Box background shows
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                // Stats Summary Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AUDIT TRAIL",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "LIVE FEED",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = Color.Green.copy(alpha = 0.8f) // "Live" indicator
                    )
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                if (logs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("NO EVENTS DETECTED", fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(logs) { log ->
                            LogItem(log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogItem(log: SystemLogEntity) {
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm:ss", Locale.US) }
    val color = when (log.type) {
        LogType.VIOLATION, LogType.ALARM_FAILURE, LogType.MISSED_CHECKIN, LogType.EMERGENCY_OVERRIDE, LogType.APP_BLOCK_EVENT -> MaterialTheme.colorScheme.error
        LogType.FOCUS_SESSION, LogType.BREATHING_SESSION, LogType.ALARM_SUCCESS -> Color(0xFF4CAF50) // Green
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "[${dateFormat.format(Date(log.timestamp))}]",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.width(110.dp)
        )
        
        Column {
             Text(
                text = log.type.name,
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                color = color
            )
            if (log.message.isNotEmpty()) {
                Text(
                    text = "> ${log.message}",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
