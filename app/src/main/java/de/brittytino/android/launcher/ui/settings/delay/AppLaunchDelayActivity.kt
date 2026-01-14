package de.brittytino.android.launcher.ui.settings.delay

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import de.brittytino.android.launcher.Application
import de.brittytino.android.launcher.R
import de.brittytino.android.launcher.apps.AbstractDetailedAppInfo
import de.brittytino.android.launcher.apps.AppInfo
import de.brittytino.android.launcher.apps.PinnedShortcutInfo
import de.brittytino.android.launcher.data.AppLaunchDelayEntity
import de.brittytino.android.launcher.ui.UIObjectActivity
import de.brittytino.android.launcher.viewmodel.AppLaunchDelayViewModel

class AppLaunchDelayActivity : UIObjectActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Apply theme here? For now just MaterialTheme.
            // Ideally we would bridge existing XML theme to Compose.
            MaterialTheme(
                colorScheme = darkColorScheme() // Enforce dark/strict look?
            ) {
                AppLaunchDelayScreen()
            }
        }
    }
}

val AbstractDetailedAppInfo.packageName: String
    get() {
        val raw = getRawInfo()
        return when(raw) {
            is AppInfo -> raw.packageName
            is PinnedShortcutInfo -> raw.packageName
        }
    }

@Composable
fun AppLaunchDelayScreen(viewModel: AppLaunchDelayViewModel = viewModel()) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val installedApps by app.apps.observeAsState(emptyList())
    val delays by viewModel.delays.collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsState()

    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isEmpty()) {
            installedApps
        } else {
            installedApps.filter { it.getLabel().contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Search apps", color = Color.Gray) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1E1E1E),
                unfocusedContainerColor = Color(0xFF1E1E1E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFFBB86FC),
                focusedIndicatorColor = Color(0xFFBB86FC),
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = MaterialTheme.shapes.medium
        )

        LazyColumn(
             modifier = Modifier.weight(1f)
        ) {
            items(filteredApps, key = { it.packageName }) { appInfo ->
                if (appInfo.packageName.isNotEmpty()) {
                    val delayEntity = delays.find { it.packageName == appInfo.packageName }
                    AppLaunchDelayRow(
                        appInfo = appInfo,
                        delayEntity = delayEntity,
                        onDelayChange = { delay, enabled ->
                           viewModel.updateDelay(appInfo.packageName, delay, enabled)
                        }
                    )
                    HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun AppLaunchDelayRow(
    appInfo: AbstractDetailedAppInfo,
    delayEntity: AppLaunchDelayEntity?,
    onDelayChange: (Int, Boolean) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    val isEnabled = delayEntity?.enabled == true
    val currentDelay = if (isEnabled) delayEntity?.delaySeconds ?: 0 else 0

    if (showDialog) {
        SetDelayDialog(
            initialDelay = currentDelay,
            onDismiss = { showDialog = false },
            onConfirm = { newDelay ->
                 if (newDelay == 0) {
                     onDelayChange(0, false)
                 } else {
                     onDelayChange(newDelay, true)
                 }
                 showDialog = false
            }
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(16.dp)
    ) {
         // Async Icon Loading
        val icon by produceState<androidx.compose.ui.graphics.ImageBitmap?>(initialValue = null, keys = arrayOf(appInfo)) {
             value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                 try {
                     appInfo.getIcon(context).toBitmap().asImageBitmap()
                 } catch (e: Exception) {
                     null
                 }
             }
        }
        
        if (icon != null) {
            Image(
                bitmap = icon!!,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        } else {
            Box(modifier = Modifier.size(48.dp).background(Color.Gray))
        }

        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = appInfo.getLabel(),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Surface(
            color = if (isEnabled) Color(0xFFBB86FC) else Color.DarkGray,
            shape = MaterialTheme.shapes.small,
            onClick = { showDialog = true }
        ) {
            Text(
                text = if (isEnabled) "${currentDelay}s" else "No Delay",
                color = if (isEnabled) Color.Black else Color.White,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun SetDelayDialog(
    initialDelay: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedOption by remember { mutableStateOf(if (listOf(0, 5, 10, 30).contains(initialDelay)) initialDelay else -1) }
    var customText by remember { mutableStateOf(if (selectedOption == -1) initialDelay.toString() else "") }
    var errorText by remember { mutableStateOf<String?>(null) }
    
    // Logic to update state when selecting a preset
    fun selectPreset(value: Int) {
        selectedOption = value
        errorText = null
        if (value == -1) {
             customText = ""
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E1E1E),
        title = {
            Text("Set Launch Delay", color = Color.White)
        },
        text = {
            Column {
                val options = listOf(0 to "No Delay", 5 to "5s", 10 to "10s", 30 to "30s")
                options.forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectPreset(value) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedOption == value),
                            onClick = { selectPreset(value) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFFBB86FC),
                                unselectedColor = Color.Gray
                            )
                        )
                        Text(
                            text = label, 
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                
                // Custom Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectPreset(-1) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (selectedOption == -1),
                        onClick = { selectPreset(-1) },
                         colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFFBB86FC),
                            unselectedColor = Color.Gray
                        )
                    )
                    Text(
                        text = "Custom (s)", 
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                if (selectedOption == -1) {
                    OutlinedTextField(
                        value = customText,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                                customText = newValue
                                errorText = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        singleLine = true,
                        placeholder = { Text("1-60") },
                        isError = errorText != null,
                        supportingText = {
                            if (errorText != null) {
                                Text(errorText!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                         colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent, 
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFFBB86FC),
                            focusedIndicatorColor = Color(0xFFBB86FC),
                            unfocusedIndicatorColor = Color.Gray
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedOption != -1) {
                        onConfirm(selectedOption)
                    } else {
                         val customValue = customText.trim().toIntOrNull()
                         if (customValue == null || customValue !in 1..60) {
                             errorText = "Enter 1-60 seconds"
                         } else {
                             onConfirm(customValue)
                         }
                    }
                }
            ) {
                Text("Apply", color = Color(0xFFBB86FC))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

// Stub components to match design rules (Modified to simpler versions)



