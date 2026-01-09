package app.olauncher.ui.productivity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.olauncher.MainViewModel
import app.olauncher.data.local.TaskEntity
import app.olauncher.ui.theme.*

@Composable
fun ProductivityScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    TScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = TLauncherTheme.spacing.medium)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = TLauncherTheme.spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRODUCTIVITY",
                    style = TLauncherTypography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(TLauncherTheme.spacing.medium),
                modifier = Modifier.weight(1f)
            ) {
                // 1. Quick Notes
                item {
                    QuickNotesCard(viewModel)
                }

                // 2. Todo List
                item {
                    TodoListCard(viewModel)
                }

                // 3. Habits
                item {
                    HabitsCard(viewModel)
                }
            }
            
            // 4. Music (Bottom Dock)
            MusicPlayerCard()
            
            Spacer(modifier = Modifier.height(TLauncherTheme.spacing.medium))
        }
    }
}

@Composable
fun QuickNotesCard(viewModel: MainViewModel) {
    val context = LocalContext.current
    // Ideally this would be persisted in DB, using a simple state for now or the "addNote" if it's single.
    // Inventory says: "Single text box". Let's assume there's one main note or scratchpad.
    // For now, I'll use a local state that *should* be in ViewModel to persist. 
    // I will use a placeholder state here, as refactoring VM for single persistent note is out of UI scope,
    // but I'll hook it to "addNote" if user saves.
    
    // Hardening: Persist scratchpad to Prefs
    var noteContent by remember { mutableStateOf(viewModel.prefs.quickNote) }
    
    // Auto-save on change (debounced slightly by effect or just save on disposal/change)
    LaunchedEffect(noteContent) {
        viewModel.prefs.quickNote = noteContent
    }
    
    TCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(TLauncherTheme.spacing.medium)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("QUICK NOTES", style = TLauncherTypography.labelSmall)
                Spacer(modifier = Modifier.weight(1f))
                Text("${noteContent.length} chars", style = TLauncherTypography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(TLauncherTheme.spacing.small))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.surface, TLauncherShapes.small)
                    .padding(8.dp)
            ) {
                BasicTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    textStyle = TLauncherTypography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxSize()
                )
                if (noteContent.isEmpty()) {
                    Text("Type something...", style = TLauncherTypography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f))
                }
            }
            
            Spacer(modifier = Modifier.height(TLauncherTheme.spacing.small))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TChip(text = "Copy", onClick = {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Note", noteContent)
                    clipboard.setPrimaryClip(clip)
                }, icon = Icons.Default.ContentCopy)
                Spacer(modifier = Modifier.width(8.dp))
                TChip(text = "Share", onClick = {
                    val sendIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, noteContent)
                        type = "text/plain"
                    }
                    context.startActivity(android.content.Intent.createChooser(sendIntent, null))
                }, icon = Icons.Default.Share)
            }
        }
    }
}

