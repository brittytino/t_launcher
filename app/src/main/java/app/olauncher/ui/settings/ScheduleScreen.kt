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
import androidx.compose.material.icons.filled.ArrowBack
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
import app.olauncher.ui.theme.*
import androidx.compose.ui.window.Dialog

@Composable
fun ScheduleScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val rules by viewModel.allRules.observeAsState(initial = emptyList())
    val categoryRules = rules.filter { it.packageName.startsWith("CATEGORY_") }
    
    var showDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<CategoryType?>(null) }
    var currentSchedule by remember { mutableStateOf<UsageRule.ScheduledBlock?>(null) }

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
                 Text("BLOCKING SCHEDULES", style = TLauncherTypography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            }
            
            Text(
                "Automate focus by scheduling blocks per category.",
                style = TLauncherTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Content
            TCard(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(modifier = Modifier.padding(vertical = 8.dp)) {
                    items(CategoryType.values()) { category ->
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
                         if (category != CategoryType.values().last()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.surface, thickness = 1.dp)
                         }
                    }
                }
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
                style = TLauncherTypography.titleMedium,
                color = getCategoryColor(category)
            )
            if (schedule != null) {
                Text(
                    text = "${schedule.startTime} - ${schedule.endTime}", 
                    style = TLauncherTypography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f)
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
    
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        TCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("SET SCHEDULE", style = TLauncherTypography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(24.dp))
                
                // Time Pickers
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TChip(
                        text = "Start: $startTime", 
                        onClick = { TimePickerDialog(context, { _, h, m -> startTime = LocalTime.of(h, m) }, startTime.hour, startTime.minute, true).show() },
                        modifier = Modifier.weight(1f),
                        selected = false
                    )
                    TChip(
                        text = "End: $endTime", 
                        onClick = { TimePickerDialog(context, { _, h, m -> endTime = LocalTime.of(h, m) }, endTime.hour, endTime.minute, true).show() },
                        modifier = Modifier.weight(1f),
                        selected = false
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("Active Days", style = TLauncherTypography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(12.dp))
                
                // Day Toggles
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                     DayOfWeek.values().take(4).forEach { day ->
                         DayToggle(day, selectedDays.contains(day)) {
                             selectedDays = if (selectedDays.contains(day)) selectedDays - day else selectedDays + day
                         }
                     }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                     DayOfWeek.values().drop(4).forEach { day ->
                         DayToggle(day, selectedDays.contains(day)) {
                             selectedDays = if (selectedDays.contains(day)) selectedDays - day else selectedDays + day
                         }
                     }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TChip(
                        text = "CANCEL", 
                        onClick = onDismiss, 
                        modifier = Modifier.weight(1f),
                        selected = false
                    )
                    TChip(
                        text = "SAVE",
                        onClick = {
                            if (selectedDays.isNotEmpty()) {
                                onSave(UsageRule.ScheduledBlock(startTime, endTime, selectedDays.toList()))
                            }
                        },
                        modifier = Modifier.weight(1f),
                        selected = true
                    )
                }
            }
        }
    }
}

@Composable
fun DayToggle(day: DayOfWeek, isSelected: Boolean, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(2.dp)
            .size(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.name.take(1),
            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
