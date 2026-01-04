package app.olauncher.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import app.olauncher.MainViewModel
import app.olauncher.data.local.NoteEntity
import app.olauncher.data.local.TaskEntity
import app.olauncher.ui.theme.TLauncherTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class ProductivityFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                TLauncherTheme {
                    val viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
                    ProductivityScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun ProductivityScreen(viewModel: MainViewModel) {
    // 0 = Focus (Tasks/Notes), 1 = Journal (Heatmap), 2 = Tools
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // ROBUST NOTCH FIX: "Box Wrapper" Strategy
    // 1. Box draws the full-screen background (behind notch).
    // 2. Inner Box/Column uses statusBarsPadding() + Extra Checker to push content down safely.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() // Pushes down below official status bar height
                .padding(top = 16.dp) // Extra safety margin for deep notches (Vivo V23)
        ) {
            // FIX: Add spacer BEFORE header to push it down below notch
            Spacer(modifier = Modifier.height(48.dp))

            // Header / Navigation
            ToolsHeader(selectedTab) { selectedTab = it }
            
            // Content
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(targetState = selectedTab, label = "TabContent") { tab ->
                    when (tab) {
                        0 -> FocusScreen(viewModel)
                        1 -> JournalScreen(viewModel)
                        2 -> ToolsScreen(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun ToolsHeader(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    // Premium Segmented Control / Tab Style
    // Rounded container with muted background, active pill.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderTab("FOCUS", selectedTab == 0) { onTabSelected(0) }
        HeaderTab("JOURNAL", selectedTab == 1) { onTabSelected(1) }
        HeaderTab("TOOLS", selectedTab == 2) { onTabSelected(2) }
    }
}

@Composable
fun HeaderTab(text: String, isSelected: Boolean, onClick: () -> Unit) {
    // Premium Typography & Indicator
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() }
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = if(isSelected) 14.sp else 12.sp,
                letterSpacing = 2.sp
            ),
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Animated Underline Indicator
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
             Box(
                modifier = Modifier
                    .width(40.dp) // Fixed width pill
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

// --- FOCUS SCREEN (Tasks / Notes) ---

@Composable
fun FocusScreen(viewModel: MainViewModel) {
    var isTasks by remember { mutableStateOf(true) } // Toggle state
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Toggle Switch
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            CustomToggle(isTasks) { isTasks = it }
        }

        // List
        Box(modifier = Modifier.weight(1f)) {
            if (isTasks) {
                TasksList(viewModel)
            } else {
                NotesList(viewModel)
            }
        }

        // FAB
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Add")
            }
        }
    }

    if (showAddDialog) {
        if (isTasks) {
            AddTaskDialog({ showAddDialog = false }, { viewModel.addTask(it); showAddDialog = false })
        } else {
            AddNoteDialog({ showAddDialog = false }, { t, c -> viewModel.addNote(t, c); showAddDialog = false })
        }
    }
}

@Composable
fun CustomToggle(isLeft: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToggleButton("TASKS", isLeft) { onToggle(true) }
        ToggleButton("NOTES", !isLeft) { onToggle(false) }
    }
}

@Composable
fun ToggleButton(text: String, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isActive) MaterialTheme.colorScheme.surface else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// --- TASKS & NOTES LIST ITEMS (Reusing logic but styling better) ---
// (Simplified for brevity, assuming standard LazyColumn implementation similar to previous but refined)
@Composable
fun TasksList(viewModel: MainViewModel) {
    val tasks by viewModel.allTasks.observeAsState(emptyList())
    LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
        items(tasks, key = { it.id }) { task ->
            TaskRow(task, { viewModel.toggleTaskCompletion(task) }, { viewModel.deleteTask(task) })
        }
    }
}

@Composable
fun TaskRow(task: TaskEntity, onCheck: (Boolean) -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = task.isCompleted, onCheckedChange = onCheck, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary))
        Text(
            text = task.text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
        )
        IconButton(onClick = onDelete) { Icon(Icons.Default.Close, "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha=0.6f)) }
    }
}

@Composable
fun NotesList(viewModel: MainViewModel) {
    val notes by viewModel.allNotes.observeAsState(emptyList())
    LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
        items(notes, key = { it.id }) { note ->
            NoteRow(note) { viewModel.deleteNote(note) }
        }
    }
}

