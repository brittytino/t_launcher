package app.olauncher.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.olauncher.MainViewModel
import app.olauncher.data.local.AccountabilityEntity
import app.olauncher.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val todayLog by viewModel.todayLog.observeAsState()
    val allLogs by viewModel.allAccountabilityLogs.observeAsState(emptyList())
    val screenTime by viewModel.screenTimeValue.observeAsState("0m")
    
    // Check-in State
    var diet by remember { mutableStateOf(false) }
    var sugar by remember { mutableStateOf(false) }
    var workout by remember { mutableStateOf(false) }
    var productive by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.checkTodayLog()
        viewModel.getTodaysScreenTime()
    }

    LaunchedEffect(todayLog) {
        todayLog?.let {
            diet = it.dietFollowed
            sugar = it.sugarFree
            workout = it.didWorkout
            productive = it.productiveToday
        }
    }

    TScaffold {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(TLauncherTheme.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TLauncherTheme.spacing.large)
        ) {
            item {
                Text(
                    "DASHBOARD", 
                    style = TLauncherTypography.labelMedium, 
                    color = MaterialTheme.colorScheme.primary, 
                    modifier = Modifier.padding(top = TLauncherTheme.spacing.small)
                )
            }

            // Screen Time
            item {
                TCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(TLauncherTheme.spacing.large), 
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("SCREEN TIME TODAY", style = TLauncherTypography.labelSmall)
                        Spacer(modifier = Modifier.height(TLauncherTheme.spacing.small))
                        Text(screenTime ?: "0m", style = TLauncherTypography.headlineLarge.copy(fontSize = 48.sp))
                    }
                }
            }

            // Daily Check-in
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("DAILY PROTOCOL", style = TLauncherTypography.labelSmall, modifier = Modifier.padding(bottom = TLauncherTheme.spacing.small))
                    
                    TCard {
                        Column(modifier = Modifier.padding(TLauncherTheme.spacing.medium)) {
                            CheckInItem("Strict Diet Followed?", diet) { diet = it }
                            CheckInItem("No Sugar / Junk?", sugar) { sugar = it }
                            CheckInItem("Workout Completed?", workout) { workout = it }
                            CheckInItem("Productive & Focused?", productive) { productive = it }
                        }
                    }

                    Spacer(modifier = Modifier.height(TLauncherTheme.spacing.medium))
                    
                    // Commit Button (Chip style)
                    TChip(
                        text = "COMMIT LOG",
                        onClick = { viewModel.saveCheckIn(diet, sugar, workout, productive) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        selected = true
                    )
                }
            }

            // Heatmap
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("CONSISTENCY HEATMAP", style = TLauncherTypography.labelSmall, modifier = Modifier.padding(bottom = TLauncherTheme.spacing.small))
                    TCard {
                         Box(modifier = Modifier.padding(TLauncherTheme.spacing.medium)) {
                             Heatmap(allLogs)
                         }
                    }
                }
            }

            // Developer Panel
            item {
                DeveloperPanel(
                    todayLog = todayLog,
                    onSave = { lc, cf ->
                         viewModel.saveDevMetrics(lc, cf) 
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(TLauncherTheme.spacing.extraLarge))
            }
        }
    }
}

@Composable
fun CheckInItem(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = TLauncherTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, style = TLauncherTypography.bodyLarge)
        TSwitch(
            checked = checked, 
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun Heatmap(logs: List<AccountabilityEntity>) {
    // Visualise last 100 days
    val gridItems = remember(logs) {
        val today = java.time.LocalDate.now()
        val formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
        val logMap = logs.associateBy { it.date }
        
        List(100) { index ->
            // Index 0 is today, 99 is 99 days ago? Or reverse?
            // Let's do: Top-left is oldest? Standard GitHub style is columns.
            // GridCells.Fixed(10) -> 10 columns. 10 rows? 
            // Let's just show last 100 days.
            val date = today.minusDays((99 - index).toLong())
            val dateStr = date.format(formatter)
            val log = logMap[dateStr]
            
            // Interaction: Green if productive OR workout OR diet
            log != null && (log.productiveToday || log.didWorkout || log.dietFollowed)
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(10), 
        modifier = Modifier.height(200.dp),
        userScrollEnabled = false,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(gridItems) { isActive ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(
                         if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f),
                         RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

@Composable
fun DeveloperPanel(
    todayLog: AccountabilityEntity?,
    onSave: (Int, Int) -> Unit
) {
    var leetcode by remember(todayLog) { mutableStateOf(todayLog?.leetcodeCount?.toString() ?: "") }
    var codeforces by remember(todayLog) { mutableStateOf(todayLog?.codeforcesCount?.toString() ?: "") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("DEV METRICS", style = TLauncherTypography.labelSmall, modifier = Modifier.padding(bottom = TLauncherTheme.spacing.small))
        
        TCard {
            Column(modifier = Modifier.padding(TLauncherTheme.spacing.medium)) {
                OutlinedTextField(
                    value = leetcode,
                    onValueChange = { 
                        leetcode = it 
                        onSave(it.toIntOrNull() ?: 0, codeforces.toIntOrNull() ?: 0)
                    },
                    label = { Text("LeetCode Solved") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.2f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(TLauncherTheme.spacing.small))
                OutlinedTextField(
                    value = codeforces,
                    onValueChange = { 
                        codeforces = it 
                        onSave(leetcode.toIntOrNull() ?: 0, it.toIntOrNull() ?: 0)
                    },
                    label = { Text("Codeforces Count") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.2f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}


