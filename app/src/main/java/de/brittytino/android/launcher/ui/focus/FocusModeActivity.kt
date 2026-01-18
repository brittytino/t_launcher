package de.brittytino.android.launcher.ui.focus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.foundation.isSystemInDarkTheme // Add import
import androidx.compose.foundation.background // Add import
import androidx.compose.foundation.layout.Box // Add import
import androidx.compose.foundation.layout.fillMaxSize // Add import
import androidx.compose.foundation.layout.systemBarsPadding // Add import
import androidx.compose.material3.* // Ensure M3 imports
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import de.brittytino.android.launcher.apps.AbstractDetailedAppInfo
import de.brittytino.android.launcher.apps.AppInfo
import de.brittytino.android.launcher.apps.PinnedShortcutInfo
import de.brittytino.android.launcher.data.FocusModeRepository
import de.brittytino.android.launcher.viewmodel.FocusModeViewModel
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.path
import androidx.activity.compose.BackHandler
import android.content.Intent
import de.brittytino.android.launcher.ui.HomeActivity
import de.brittytino.android.launcher.ui.settings.SettingsTheme
import de.brittytino.android.launcher.ui.settings.SettingsScaffold
import de.brittytino.android.launcher.ui.settings.SettingsSectionHeader
import de.brittytino.android.launcher.ui.settings.SettingsCard
import de.brittytino.android.launcher.ui.settings.SettingsItem
import de.brittytino.android.launcher.ui.settings.SettingsToggle
import de.brittytino.android.launcher.ui.settings.IconArrow
import java.util.concurrent.TimeUnit
import androidx.compose.material3.HorizontalDivider // Correct import for M3

@Composable
fun PauseIcon(): ImageVector {
    val color = MaterialTheme.colorScheme.onSurface
    return remember(color) {
        ImageVector.Builder(
            name = "Pause",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(color)) {
                moveTo(6f, 19f)
                horizontalLineToRelative(4f)
                verticalLineTo(5f)
                horizontalLineTo(6f)
                verticalLineToRelative(14f)
                close()
                moveTo(14f, 5f)
                verticalLineToRelative(14f)
                horizontalLineToRelative(4f)
                verticalLineTo(5f)
                horizontalLineToRelative(-4f)
                close()
            }
        }.build()
    }
}



class FocusModeActivity : ComponentActivity() {
    private val viewModel: FocusModeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Full screen setup - allow drawing behind bars to ensure solid color covers everything
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            SettingsTheme {
                // Surface ensures background color fills the screen including behind bars
                // Using Box with background explicitly to guarantee coverage
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Content is padded to avoid overlap with system bars, but background remains full
                    Box(modifier = Modifier.systemBarsPadding()) {
                        FocusModeScreen(viewModel, onBack = { finish() })
                    }
                }
            }
        }
    }
}