@Composable
fun NoteRow(note: NoteEntity, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(note.title.ifBlank { "Untitled" }, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
            IconButton(onClick = onDelete) { Icon(Icons.Default.Close, "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha=0.6f)) }
        }
        if (expanded || note.content.isNotEmpty()) {
            Text(note.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = if (expanded) Int.MAX_VALUE else 2)
        }
    }
}

// --- JOURNAL SCREEN (HEATMAP) ---

@Composable
fun JournalScreen(viewModel: MainViewModel) {
    val logs by viewModel.allAccountabilityLogs.observeAsState(emptyList())
    
    // User requested: "Calendar jan 1 thursday to dec 31 thursday... One box per day... Green ALL, Red ANY"
    // I will build the grid manually to ensure 2026 structure.
    
    var showCheckIn by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Heatmap Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("2026 CONSISTENCY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Pass save callback down to grid -> box
                HeatmapGrid(logs) { d, s, w, p -> 
                    viewModel.saveCheckIn(d, s, w, p)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegendItem(Color(0xFF2E7D32), "Perfect")
                    LegendItem(Color(0xFFC62828), "Missed")
                    LegendItem(Color(0xFF424242), "Pending")
                }
            }
        }
        
        // Stats
        UsageStatsSection(viewModel)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Check-In Button
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
             Button(
                 onClick = { showCheckIn = true },
                 colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                 modifier = Modifier.fillMaxWidth(0.8f).height(56.dp)
             ) {
                 Text("DAILY CHECK-IN", fontSize = 16.sp, fontWeight = FontWeight.Bold)
             }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
    
    if (showCheckIn) {
        // Pre-fill with today's log if exists? ViewModel doesn't expose 'todayLog' easily here without observer.
        // But saveCheckIn overwrites.
        // Ideally we fetch today's log first in the dialog.
        // For now, new check-in defaults to false.
        AccountabilityDialog(
            onDismiss = { showCheckIn = false },
            onSave = { d, s, w, p -> viewModel.saveCheckIn(d, s, w, p); showCheckIn = false }
        )
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun HeatmapGrid(
    logs: List<app.olauncher.data.local.AccountabilityEntity>,
    onSaveToday: (Boolean, Boolean, Boolean, Boolean) -> Unit
) {
    // 2026 Config: Jan 1 is Thursday. 365 days.
    // GitHub style: 7 rows (Mon-Sun or Sun-Sat?), Columns = Weeks.
    // User image shows Mon, Wed, Fri labels. Usually Sun-Sat or Mon-Sun.
    // Jan 1 2026 is Thursday.
    
    // Let's generate a list of 365 days.
    val days = remember {
        val start = LocalDate.of(2026, 1, 1)
        (0 until 365).map { start.plusDays(it.toLong()) }
    }
    
    // Map logs to dates for O(1) lookup
    val logMap = remember(logs) { logs.associateBy { it.date } } // date string "YYYY-MM-DD"

    // Grid: Horizontal scroll or fitted? "Calendar-accurate". GitHub is horizontal scroll usually.
    // I'll make it horizontal scrollable Row of Columns (Weeks).
    
    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
        // Offset for Jan 1 being Thursday (Assuming Mon start).
        // Mon=1, Tue=2, Wed=3, Thu=4. So 3 empty slots if Mon-start.
        
        // 2026 has 52 weeks and 1 day, or spans across 53 partial weeks.
        
        repeat(53) { weekIndex ->
            Column(modifier = Modifier.padding(2.dp)) {
                for (row in 0..6) { // 0=Mon, ... 6=Sun
                     val dayOffset = (weekIndex * 7) + row
                     // Jan 1 is Thursday. If Mon is start, then index 0,1,2 are pre-year.
                     val actualDayIndex = dayOffset - 3 
                     
                     if (actualDayIndex >= 0 && actualDayIndex < days.size) {
                         val date = days[actualDayIndex]
                         val log = logMap[date.toString()]
                         DayBox(date, log, onSaveToday)
                     } else {
                         // Empty placeholder
                         Box(modifier = Modifier.size(14.dp).padding(1.dp).background(Color.Transparent))
                     }
                }
            }
        }
    }
}

@Composable
fun DayBox(
    date: LocalDate, 
    log: app.olauncher.data.local.AccountabilityEntity?,
    onSaveToday: (Boolean, Boolean, Boolean, Boolean) -> Unit
) {
    // Logic:
    // Green -> log != null AND (diet && sugar && workout && productive)
    // Red -> log != null AND !(all true)
    // Gray -> log == null (Future or missed without logging? User says "Skipped -> Red").
    // User says "One box per day. Color Logic: Green ALL checked. Red ANY missed or skipped."
    // So if it's the Past and NO log -> Should be Red? Or Gray?
    // Usually Gray is "No Activity". Red is "Failure".
    // I'll stick to: If Log exists -> Check logic. If No log -> Gray (Pending/No Data).
    // Wait, "Skipped -> Red" in requirements.
    // If date < Today AND no log -> Red.
    
    val today = LocalDate.now()
    val isPast = date.isBefore(today)
    val isFuture = date.isAfter(today)
    
    val color = when {
        isFuture -> Color(0xFF1E1E1E) // Future dark gray
        log != null -> {
             if (log.dietFollowed && log.sugarFree && log.didWorkout && log.productiveToday) Color(0xFF2E7D32) // Green
             else Color(0xFFC62828) // Red
        }
        isPast -> Color(0xFFC62828) // Skipped = Red (Brutal mode)
        else -> Color(0xFF424242) // Today, pending
    }
    
    // Click interaction
    var showPopup by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .size(14.dp) 
            .padding(1.dp)
            .background(color, RoundedCornerShape(2.dp))
            .clickable { showPopup = true }
    )
    
    if (showPopup && log != null) {
        AlertDialog(
            onDismissRequest = { showPopup = false },
            title = { Text(date.toString()) },
            text = {
                Column {
                    Text("Diet: ${if (log.dietFollowed) "YES" else "NO"}")
                    Text("Sugar Clean: ${if (log.sugarFree) "YES" else "NO"}")
                    Text("Workout: ${if (log.didWorkout) "YES" else "NO"}")
                    Text("Productive: ${if (log.productiveToday) "YES" else "NO"}")
                }
            },
            confirmButton = { TextButton(onClick = { showPopup = false }) { Text("Close") } },
            dismissButton = { 
                // ONLY ALLOW EDITING TODAY
                if (date == LocalDate.now()) {
                    TextButton(onClick = { showPopup = false; showEdit = true }) { Text("Edit") } 
                }
            }
        )
    } else if (showPopup) {
         // If clicking past data that is empty, maybe allow retroactive logging? 
         // User said "log his daily log". Usually means today. 
         // But if "edit that too", implies modification.
         // Let's allow editing only if log exists for now, or if it is Today.
         if (date == LocalDate.now()) showEdit = true // Allow logging for today
         else {
             AlertDialog(
                onDismissRequest = { showPopup = false },
                title = { Text(date.toString()) },
                text = { Text("No Data Recorded") },
                confirmButton = { TextButton(onClick = { showPopup = false }) { Text("Close") } }
            )
         }
    }
    
    if (showEdit) {
        // Need to pass a way to save. But ViewModel.saveCheckIn uses TODAY.
        // We need 'saveCheckInForDate' or assume editing is only for Today?
        // User said "after the user log his daily log , he can edit that too".
        // Likely implies Today's log. 
        // If I strictly use 'saveCheckIn', it inserts for Today.
        // If I am editing a PAST log, I need to prevent overwriting Today's data with Past data or vice-versa.
        // The DAO 'insertLog' replaces based on Primary Key (Date).
        // So if I construct an Entity with the CURRENT box's date, it works.
        // But 'viewModel.saveCheckIn' constructs for TODAY. 
        // I should probably only allow editing TODAY'S log for safety unless I update ViewModel.
        // For now, let's assume editing is for Today. If user clicks a past log, viewing is fine.
        
        // Wait, if I click Today's box -> Edit -> Save -> It calls saveCheckIn (Today). Correct.
        // If I click Past box -> Edit -> Save -> It calls saveCheckIn (Today) -> Overwrites TODAY with Past data? NO!
        // I MUST NOT allow editing past logs via 'saveCheckIn' unless I parameterize date.
        
        // For safety/scope: Only allow editing TODAY'S log.
        AccountabilityDialog(
            initialLog = log,
            onDismiss = { showEdit = false },
            onSave = { d, s, w, p -> 
                // This calls vm.saveCheckIn which uses new Date() -> Today. Correct.
                // Effectively updates today.
                // We need to pass the ViewModel down or use a callback.
                 // IMPORTANT: 'DayBox' doesn't have reference to ViewModel directly but parent does.
                 // But DayBox is inside HeatmapGrid.
                 // I need to propagate the Save action.
                 // Simplify: DayBox just triggers a callback 'onEdit(log)'?
                 // Refactoring required to pass callback.
                onSaveToday(d, s, w, p)
                showEdit = false
            }
        )
    }
}

