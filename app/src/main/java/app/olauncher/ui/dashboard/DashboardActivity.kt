package app.olauncher.ui.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.olauncher.TLauncherApplication
import app.olauncher.domain.managers.ModeManager
import app.olauncher.domain.model.AppMode
import app.olauncher.ui.theme.TLauncherTheme
import app.olauncher.ui.theme.TLauncherTypography

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as TLauncherApplication).container
        val modeManager = container.modeManager
        val logRepository = container.systemLogRepository
        val usageRepository = container.usageStatsRepository
        val accountabilityRepository = container.accountabilityRepository
        
        setContent {
            TLauncherTheme {
                val todayViolations = produceState(initialValue = 0) {
                    value = logRepository.getViolationCountToday()
                }
                
                val totalUsage = produceState(initialValue = 0L) {
                    value = usageRepository.getTodayTotalUsage()
                }

                val todayLog = produceState<app.olauncher.data.local.AccountabilityEntity?>(initialValue = null) {
                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    value = accountabilityRepository.getLogForDate(today)
                }

                val allLogs by accountabilityRepository.allLogs.collectAsStateWithLifecycle(initialValue = emptyList())

                DashboardScreen(
                    modeManager = modeManager, 
                    violations = todayViolations.value,
                    totalUsageMillis = totalUsage.value,
                    accountabilityLog = todayLog.value,
                    allLogs = allLogs
                ) {
                    finish() // Close dashboard on back/home action if needed
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    modeManager: ModeManager, 
    violations: Int,
    totalUsageMillis: Long,
    accountabilityLog: app.olauncher.data.local.AccountabilityEntity?,
    allLogs: List<app.olauncher.data.local.AccountabilityEntity>,
    onClose: () -> Unit
) {
    val currentMode by modeManager.currentMode.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(
                    text = "COMPLIANCE RECORD",
                    style = TLauncherTypography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp
                )
            }

            item {
                ModeSelector(currentMode) { newMode ->
                    modeManager.setMode(newMode)
                }
            }

            item {
                StatsSection(violations, totalUsageMillis)
            }
            
            item {
                AccountabilitySection(accountabilityLog)
            }

            item {
                YearlyContributionGrid(allLogs)
            }
        }
    }
}

@Composable
fun YearlyContributionGrid(logs: List<app.olauncher.data.local.AccountabilityEntity>) {
    // Strict Logic: Green (All Yes), Red (Any No or Missed), Grey (Future)
    val context = LocalContext.current
    val currentYear = java.time.LocalDate.now().year
    val today = java.time.LocalDate.now()
    
    val logMap = remember(logs) {
        logs.associateBy { java.time.LocalDate.parse(it.date) }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("HISTORICAL DATA ($currentYear)", style = TLauncherTypography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Determine start date (Jan 1)
                val startDate = java.time.LocalDate.of(currentYear, 1, 1)
                
                // GitHub-style grid logic (simplified for walkthrough relevance)
                // Color mapping:
                fun getColor(date: java.time.LocalDate): Color {
                    if (date.year != currentYear) return Color.Transparent
                    if (date.isAfter(today)) return Color.LightGray.copy(alpha = 0.2f)
                    
                    val log = logMap[date]
                    if (log == null) return Color(0xFFE57373) // Red (Failure to report)
                    
                    val isPass = log.dietFollowed && log.productiveToday && log.sugarFree && log.didWorkout
                    return if (isPass) Color(0xFF4CAF50) else Color(0xFFE57373)
                }
                
                // Render "Weeks" (Row of Columns)
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                     items(53) { weekIndex ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(7) { dayIndex ->
                                val dayOfYear = (weekIndex * 7) + dayIndex
                                // Approximate mapping, assumes Jan 1 is start of first col
                                val date = startDate.plusDays(dayOfYear.toLong())
                                
                                if (date.year == currentYear) {
                                     Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(
                                                getColor(date),
                                                shape = RoundedCornerShape(2.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ModeSelector(currentMode: AppMode, onModeSelected: (AppMode) -> Unit) {
    Column {
        Text("CURRENT STATE", style = TLauncherTypography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        
        AppMode.values().forEach { mode ->
            ModeItem(
                mode = mode,
                isSelected = mode == currentMode,
                onClick = { onModeSelected(mode) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ModeItem(mode: AppMode, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mode.name,
                    style = TLauncherTypography.titleSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = getModeDescription(mode),
                    style = TLauncherTypography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
            if (isSelected) {
                Text("CORRECT", style = TLauncherTypography.labelSmall, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

fun getModeDescription(mode: AppMode): String {
    return when (mode) {
        AppMode.NORMAL -> "Standard protocols."
        AppMode.BORED -> "Nothing to see here."
        AppMode.PRODUCTIVITY -> "Essentials only."
        AppMode.DRIVING -> "Eyes on the road."
        AppMode.EMERGENCY -> "Weakness detected."
    }
}

@Composable
fun StatsSection(violations: Int, totalUsageMillis: Long) {
    val hours = totalUsageMillis / (1000 * 60 * 60)
    val minutes = (totalUsageMillis / (1000 * 60)) % 60
    val timeString = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

    Column {
        Text("METRICS", style = TLauncherTypography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                StatRow("Time Wasted", timeString)
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                StatRow("Failures Today", violations.toString())
            }
        }
    }
}

@Composable
fun AccountabilitySection(log: app.olauncher.data.local.AccountabilityEntity?) {
    Column {
        Text("DAILY REPORT", style = TLauncherTypography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (log == null) {
                    Text(
                        "Report Missing. Submit immediately.",
                        style = TLauncherTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    StatRow("Diet", if (log.dietFollowed) "YES" else "NO")
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    StatRow("Sugar", if (log.sugarFree) "ZERO" else "FAILED")
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    StatRow("Training", if (log.didWorkout) "DONE" else "MISSED")
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    StatRow("Productive", if (log.productiveToday) "YES" else "NO")
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = TLauncherTypography.bodyMedium)
        Text(value, style = TLauncherTypography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
