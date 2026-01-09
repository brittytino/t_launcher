package app.olauncher.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import app.olauncher.MainViewModel
import app.olauncher.data.AppModel
import app.olauncher.ui.theme.*

@Composable
fun TimeLimitScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val appList by viewModel.appList.observeAsState(initial = emptyList())
    val rules by viewModel.allRules.observeAsState(initial = emptyList())
    
    var showDialog by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf<AppModel?>(null) }
    var limitInput by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        if (appList.isNullOrEmpty()) {
            viewModel.getAppList()
        }
    }

    TScaffold {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                 IconButton(onClick = onBack) {
                     Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                 }
                 Text("TIME LIMITS", style = TLauncherTypography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            }

            if (appList.isNullOrEmpty()) {
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                TCard(modifier = Modifier.fillMaxWidth()) {
                    LazyColumn {
                        item {
                             Text(
                                "Tap an app to set a daily time limit.",
                                modifier = Modifier.padding(16.dp),
                                style = TLauncherTypography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        val sortedApps = appList!!.sortedBy { it.appLabel }
                        items(sortedApps) { app ->
                             val rule = rules.find { it.packageName == app.appPackage && it.ruleType == "DAILY" }
                             val limitText = if (rule != null) "Limit Set" else ""
        
                            AppItem(
                                app = app,
                                limitText = limitText,
                                onClick = {
                                    selectedApp = app
                                    limitInput = "" 
                                    showDialog = true
                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.surface, thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
    
    if (showDialog && selectedApp != null) {
        val currentRule = rules.find { it.packageName == selectedApp!!.appPackage && it.ruleType == "DAILY" }
        
        Dialog(onDismissRequest = { showDialog = false }) {
            TCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SET LIMIT", style = TLauncherTypography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(selectedApp!!.appLabel, style = TLauncherTypography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Minutes per day:", style = TLauncherTypography.labelSmall)
                    OutlinedTextField(
                        value = limitInput,
                        onValueChange = { if (it.all { char -> char.isDigit() }) limitInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                         TChip(
                             text = "CANCEL", 
                             onClick = { showDialog = false }, 
                             modifier = Modifier.weight(1f),
                             selected = false
                         )
                         TChip(
                             text = "SAVE", 
                             onClick = {
                                val limit = limitInput.toIntOrNull()
                                if (limit != null && limit > 0) {
                                    viewModel.setAppLimit(selectedApp!!.appPackage, limit)
                                    showDialog = false
                                }
                             }, 
                             modifier = Modifier.weight(1f),
                             selected = true
                         )
                    }
                    
                    if (currentRule != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        TChip(
                             text = "REMOVE LIMIT", 
                             onClick = {
                                viewModel.removeAppLimit(selectedApp!!.appPackage)
                                showDialog = false
                             }, 
                             modifier = Modifier.fillMaxWidth(),
                             selected = false
                         )
                    }
                }
            }
        }
    }
}

@Composable
fun AppItem(app: AppModel, limitText: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.appLabel, 
                style = TLauncherTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (limitText.isNotEmpty()) {
             Text(
                text = limitText,
                style = TLauncherTypography.labelMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
