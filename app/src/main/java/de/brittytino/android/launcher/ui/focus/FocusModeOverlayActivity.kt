package de.brittytino.android.launcher.ui.focus

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import de.brittytino.android.launcher.data.FocusModeRepository
import de.brittytino.android.launcher.viewmodel.FocusModeViewModel
import de.brittytino.android.launcher.ui.settings.SettingsTheme

class FocusModeOverlayActivity : ComponentActivity() {
    private val viewModel: FocusModeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Full screen setup - allow drawing behind bars to ensure solid color covers everything
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Block back button
            }
        })

        setContent {
            SettingsTheme {
                FocusOverlayScreen(
                    viewModel = viewModel,
                    onUnlockSuccess = {
                        viewModel.stopFocus()
                        finish()
                    },
                    onCancel = {
                        // Go back to home (which is filtered) instead of staying on overlay?
                        // Or just minimize?
                        // For strictness, if they launched a blocked app, we stay here until they go home.
                        // But we can just launch Home.
                        val homeIntent = android.content.Intent(android.content.Intent.ACTION_MAIN)
                        homeIntent.addCategory(android.content.Intent.CATEGORY_HOME)
                        homeIntent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(homeIntent)
                    }
                )
            }
        }
    }
}

@Composable
fun FocusOverlayScreen(
    viewModel: FocusModeViewModel,
    onUnlockSuccess: () -> Unit,
    onCancel: () -> Unit // Used if user cancels UNLOCK_PENDING
) {
    val focusState by viewModel.focusState.collectAsState()
    val unlockPhrase by viewModel.unlockPhrase.collectAsState()
    
    // Auto-finish if inactive
    LaunchedEffect(focusState.state) {
        if (focusState.state == FocusModeRepository.FocusState.INACTIVE) {
            onUnlockSuccess()
        }
    }

    // Main Content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        when (focusState.state) {
            FocusModeRepository.FocusState.ACTIVE -> {
                FocusActiveScreen(
                    onUnlockRequest = { viewModel.requestUnlock() }
                )
            }
            FocusModeRepository.FocusState.UNLOCK_PENDING -> {
                FocusUnlockScreen(
                    lockType = focusState.lockType,
                    requiredPhrase = unlockPhrase,
                    customPassword = focusState.customPassword,
                    onUnlock = { viewModel.stopFocus() },
                    onCancel = { viewModel.cancelUnlock() }
                )
            }
            else -> {
                // Should not happen here ideally, or showing spinner waiting for finish
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun FocusActiveScreen(onUnlockRequest: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            // Use a built-in icon or painter if available, or just Text for now to be safe
            // Icons.Filled.Lock is material icons core usually.
            // Let's safe-play with Text or a simple circle
            imageVector = androidx.compose.material.icons.Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Focus Mode Active",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Access to this app is restricted.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onUnlockRequest,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Unlock", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun FocusUnlockScreen(
    lockType: FocusModeRepository.LockType,
    requiredPhrase: String,
    customPassword: String?,
    onUnlock: () -> Unit,
    onCancel: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Unlock Focus Mode",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (lockType == FocusModeRepository.LockType.RANDOM_STRING) {
            Text(
                "Type the following phrase:",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                requiredPhrase,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        } else if (lockType == FocusModeRepository.LockType.CUSTOM_PASSWORD) {
            Text(
                "Enter your password:",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { 
                input = it
                error = false
            },
            singleLine = true,
            isError = error,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        if (error) {
            Text(
                text = "Incorrect, try again.", 
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row {
            TextButton(onClick = onCancel) {
                Text("Cancel", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = {
                    val isValid = when (lockType) {
                        FocusModeRepository.LockType.RANDOM_STRING -> input.trim() == requiredPhrase.trim()
                        FocusModeRepository.LockType.CUSTOM_PASSWORD -> input == customPassword
                        else -> false
                    }
                    if (isValid) {
                        onUnlock()
                    } else {
                        error = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Unlock", color = MaterialTheme.colorScheme.onError)
            }
        }
    }
}
