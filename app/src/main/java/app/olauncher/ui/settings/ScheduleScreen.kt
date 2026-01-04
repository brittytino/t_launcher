package app.olauncher.ui.settings

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.olauncher.MainViewModel
import app.olauncher.domain.model.CategoryType
import app.olauncher.domain.model.UsageRule
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import app.olauncher.ui.theme.TLauncherTypography

@Composable
fun ScheduleScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val rules by viewModel.allRules.observeAsState(initial = emptyList())
    // We filter rules that start with CATEGORY_
    
    val categoryRules = rules.filter { it.packageName.startsWith("CATEGORY_") }
    
    var showDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<CategoryType?>(null) }
    var currentSchedule by remember { mutableStateOf<UsageRule.ScheduledBlock?>(null) }

    Scaffold(
        topBar = {
            SettingsTopBar(onBack)
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
             item {
                 Text(
                    "Set blocking schedules for app categories.",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            items(CategoryType.values()) { category ->
                // Check if rule exists
                val ruleEntity = categoryRules.find { it.packageName == "CATEGORY_${category.name}" && it.ruleType == "SCHEDULE" }
                val rule = if (ruleEntity != null) {
                    try {
                        app.olauncher.data.local.RuleSerializer.deserialize("SCHEDULE", ruleEntity.ruleData) as? UsageRule.ScheduledBlock
                    } catch (e: Exception) { null }
                } else null

                CategoryScheduleItem(
                    category = category,
                    schedule = rule,
                    onEdit = {
                        selectedCategory = category
                        currentSchedule = rule
                        showDialog = true
                    },
                    onDelete = {
                         if (rule != null) {
                             viewModel.removeRule("CATEGORY_${category.name}", rule)
                         }
                    }
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            }
        }
    }
    
    if (showDialog && selectedCategory != null) {
        ScheduleDialog(
            category = selectedCategory!!,
            existingSchedule = currentSchedule,
            onDismiss = { showDialog = false },
            onSave = { schedule ->
                viewModel.addRule("CATEGORY_${selectedCategory!!.name}", schedule)
                showDialog = false
            }
        )
    }
}



@Composable
fun CategoryScheduleItem(
    category: CategoryType,
    schedule: UsageRule.ScheduledBlock?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.name, 
                style = TLauncherTypography.bodyLarge,
                color = getCategoryColor(category)
            )
            if (schedule != null) {
                Text(
                    text = "${schedule.startTime} - ${schedule.endTime}", 
                    style = TLauncherTypography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = schedule.daysOfWeek.joinToString(", ") { it.name.take(3) },
                    style = TLauncherTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "No Schedule", 
                    style = TLauncherTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (schedule != null) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        } else {
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun ScheduleDialog(
    category: CategoryType,
    existingSchedule: UsageRule.ScheduledBlock?,
    onDismiss: () -> Unit,
    onSave: (UsageRule.ScheduledBlock) -> Unit
) {
    var startTime by remember { mutableStateOf(existingSchedule?.startTime ?: LocalTime.of(9, 0)) }
    var endTime by remember { mutableStateOf(existingSchedule?.endTime ?: LocalTime.of(17, 0)) }
    var selectedDays by remember { mutableStateOf(existingSchedule?.daysOfWeek?.toSet() ?: DayOfWeek.values().toSet()) }
    
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule for ${category.name}") },
        text = {
            Column {
                // Time Pickers
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(onClick = {
                        TimePickerDialog(context, { _, h, m -> startTime = LocalTime.of(h, m) }, startTime.hour, startTime.minute, true).show()
                    }) {
                        Text("Start: $startTime")
                    }
                    Button(onClick = {
                        TimePickerDialog(context, { _, h, m -> endTime = LocalTime.of(h, m) }, endTime.hour, endTime.minute, true).show()
                    }) {
                        Text("End: $endTime")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Active Days:", fontWeight = FontWeight.Bold)
                
                // Day Toggles
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    DayOfWeek.values().take(3).forEach { day ->
                        DayToggle(day, selectedDays.contains(day)) {
                            selectedDays = if (selectedDays.contains(day)) selectedDays - day else selectedDays + day
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    DayOfWeek.values().drop(3).take(4).forEach { day ->
                        DayToggle(day, selectedDays.contains(day)) {
                            selectedDays = if (selectedDays.contains(day)) selectedDays - day else selectedDays + day
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedDays.isNotEmpty()) {
                        onSave(UsageRule.ScheduledBlock(startTime, endTime, selectedDays.toList()))
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DayToggle(day: DayOfWeek, isSelected: Boolean, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(2.dp)
            .size(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray)
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.name.take(1),
            color = if (isSelected) Color.White else Color.Black,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