@Composable
fun FocusModeScreen(viewModel: FocusModeViewModel, onBack: () -> Unit) {
    var showAppSelector by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showPauseDialog by remember { mutableStateOf(false) }
    var showStartConfirmation by remember { mutableStateOf(false) }
    
    val focusState by viewModel.focusState.collectAsState()
    val allApps by viewModel.appList.observeAsState(emptyList())
    val unlockPhrase by viewModel.unlockPhrase.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Navigation Flow: Active/Paused state should route Back to Home (Launcher)
    // Not settings.
    BackHandler(enabled = true) {
        if (showAppSelector) {
            showAppSelector = false
        } else if (focusState.state != FocusModeRepository.FocusState.INACTIVE) {
            // "Pressing back should never reveal launcher settings until Focus is fully turned off"
            // We launch HomeActivity which is our launcher surface.
            val intent = Intent(context, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(intent)
        } else {
           // Inactive: Exit to caller (Settings)
           onBack()
        }
    }

    // Handle UNLOCK_PENDING state
    if (focusState.state == FocusModeRepository.FocusState.UNLOCK_PENDING) {
        UnlockDialog(
            lockType = focusState.lockType,
            requiredPhrase = unlockPhrase,
            customPassword = focusState.customPassword,
            onUnlock = { viewModel.stopFocus() },
            onCancel = { viewModel.cancelUnlock() }
        )
    }

    if (showAppSelector) {
        FocusAppSelector(
            allApps = allApps,
            selectedApps = focusState.focusApps,
            onToggleApp = { pkg, selected -> viewModel.updateFocusApps(pkg, selected) },
            onBack = { showAppSelector = false }
        )
    } else {
        FocusModeDashboard(
            focusState = focusState,
            appsCount = focusState.focusApps.size,
            onOpenAppSelector = { showAppSelector = true },
            onToggleQuietMode = { viewModel.setQuietMode(it) },
            onSetLockType = { 
                viewModel.setLockType(it) 
                if (it == FocusModeRepository.LockType.CUSTOM_PASSWORD && focusState.customPassword.isNullOrEmpty()) {
                    showPasswordDialog = true
                }
            },
            onStartFocus = { 
                when (focusState.state) {
                    FocusModeRepository.FocusState.INACTIVE -> {
                         // Check prerequisites first
                         if (focusState.lockType == FocusModeRepository.LockType.CUSTOM_PASSWORD && focusState.customPassword.isNullOrEmpty()) {
                             showPasswordDialog = true
                         } else {
                             showStartConfirmation = true
                         }
                    }
                    FocusModeRepository.FocusState.ACTIVE -> {
                        val cooldown = viewModel.getPauseCooldownCallback()
                        if (cooldown == 0L) {
                             showPauseDialog = true
                        } else {
                             // "Inline message"
                             // We don't have a snackbar host here easily, so we rely on UI status text 
                             // which we will add to dashboard or show a quick Toast?
                             // Request says "Show an inline message".
                             // Let's rely on dashboard text update but also Toast for feedback.
                             android.widget.Toast.makeText(context, "Pause available in ${TimeUnit.MILLISECONDS.toMinutes(cooldown) + 1}m", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                    FocusModeRepository.FocusState.PAUSED -> viewModel.resumeFocus()
                    else -> {}
                }
            },
            onStopFocus = { viewModel.requestUnlock() },
            onBack = {
                // If active, go Home. If inactive, finish.
                if (focusState.state != FocusModeRepository.FocusState.INACTIVE) {
                    val intent = Intent(context, HomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    context.startActivity(intent)
                } else {
                    onBack()
                }
            }
        )
        
        if (showStartConfirmation) {
            StartFocusConfirmationDialog(
                focusState = focusState,
                onConfirm = {
                    val result = viewModel.confirmStartFocus()
                    if (result == null) {
                         showStartConfirmation = false
                         // Transition to Home immediately? "Focus Mode panel remains the primary surface"
                         // Actually "Start Focus" usually launches the mode.
                         // But the requirements say "Focus Mode panel remains the primary surface...".
                         // So we stay here.
                    } else {
                         // Error (e.g. password lost)
                         showStartConfirmation = false
                    }
                },
                onDismiss = { showStartConfirmation = false }
            )
        }

        if (showPasswordDialog) {
            SetPasswordDialog(
                onConfirm = { 
                    viewModel.setCustomPassword(it)
                    showPasswordDialog = false
                },
                onDismiss = { showPasswordDialog = false }
            )
        }
        
        if (showPauseDialog) {
            PauseConfirmationDialog(
                onConfirm = {
                    viewModel.pauseFocus()
                    showPauseDialog = false
                },
                onDismiss = { showPauseDialog = false }
            )
        }
    }
}

@Composable
fun StartFocusConfirmationDialog(focusState: FocusModeViewModel.FocusStateModel, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Turn On Focus Mode?", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                     Text("• ${focusState.focusApps.size} apps allowed.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                     Text("• Other apps will be blocked.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                     if (focusState.isQuietMode) Text("• Notifications from hidden apps silently blocked.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                     Text("• Pause allowed once every 15m (max 2m).", color = MaterialTheme.colorScheme.onSurfaceVariant)
                     Text("• Authenticaton required to exit.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = MaterialTheme.colorScheme.primary) }
                    Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                        Text("Turn On Focus", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun UnlockDialog(
    lockType: FocusModeRepository.LockType,
    requiredPhrase: String,
    customPassword: String?,
    onUnlock: () -> Unit,
    onCancel: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onCancel) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Unlock Focus Mode", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Spacer(Modifier.height(16.dp))
                if (lockType == FocusModeRepository.LockType.RANDOM_STRING) {
                    Text("Type this phrase:", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    Text(requiredPhrase, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                }

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it; error = false },
                    placeholder = { 
                        Text(if (lockType == FocusModeRepository.LockType.CUSTOM_PASSWORD) "Enter password" else "Enter phrase", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) 
                    },
                    singleLine = true,
                    visualTransformation = if (lockType == FocusModeRepository.LockType.CUSTOM_PASSWORD && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                    trailingIcon = {
                        if (lockType == FocusModeRepository.LockType.CUSTOM_PASSWORD) {
                            TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                Text(if (passwordVisible) "Hide" else "Show", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error) {
                    Text("Incorrect", color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onCancel) { Text("Cancel", color = MaterialTheme.colorScheme.primary) }
                    TextButton(onClick = { 
                         val isValid = when(lockType) {
                             FocusModeRepository.LockType.CUSTOM_PASSWORD -> text == customPassword
                             FocusModeRepository.LockType.RANDOM_STRING -> text.trim() == requiredPhrase.trim() // Case sensitive
                             else -> true
                         }
                         if (isValid) onUnlock() else error = true
                    }) { Text("Unlock", color = MaterialTheme.colorScheme.primary) }
                }
            }
        }
    }
}




@Composable
fun SetPasswordDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Set Password", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        if (error != null) error = null 
                    },
                    label = { Text("Password (min 8 chars)", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.7f)) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(if (passwordVisible) "Hide" else "Show", color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        if (error != null) error = null 
                    },
                    label = { Text("Confirm password", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    singleLine = true,
                    visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { confirmVisible = !confirmVisible }) {
                            Text(if (confirmVisible) "Hide" else "Show", color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = MaterialTheme.colorScheme.primary) }
                    TextButton(onClick = { 
                        if (password.length < 8) {
                            error = "Password must be at least 8 characters"
                        } else if (password != confirmPassword) {
                            error = "Passwords do not match"
                        } else {
                            onConfirm(password)
                        }
                    }) { Text("Confirm", color = MaterialTheme.colorScheme.primary) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusModeDashboard(
    focusState: FocusModeViewModel.FocusStateModel,
    appsCount: Int,
    onOpenAppSelector: () -> Unit,
    onToggleQuietMode: (Boolean) -> Unit,
    onSetLockType: (FocusModeRepository.LockType) -> Unit,
    onStartFocus: () -> Unit,
    onStopFocus: () -> Unit,
    onBack: () -> Unit
) {
    SettingsScaffold {
        // Title
        Text(
            "Focus Mode", 
            style = MaterialTheme.typography.headlineMedium, 
            fontWeight = FontWeight.Bold, 
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
        )

        // Status Card
        SettingsCard {
            val buttonText = when(focusState.state) {
                FocusModeRepository.FocusState.ACTIVE -> "Pause Focus"
                FocusModeRepository.FocusState.PAUSED -> "Continue Focus"
                else -> "Start Focus"
            }
            val buttonColor = when(focusState.state) {
                FocusModeRepository.FocusState.PAUSED -> Color(0xFFFFB74D) // Orange
                FocusModeRepository.FocusState.ACTIVE -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.primary
            }
            val contentColor = when(focusState.state) {
                 FocusModeRepository.FocusState.PAUSED -> Color.Black
                 FocusModeRepository.FocusState.ACTIVE -> MaterialTheme.colorScheme.onSecondaryContainer
                 else -> MaterialTheme.colorScheme.onPrimary
            }
            
            // Main Action Button
             Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(buttonColor)
                    .clickable { onStartFocus() }
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if(focusState.state == FocusModeRepository.FocusState.ACTIVE) PauseIcon() else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = contentColor
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(buttonText, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = contentColor)
                    if (focusState.state == FocusModeRepository.FocusState.ACTIVE) {
                           val now = System.currentTimeMillis()
                           val elapsed = now - focusState.lastPauseTimestamp
                           val cooldown = 15 * 60 * 1000L
                           if (elapsed < cooldown) {
                               val remainingMins = TimeUnit.MILLISECONDS.toMinutes(cooldown - elapsed) + 1
                               Text("Pause cooldown: ${remainingMins}m", style = MaterialTheme.typography.bodyMedium, color = contentColor.copy(alpha = 0.7f))
                           } else {
                               Text("Pause available (2m)", style = MaterialTheme.typography.bodyMedium, color = contentColor.copy(alpha = 0.7f))
                           }
                    } else if (focusState.state == FocusModeRepository.FocusState.PAUSED) {
                        val secs = TimeUnit.MILLISECONDS.toSeconds(focusState.pauseTimeRemaining)
                        val min = secs / 60
                        val s = secs % 60
                        val timeStr = String.format("%02d:%02d", min, s)
                        Text("Resuming in $timeStr", style = MaterialTheme.typography.bodyMedium, color = contentColor)
                    } else {
                        Text("Break the loop", style = MaterialTheme.typography.bodyMedium, color = contentColor.copy(alpha = 0.7f))
                    }
                }
            }
        }
        
        if (focusState.state != FocusModeRepository.FocusState.INACTIVE) {
             Spacer(Modifier.height(16.dp))
             SettingsCard {
                 SettingsItem(
                     title = "Stop Focus Mode",
                     textColor = MaterialTheme.colorScheme.error,
                     onClick = onStopFocus
                 )
             }
        }

        SettingsSectionHeader("Configuration")

        SettingsCard {
            SettingsItem(
                title = "Focus Apps",
                subtitle = "$appsCount apps allowed",
                onClick = onOpenAppSelector,
                action = { IconArrow() }
            )
            
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            SettingsItem(
                title = "Quiet Mode",
                subtitle = "Collapse notification shade",
                action = { 
                    SettingsToggle(
                        checked = focusState.isQuietMode, 
                        onCheckedChange = onToggleQuietMode
                    )
                }
            )
        }
        
        SettingsSectionHeader("Lock Method")
        SettingsCard {
            SettingsItem(
                title = "Random Phrase",
                onClick = { onSetLockType(FocusModeRepository.LockType.RANDOM_STRING) },
                action = { SettingsToggle(checked = focusState.lockType == FocusModeRepository.LockType.RANDOM_STRING, onCheckedChange = { onSetLockType(FocusModeRepository.LockType.RANDOM_STRING) }) }
            )
            SettingsItem(
                title = "Password",
                subtitle = if (focusState.lockType == FocusModeRepository.LockType.CUSTOM_PASSWORD && !focusState.customPassword.isNullOrEmpty()) "Password set" else "Setup required",
                onClick = { onSetLockType(FocusModeRepository.LockType.CUSTOM_PASSWORD) },
                action = { SettingsToggle(checked = focusState.lockType == FocusModeRepository.LockType.CUSTOM_PASSWORD, onCheckedChange = { onSetLockType(FocusModeRepository.LockType.CUSTOM_PASSWORD) }) }
            )
        }
    }
}

@Composable
fun PauseConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Pause Focus Mode", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                Text(
                    "Focus will pause for a maximum of 2 minutes only. It will automatically resume afterwards.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                     "Duration: 02:00",
                     style = MaterialTheme.typography.titleMedium,
                     color = Color(0xFFFFB74D), // Keep Orange for warning
                     fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = MaterialTheme.colorScheme.primary) }
                    Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D))) {
                        Text("Start Pause (2m)", color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsItem(title: String, subtitle: String, icon: ImageVector?, onClick: () -> Unit) {
    // Deprecated: Removed in favor of SettingsDesignSystem
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusAppSelector(
    allApps: List<AbstractDetailedAppInfo>,
    selectedApps: Set<String>,
    onToggleApp: (String, Boolean) -> Unit,
    onBack: () -> Unit
) {
    SettingsScaffold {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                "Select Apps",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Content
        Card(
             colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
             modifier = Modifier.weight(1f).fillMaxWidth(),
             shape = RoundedCornerShape(16.dp)
        ) {
            LazyColumn {
                items(allApps) { app ->
                    val packageName = getPackageName(app) ?: return@items
                    val isSelected = selectedApps.contains(packageName)
                    
                    SettingsItem(
                        title = app.getLabel(),
                        subtitle = null,
                        icon = null, 
                        action = {
                            SettingsToggle(
                                checked = isSelected,
                                onCheckedChange = { onToggleApp(packageName, it) }
                            )
                        },
                        onClick = { onToggleApp(packageName, !isSelected) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                }
            }
        }
    }
}

fun getPackageName(info: AbstractDetailedAppInfo): String? {
    val raw = info.getRawInfo()
    return when (raw) {
        is AppInfo -> raw.packageName
        is PinnedShortcutInfo -> raw.packageName
        else -> null
    }
}
