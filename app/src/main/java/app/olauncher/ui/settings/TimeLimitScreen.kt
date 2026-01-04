package app.olauncher.ui.settings

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.olauncher.MainViewModel
import app.olauncher.data.AppModel
import app.olauncher.data.local.RuleEntity
import app.olauncher.ui.theme.TLauncherTypography

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

    Scaffold(
        topBar = {
            SettingsTopBar(onBack)
        }
    ) { padding ->
        if (appList.isNullOrEmpty()) {
             Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                item {
                     Text(
                        "Tap an app to set a daily time limit.",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                val sortedApps = appList!!.sortedBy { it.appLabel }
                items(sortedApps) { app ->
                     // Find if rule exists
                     val rule = rules.find { it.packageName == app.appPackage && it.ruleType == "DAILY" }
                     val limitText = if (rule != null) {
                         // Deserialize simple int from JSON logic or just assume 
                         // For now, let's assume we can parse it if we knew the format. 
                         // But ruleData is JSON. Let's just say "Limit Set" 
                         "Limit Set" 
                     } else ""

                    AppItem(
                        app = app,
                        limitText = limitText,
                        onClick = {
                            selectedApp = app
                            limitInput = "" // Reset or fetch existing
                            showDialog = true
                        }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                }
            }
        }
    }
    
    if (showDialog && selectedApp != null) {
        val currentRule = rules.find { it.packageName == selectedApp!!.appPackage && it.ruleType == "DAILY" }
        
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Set Time Limit for ${selectedApp!!.appLabel}") },
            text = {
                Column {
                    Text("Enter daily limit in minutes:")
                    OutlinedTextField(
                        value = limitInput,
                        onValueChange = { if (it.all { char -> char.isDigit() }) limitInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                    if (currentRule != null) {
                         Text("Current limit is active.", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val limit = limitInput.toIntOrNull()
                        if (limit != null && limit > 0) {
                            viewModel.setAppLimit(selectedApp!!.appPackage, limit)
                            showDialog = false
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                Row {
                    if (currentRule != null) {
                        TextButton(
                            onClick = {
                                viewModel.removeAppLimit(selectedApp!!.appPackage)
                                showDialog = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                        ) { Text("Remove Limit") }
                    }
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            }
        )
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
        // Text-only UI as per request/aesthetic
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.appLabel, 
                style = TLauncherTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = app.appPackage, 
                style = TLauncherTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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

fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }
    val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
