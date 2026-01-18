package de.brittytino.android.launcher.ui.settings.delay

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import de.brittytino.android.launcher.Application
import de.brittytino.android.launcher.R
import de.brittytino.android.launcher.apps.AbstractDetailedAppInfo
import de.brittytino.android.launcher.apps.AppInfo
import de.brittytino.android.launcher.data.AppLaunchDelayEntity
import de.brittytino.android.launcher.ui.UIObjectActivity
import de.brittytino.android.launcher.viewmodel.AppLaunchDelayViewModel
import de.brittytino.android.launcher.ui.settings.SettingsTheme

class AppLaunchDelayActivity : UIObjectActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Full screen setup
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            SettingsTheme {
                 Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Box(modifier = Modifier.systemBarsPadding()) {
                        AppLaunchDelayScreen(onBack = { finish() })
                    }
                }
            }
        }
    }
}

@Composable
fun AppLaunchDelayScreen(viewModel: AppLaunchDelayViewModel = viewModel(), onBack: () -> Unit) {
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

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Search apps") },
            leadingIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack, 
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )

        LazyColumn {
            items(filteredApps, key = { (it.getRawInfo() as? AppInfo)?.packageName ?: "" }) { appInfo ->
                val delayEntity = delays.find { it.packageName == (appInfo.getRawInfo() as? AppInfo)?.packageName }
                AppLaunchDelayRow(
                    appInfo = appInfo,
                    delayEntity = delayEntity,
                    onDelayChange = { delay, enabled ->
                        (appInfo.getRawInfo() as? AppInfo)?.packageName?.let { packageName ->
                            viewModel.updateDelay(packageName, delay, enabled)
                        }
                    }
                )
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
    var expanded by remember { mutableStateOf(delayEntity?.enabled == true) }

    // Sync expanded with enabled state if it changes externally?
    // Use the entity state primarily.
    val isEnabled = delayEntity?.enabled == true
    val currentDelay = delayEntity?.delaySeconds ?: 0
    val context = LocalContext.current

    TCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Convert Drawable to Bitmap for Image
                val icon = remember(appInfo) {
                    // This is expensive on UI thread. Should be async.
                    // But for now, direct access.
                    appInfo.getIcon(context)
                }

                // Assuming appInfo.icon is a Drawable
                // Compose Image needs ImageBitmap or Painter
                Image(
                    bitmap = icon.toBitmap().asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = appInfo.getLabel(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                TSwitch(
                    checked = isEnabled,
                    onCheckedChange = { checked ->
                        val newDelay = if (checked && currentDelay == 0) 10 else currentDelay
                        onDelayChange(newDelay, checked)
                        expanded = checked // Show options if enabled
                    }
                )
            }

            if (isEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                DelaySelector(
                    currentDelay = currentDelay,
                    onDelaySelected = { newDelay ->
                        onDelayChange(newDelay, true)
                    }
                )
            }
        }
    }
}

@Composable
fun DelaySelector(
    currentDelay: Int,
    onDelaySelected: (Int) -> Unit
) {
    var rawInput by remember(currentDelay) { mutableStateOf(currentDelay.toString()) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TChip(
                text = "10s",
                selected = currentDelay == 10,
                onClick = { onDelaySelected(10); rawInput = "10" }
            )
            TChip(
                text = "30s",
                selected = currentDelay == 30,
                onClick = { onDelaySelected(30); rawInput = "30" }
            )
            TChip(
                text = "1m",
                selected = currentDelay == 60,
                onClick = { onDelaySelected(60); rawInput = "60" }
            )
             TChip(
                text = "5m",
                selected = currentDelay == 300,
                onClick = { onDelaySelected(300); rawInput = "300" }
            )
        }

        OutlinedTextField(
            value = rawInput,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    rawInput = newValue
                    val intVal = newValue.toIntOrNull()
                    if (intVal != null) {
                         // Clamp to 3600
                         onDelaySelected(intVal.coerceAtMost(3600))
                    }
                }
            },
            label = { Text("Custom Seconds (Max 3600)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = Color.Gray,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        Slider(
            value = currentDelay.toFloat().coerceIn(0f, 3600f),
            onValueChange = { 
                val i = it.toInt()
                onDelaySelected(i)
                rawInput = i.toString()
            },
            valueRange = 0f..3600f,
            steps = 0 // Continuous or too many steps
        )
    }
}

// Stub components to match design rules
@Composable
fun TCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        content()
    }
}

@Composable
fun TChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun TSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.primary,
            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}
