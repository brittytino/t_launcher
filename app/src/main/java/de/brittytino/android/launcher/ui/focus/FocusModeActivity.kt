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
import java.util.concurrent.TimeUnit
import androidx.compose.material3.HorizontalDivider // Correct import for M3

val PauseIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Pause",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
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

class FocusModeActivity : ComponentActivity() {
    private val viewModel: FocusModeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Full screen setup - allow drawing behind bars to ensure solid color covers everything
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            val isDark = isSystemInDarkTheme()
            val focusBackgroundColor = if (isDark) Color.Black else Color.White
            val contentColor = if (isDark) Color.White else Color.Black
            
            MaterialTheme(
                colorScheme = if (isDark) {
                    darkColorScheme(
                        background = Color.Black,
                        surface = Color.Black,
                        primary = Color(0xFF42A5F5),
                        onBackground = Color.White,
                        onSurface = Color.White
                    )
                } else {
                    lightColorScheme(
                        background = Color.White,
                        surface = Color.White,
                        primary = Color(0xFF42A5F5),
                         onBackground = Color.Black,
                        onSurface = Color.Black
                    )
                }
            ) {
                // Surface ensures background color fills the screen including behind bars
                // Using Box with background explicitly to guarantee coverage
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(focusBackgroundColor)
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
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Turn On Focus Mode?", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.Gray.copy(alpha = 0.2f))
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                     Text("• ${focusState.focusApps.size} apps allowed.", color = Color.White)
                     Text("• Other apps will be blocked.", color = Color.White)
                     if (focusState.isQuietMode) Text("• Notifications from hidden apps silently blocked.", color = Color.White)
                     Text("• Pause allowed once every 15m (max 2m).", color = Color.White)
                     Text("• Authenticaton required to exit.", color = Color.White)
                }

                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                        Text("Turn On Focus", color = Color.Black)
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
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Unlock Focus Mode", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                
                Spacer(Modifier.height(16.dp))
                if (lockType == FocusModeRepository.LockType.RANDOM_STRING) {
                    Text("Type this phrase:", color = Color.Gray)
                    Text(requiredPhrase, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                }

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it; error = false },
                    placeholder = { 
                        Text(if (lockType == FocusModeRepository.LockType.CUSTOM_PASSWORD) "Enter password" else "Enter phrase", color = Color.Gray) 
                    },
                    singleLine = true,
                    visualTransformation = if (lockType == FocusModeRepository.LockType.CUSTOM_PASSWORD && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                    trailingIcon = {
                        if (lockType == FocusModeRepository.LockType.CUSTOM_PASSWORD) {
                            TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                Text(if (passwordVisible) "Hide" else "Show", color = Color.Gray)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color(0xFF5F6368),
                        unfocusedBorderColor = Color(0xFF5F6368)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error) {
                    Text("Incorrect", color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onCancel) { Text("Cancel") }
                    TextButton(onClick = { 
                         val isValid = when(lockType) {
                             FocusModeRepository.LockType.CUSTOM_PASSWORD -> text == customPassword
                             FocusModeRepository.LockType.RANDOM_STRING -> text.trim() == requiredPhrase.trim() // Case sensitive
                             else -> true
                         }
                         if (isValid) onUnlock() else error = true
                    }) { Text("Unlock") }
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
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Set Password", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        if (error != null) error = null 
                    },
                    label = { Text("Password (min 8 chars)", color = Color.Gray) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(if (passwordVisible) "Hide" else "Show", color = Color.Gray)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color(0xFF5F6368),
                        unfocusedBorderColor = Color(0xFF5F6368)
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
                    label = { Text("Confirm password", color = Color.Gray) },
                    singleLine = true,
                    visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { confirmVisible = !confirmVisible }) {
                            Text(if (confirmVisible) "Hide" else "Show", color = Color.Gray)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color(0xFF5F6368),
                        unfocusedBorderColor = Color(0xFF5F6368)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(onClick = { 
                        if (password.length < 8) {
                            error = "Password must be at least 8 characters"
                        } else if (password != confirmPassword) {
                            error = "Passwords do not match"
                        } else {
                            onConfirm(password)
                        }
                    }) { Text("Confirm") }
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Focus Mode", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val buttonText = when(focusState.state) {
                FocusModeRepository.FocusState.ACTIVE -> "Pause Focus"
                FocusModeRepository.FocusState.PAUSED -> "Continue Focus"
                else -> "Start Focus"
            }
            // Use Bold Pause if Active, Play if Paused/Inactive
            val buttonIcon = when(focusState.state) {
                FocusModeRepository.FocusState.ACTIVE -> PauseIcon
                else -> Icons.Filled.PlayArrow
            } 
            val buttonColor = when(focusState.state) {
                FocusModeRepository.FocusState.PAUSED -> Color(0xFFFFB74D) // Orange
                FocusModeRepository.FocusState.ACTIVE -> {
                     // Check cooldown to visuals? 
                     // Maybe dim button if cooldown active?
                     // Currently request says "clearly display... remaining pause cooldown".
                     MaterialTheme.colorScheme.primary
                }
                else -> MaterialTheme.colorScheme.primary
            }
            val textColor = Color.Black

            // Start Focus Card
            Card(
                colors = CardDefaults.cardColors(containerColor = buttonColor),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clickable { onStartFocus() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(buttonText, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = textColor)
                        if (focusState.state == FocusModeRepository.FocusState.INACTIVE) {
                           Text("${appsCount} apps allowed", style = MaterialTheme.typography.bodyMedium, color = textColor.copy(alpha = 0.7f))
                        } else if (focusState.state == FocusModeRepository.FocusState.ACTIVE) {
                           // Show cooldown info if any
                           val now = System.currentTimeMillis()
                           val elapsed = now - focusState.lastPauseTimestamp
                           val cooldown = 15 * 60 * 1000L
                           if (elapsed < cooldown) {
                               val remainingMins = TimeUnit.MILLISECONDS.toMinutes(cooldown - elapsed) + 1
                               Text("Pause cooldown: ${remainingMins}m", style = MaterialTheme.typography.bodyMedium, color = textColor.copy(alpha = 0.7f))
                           } else {
                               Text("Pause available (2m)", style = MaterialTheme.typography.bodyMedium, color = textColor.copy(alpha = 0.7f))
                           }
                        } else if (focusState.state == FocusModeRepository.FocusState.PAUSED) {
                            // Show remaining pause time
                            val secs = TimeUnit.MILLISECONDS.toSeconds(focusState.pauseTimeRemaining)
                            val min = secs / 60
                            val s = secs % 60
                            val timeStr = String.format("%02d:%02d", min, s)
                            Text("Resuming in $timeStr", style = MaterialTheme.typography.bodyMedium, color = textColor.copy(alpha = 0.7f))
                        }
                    }
                    Icon(buttonIcon, contentDescription = null, tint = textColor, modifier = Modifier.size(32.dp))
                }
            }

            if (focusState.state != FocusModeRepository.FocusState.INACTIVE) {
                 OutlinedButton(
                     onClick = onStopFocus, 
                     modifier = Modifier.fillMaxWidth(),
                     border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                     colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                 ) {
                     Text("Stop & Unlock Focus")
                 }
            }

            Text("Focus Settings", color = Color.Gray, style = MaterialTheme.typography.labelLarge)

            // Focus Apps Item
            SettingsItem(
                title = "Focus Apps",
                subtitle = "$appsCount apps allowed",
                icon = Icons.Filled.Home,
                onClick = onOpenAppSelector
            )

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
            
            Text("Focus Lock", color = Color.Gray, style = MaterialTheme.typography.labelLarge)

            // Lock Type Selection
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onSetLockType(FocusModeRepository.LockType.RANDOM_STRING) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = focusState.lockType == FocusModeRepository.LockType.RANDOM_STRING,
                        onClick = { onSetLockType(FocusModeRepository.LockType.RANDOM_STRING) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Random string", fontWeight = FontWeight.SemiBold)
                        Text("Set random phrase to unlock", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f), modifier = Modifier.padding(horizontal = 16.dp))

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onSetLockType(FocusModeRepository.LockType.CUSTOM_PASSWORD) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = focusState.lockType == FocusModeRepository.LockType.CUSTOM_PASSWORD,
                        onClick = { onSetLockType(FocusModeRepository.LockType.CUSTOM_PASSWORD) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Custom password", fontWeight = FontWeight.SemiBold)
                        Text("Set password to unlock", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            Text("Notifications", color = Color.Gray, style = MaterialTheme.typography.labelLarge)
            
            // Quiet Mode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color.Gray)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("Quiet Mode", fontWeight = FontWeight.SemiBold)
                    Text("Stay focused by hiding alerts", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Switch(
                    checked = focusState.isQuietMode,
                    onCheckedChange = onToggleQuietMode
                )
            }
            
            Text(
                "Note: Ensure ‘Hide Notifications’ access is enabled in system settings. Your notifications stay on your device; we never collect or share your data.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun PauseConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Pause Focus Mode", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                Spacer(Modifier.height(16.dp))
                Text(
                    "Focus will pause for a maximum of 2 minutes only. It will automatically resume afterwards.",
                    color = Color.White
                )
                Spacer(Modifier.height(8.dp))
                Text(
                     "Duration: 02:00",
                     style = MaterialTheme.typography.titleMedium,
                     color = Color(0xFFFFB74D),
                     fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Color.Gray)
            Spacer(Modifier.width(16.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = Color.Gray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusAppSelector(
    allApps: List<AbstractDetailedAppInfo>,
    selectedApps: Set<String>,
    onToggleApp: (String, Boolean) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Focus Apps") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(Modifier.padding(padding)) {
            LazyColumn {
                items(allApps) { app ->
                    val packageName = getPackageName(app) ?: return@items
                    val isSelected = selectedApps.contains(packageName)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleApp(packageName, !isSelected) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(app.getLabel(), modifier = Modifier.weight(1f), color = Color.White)
                        Switch(
                            checked = isSelected,
                            onCheckedChange = { onToggleApp(packageName, it) }
                        )
                    }
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
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