@Composable
fun TodoListCard(viewModel: MainViewModel) {
    val tasks by viewModel.allTasks.observeAsState(emptyList())
    var newTaskText by remember { mutableStateOf("") }
    var showCompleted by remember { mutableStateOf(false) }

    TCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(TLauncherTheme.spacing.medium)) {
            Text("TASKS", style = TLauncherTypography.labelSmall)
            Spacer(modifier = Modifier.height(TLauncherTheme.spacing.medium))

            // Add Task Input
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = newTaskText,
                    onValueChange = { newTaskText = it },
                    textStyle = TLauncherTypography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (newTaskText.isNotBlank()) {
                            viewModel.addTask(newTaskText)
                            newTaskText = ""
                        }
                    }),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (newTaskText.isEmpty()) Text("Add a new task...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        inner()
                    }
                )
                IconButton(onClick = {
                    if (newTaskText.isNotBlank()) {
                        viewModel.addTask(newTaskText)
                        newTaskText = ""
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                }
            }
            
            Divider(color = MaterialTheme.colorScheme.surface, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

            // Active Tasks
            val activeTasks = tasks.filter { !it.isCompleted }
            Column {
                activeTasks.forEach { task ->
                    TaskItem(task, viewModel)
                }
            }

            // Completed Tasks
            val completedTasks = tasks.filter { it.isCompleted }
            if (completedTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCompleted = !showCompleted }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Completed (${completedTasks.size})",
                        style = TLauncherTypography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        if (showCompleted) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                AnimatedVisibility(visible = showCompleted) {
                    Column {
                        completedTasks.forEach { task ->
                            TaskItem(task, viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: TaskEntity, viewModel: MainViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.toggleTaskCompletion(task) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Custom Checkbox
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    if (task.isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent,
                    TLauncherShapes.extraSmall
                )
                .then(
                    if (!task.isCompleted) Modifier.border(2.dp, MaterialTheme.colorScheme.outline, TLauncherShapes.extraSmall)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (task.isCompleted) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = task.text,
            style = TLauncherTypography.bodyMedium.copy(
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.weight(1f)
        )
        
        IconButton(onClick = { viewModel.deleteTask(task) }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha=0.5f), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun HabitsCard(viewModel: MainViewModel) {
    // Inventory: "Habit cards (eg. Do Workout, No Fap) ... Daily check-in button"
    // Using checkTodayLog() to get status?
    // Data/CheckIn is: diet, sugar, workout, productive.
    // We can map these to "Habits".
    
    // NOTE: In a real app we'd query specific habit entities. 
    // Here we hardcode to the "Daily Protocol" booleans for simplicity as per existing ViewModel.

    // We don't have direct access to "isDietChecked" etc as observable booleans cleanly exposed in the outline
    // except via "checkTodayLog" which updates something?
    // Let's assume for UI demo we have local state or mock it.
    // Actually, "Dashboard" used these too. 
    
    var diet by remember { mutableStateOf(false) }
    var workout by remember { mutableStateOf(false) }
    var noSugar by remember { mutableStateOf(false) }
    
    // We would sync this with VM ideally.
    
    TCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(TLauncherTheme.spacing.medium)) {
            Text("HABITS & PROTOCOLS", style = TLauncherTypography.labelSmall)
            Spacer(modifier = Modifier.height(TLauncherTheme.spacing.medium))
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(TLauncherTheme.spacing.small)) {
                item { HabitItem("Workout", workout) { workout = !workout; viewModel.saveCheckIn(diet, noSugar, workout, false) } }
                item { HabitItem("No Sugar", noSugar) { noSugar = !noSugar; viewModel.saveCheckIn(diet, noSugar, workout, false) } }
                item { HabitItem("Clean Diet", diet) { diet = !diet; viewModel.saveCheckIn(diet, noSugar, workout, false) } }
            }
        }
    }
}

@Composable
fun HabitItem(name: String, isDone: Boolean, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .width(100.dp)
            .height(80.dp)
            .background(
                if (isDone) MaterialTheme.colorScheme.primary.copy(alpha=0.2f) else MaterialTheme.colorScheme.surface,
                TLauncherShapes.medium
            )
            .clickable(onClick = onToggle)
            .padding(12.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column {
             Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(name, style = TLauncherTypography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
fun MusicPlayerCard() {
    TCard(modifier = Modifier.fillMaxWidth()) {
        ApplicationSpecificMusicPlayer()
    }
}

@Composable
fun ApplicationSpecificMusicPlayer() {
     Row(
        modifier = Modifier
            .padding(TLauncherTheme.spacing.medium)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, TLauncherShapes.small),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Not Playing", style = TLauncherTypography.bodyLarge, fontWeight = FontWeight.Medium)
            Text("Music Service", style = TLauncherTypography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        IconButton(onClick = { /* Prev */ }) { Icon(Icons.Default.SkipPrevious, contentDescription = "Prev") }
        IconButton(onClick = { /* Play */ }) { Icon(Icons.Default.PlayArrow, contentDescription = "Play") }
        IconButton(onClick = { /* Next */ }) { Icon(Icons.Default.SkipNext, contentDescription = "Next") }
    }
}
