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
import de.brittytino.android.launcher.Application
import de.brittytino.android.launcher.R
import de.brittytino.android.launcher.apps.AbstractDetailedAppInfo
import de.brittytino.android.launcher.apps.AppInfo
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
            placeholder = { Text("Search apps") },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
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
                if (icon != null) {
                    Image(
                        bitmap = icon.toBitmap().asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
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
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TChip(
                text = "10s",
                selected = currentDelay == 10,
                onClick = { onDelaySelected(10) }
            )
            TChip(
                text = "30s",
                selected = currentDelay == 30,
                onClick = { onDelaySelected(30) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("Custom: ${currentDelay}s", color = Color.Gray)
        Slider(
            value = currentDelay.toFloat().coerceIn(0f, 60f),
            onValueChange = { onDelaySelected(it.toInt()) },
            valueRange = 0f..60f,
            steps = 59
        )
    }
}

// Stub components to match design rules
@Composable
fun TCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
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
            selectedContainerColor = Color(0xFFBB86FC),
            selectedLabelColor = Color.Black
        )
    )
}

@Composable
fun TSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color(0xFFBB86FC),
            checkedTrackColor = Color(0xFF3700B3)
        )
    )
}
