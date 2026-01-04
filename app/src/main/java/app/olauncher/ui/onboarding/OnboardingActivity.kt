package app.olauncher.ui.onboarding

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import app.olauncher.MainActivity
import app.olauncher.helper.PermissionManager
import app.olauncher.ui.theme.TLauncherTheme
import app.olauncher.ui.theme.TLauncherTypography

import app.olauncher.data.Prefs

class OnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val permissionManager = PermissionManager(this)
        val prefs = Prefs(this)

        setContent {
            TLauncherTheme {
                OnboardingScreen(
                    permissionManager = permissionManager,
                    onFinish = {
                        prefs.firstOpen = false // Ensure we don't show again
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun OnboardingScreen(
    permissionManager: PermissionManager,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(0) }
    
    // Permission States - Refresh on Resume
    var isDefaultLauncher by remember { mutableStateOf(false) } // Can't easily check programmatically without hack, assume false strictly for flow or check intent
    var isAccessibilityGranted by remember { mutableStateOf(permissionManager.isAccessibilityServiceEnabled()) }
    var isUsageGranted by remember { mutableStateOf(permissionManager.isUsageStatsGranted()) }
    var isNotificationGranted by remember { mutableStateOf(permissionManager.isNotificationListenerGranted()) }
    var isBatteryIgnored by remember { mutableStateOf(permissionManager.isBatteryOptimizationIgnored()) }
    var isOverlayGranted by remember { mutableStateOf(permissionManager.isOverlayGranted()) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        isAccessibilityGranted = permissionManager.isAccessibilityServiceEnabled()
        isUsageGranted = permissionManager.isUsageStatsGranted()
        isNotificationGranted = permissionManager.isNotificationListenerGranted()
        isBatteryIgnored = permissionManager.isBatteryOptimizationIgnored()
        isOverlayGranted = permissionManager.isOverlayGranted()
        
        // Auto-advance logic could go here, but manual next is safer for user agency
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Spacer(modifier = Modifier.weight(1f))

            when(currentStep) {
                0 -> WelcomeStep()
                1 -> PermissionStep(
                    title = "Set Default Launcher",
                    description = "T Launcher needs to be your home screen to help you focus.",
                    isGranted = false, // Always prompt to check
                    buttonText = "Set Default",
                    onAction = { context.startActivity(permissionManager.getDefaultLauncherIntent()) }
                )
                2 -> PermissionStep(
                    title = "Accessibility Service",
                    description = "Required to block distracting apps and lock your phone. No data leaves your device.",
                    isGranted = isAccessibilityGranted,
                    buttonText = "Grant Accessibility",
                    onAction = { context.startActivity(permissionManager.getAccessibilityIntent()) }
                )
                3 -> PermissionStep(
                    title = "Usage Access",
                    description = "Required to measure your screen time and enforce daily limits.",
                    isGranted = isUsageGranted,
                    buttonText = "Grant Usage Access",
                    onAction = { context.startActivity(permissionManager.getUsageStatsIntent()) }
                )
                  4 -> PermissionStep(
                    title = "Display Over Other Apps",
                    description = "Required to show the strict blocking screen immediately.",
                    isGranted = isOverlayGranted,
                    buttonText = "Grant Overlay",
                    onAction = { context.startActivity(permissionManager.getOverlayIntent()) }
                )
                5 -> PermissionStep(
                    title = "Notification Listener",
                    description = "Required to hide distracting notifications during focus mode.",
                    isGranted = isNotificationGranted,
                    buttonText = "Grant Notification Access",
                    onAction = { context.startActivity(permissionManager.getNotificationListenerIntent()) }
                )
                6 -> PermissionStep(
                    title = "Battery Optimization",
                    description = "Allow T Launcher to run in the background to ensure alarms work reliably.",
                    isGranted = isBatteryIgnored,
                    buttonText = "Ignore Optimization",
                    onAction = { context.startActivity(permissionManager.getBatteryOptimizationIntent()) }
                )
                7 -> FinishStep(onFinish)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Navigation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                 if (currentStep > 0) {
                    TextButton(onClick = { currentStep-- }) {
                        Text("Back", style = TLauncherTypography.labelMedium)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = { currentStep++ },
                    enabled = when(currentStep) {
                        0 -> true
                        1 -> true // Default launcher check is tricky, let user proceed
                        2 -> isAccessibilityGranted
                        3 -> isUsageGranted
                        4 -> isOverlayGranted
                        5 -> isNotificationGranted // Optional? No, strict.
                        6 -> true // Battery can be skipped if user insists or fails
                        else -> false
                    }
                ) {
                    Text(if (currentStep == 7) "Finish" else "Next", style = TLauncherTypography.labelMedium)
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Progress Indicator
            LinearProgressIndicator(
                progress = (currentStep + 1) / 8f,
                modifier = Modifier.fillMaxWidth().height(4.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun WelcomeStep() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Welcome to T Launcher", style = TLauncherTypography.headlineLarge, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "A boring, serious tool for digital minimalism.\n\nTo work correctly, T Launcher needs deep system permissions. We will guide you through setting them up once.",
            style = TLauncherTypography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PermissionStep(
    title: String,
    description: String,
    isGranted: Boolean,
    buttonText: String,
    onAction: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = TLauncherTypography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(description, style = TLauncherTypography.bodyLarge, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        
        if (isGranted) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    "Permission Granted ✓",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            OutlinedButton(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonText)
            }
        }
    }
}

@Composable
fun FinishStep(onFinish: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Setup Complete", style = TLauncherTypography.headlineLarge, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "T Launcher is now ready.\n\nRemember: strictness is a feature, not a bug.",
            style = TLauncherTypography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Enter T Launcher", style = TLauncherTypography.labelMedium)
        }
    }
}
