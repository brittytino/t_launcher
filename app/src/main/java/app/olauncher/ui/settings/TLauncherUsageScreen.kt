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
import app.olauncher.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TLauncherUsageScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val logs by viewModel.surveillanceLogs.observeAsState(emptyList())

    TScaffold {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = TLauncherTheme.spacing.medium)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = TLauncherTheme.spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                 IconButton(onClick = onBack) {
                     Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                 }
                 Text("SYSTEM SURVEILLANCE", style = TLauncherTypography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            }
            
            // Status Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AUDIT TRAIL",
                    style = TLauncherTypography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 2.sp
                )
                
                TChip(
                    text = "LIVE",
                    onClick = {},
                    selected = true,
                    modifier = Modifier.height(24.dp)
                )
            }

            if (logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("NO EVENTS DETECTED", style = TLauncherTypography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                TCard(modifier = Modifier.fillMaxSize().padding(bottom = TLauncherTheme.spacing.medium)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(logs.reversed()) { log ->
                            LogItem(log)
                            HorizontalDivider(color = MaterialTheme.colorScheme.surface, thickness = 1.dp)
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
        LogType.FOCUS_SESSION, LogType.BREATHING_SESSION, LogType.ALARM_SUCCESS -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = "[${dateFormat.format(Date(log.timestamp))}]",
            style = TLauncherTypography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.width(110.dp)
        )
        
        Column {
             Text(
                text = log.type.name,
                style = TLauncherTypography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                color = color
            )
            if (log.message.isNotEmpty()) {
                Text(
                    text = "> ${log.message}",
                    style = TLauncherTypography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