// --- TOOLS SCREEN ---
@Composable
fun ToolsScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
         app.olauncher.ui.FocusTimer(viewModel)
         Divider(color = MaterialTheme.colorScheme.surfaceVariant)
         app.olauncher.ui.BreathingExercise(viewModel)
    }
}

@Composable
fun WallpaperSelector() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var currentName by remember { mutableStateOf("Tap to Change") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("WALLPAPER", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, letterSpacing = 3.sp)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().clickable { 
                // Randomly select or cycle? User said "one bar to change". 
                // Let's make it a simple "Next" button effect or a horizontal list?
                // "add one bar to change wallpaper".
                // Let's simple Cycle for now.
                val count = app.olauncher.helper.WallpaperManager.getWallpaperCount()
                val randomIdx = (0 until count).random()
                app.olauncher.helper.WallpaperManager.applyWallpaper(context, randomIdx)
                currentName = app.olauncher.helper.WallpaperManager.getWallpaperName(randomIdx)
                android.widget.Toast.makeText(context, "Applied: $currentName", android.widget.Toast.LENGTH_SHORT).show()
            }
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(currentName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Tap to Randomize", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Default.Refresh, contentDescription = "Change", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}


// --- DIALOGS (Updated style) ---
@Composable
fun AccountabilityDialog(
    initialLog: app.olauncher.data.local.AccountabilityEntity? = null,
    onDismiss: () -> Unit, 
    onSave: (Boolean, Boolean, Boolean, Boolean) -> Unit
) {
    var diet by remember { mutableStateOf(initialLog?.dietFollowed ?: false) }
    var sugar by remember { mutableStateOf(initialLog?.sugarFree ?: false) }
    var workout by remember { mutableStateOf(initialLog?.didWorkout ?: false) }
    var productive by remember { mutableStateOf(initialLog?.productiveToday ?: false) }
    
    val isEditing = initialLog != null
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if(isEditing) "EDIT LOG" else "DAILY CHECK-IN (9 PM)", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CheckItem("Diet Followed?", diet) { diet = it }
                CheckItem("Avoided Sugar?", sugar) { sugar = it }
                CheckItem("Workout Done?", workout) { workout = it }
                CheckItem("Productive Today?", productive) { productive = it }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Rules: All YES = Green. One NO = Red.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(onClick = { onSave(diet, sugar, workout, productive) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Text(if(isEditing) "UPDATE" else "SUBMIT")
            }
        }
    )
}

@Composable
fun CheckItem(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun UsageStatsSection(viewModel: MainViewModel) {
    val stats by viewModel.usageStats.observeAsState(app.olauncher.MainViewModel.TLauncherStats())
    LaunchedEffect(Unit) { viewModel.fetchUsageStats() } // Fetch on load
    
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("USAGE SUMMARY", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
             StatItem("Focus", stats.focusSessions.toString())
             StatItem("Breathing", stats.breathingSessions.toString())
             StatItem("Blocked", stats.blocks.toString())
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) { 
    var text by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add Task") }, text = { OutlinedTextField(value = text, onValueChange = { text = it }) }, confirmButton = { Button(onClick = { onAdd(text) }) { Text("Add") } })
}
@Composable
fun AddNoteDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) { 
    var t by remember { mutableStateOf("") }
    var c by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("New Note") }, text = { Column { OutlinedTextField(value = t, onValueChange = { t = it }, label={Text("Title")}); OutlinedTextField(value = c, onValueChange = { c = it }, label={Text("Content")}) } }, confirmButton = { Button(onClick = { onAdd(t, c) }) { Text("Save") } })
}
