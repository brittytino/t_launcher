package de.brittytino.android.launcher.ui.settings.delay

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.os.Bundle
import android.os.UserHandle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import de.brittytino.android.launcher.Application
import de.brittytino.android.launcher.R
import de.brittytino.android.launcher.apps.AbstractDetailedAppInfo
import de.brittytino.android.launcher.ui.UIObjectActivity
import kotlinx.coroutines.delay

import androidx.compose.runtime.saveable.rememberSaveable

class AppLaunchDelayOverlayActivity : UIObjectActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val packageName = intent.getStringExtra("EXTRA_PACKAGE_NAME")
        val delaySeconds = intent.getIntExtra("EXTRA_DELAY_SECONDS", 0)
        val userHandle = intent.getParcelableExtra<UserHandle>("EXTRA_USER_HANDLE")
        val componentName = intent.getParcelableExtra<ComponentName>("EXTRA_COMPONENT_NAME")
        val sourceRect = intent.getParcelableExtra<Rect>("EXTRA_SOURCE_RECT")

        if (packageName == null || delaySeconds <= 0) {
            finish()
            return
        }

        // Prevent back press from doing anything default? 
        // Request says: "Back button -> Cancel"
        // UIObjectActivity handles back press? We can override onBackPressed or use BackHandler in Compose.

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                DelayOverlayScreen(
                    packageName = packageName,
                    initialDelaySeconds = delaySeconds,
                    onCancel = { finish() },
                    onComplete = {
                        launchApp(packageName, userHandle, componentName, sourceRect)
                        finish()
                    }
                )
            }
        }
    }
    
    // Manual handling of launch logic replicated from AppAction
    private fun launchApp(
        packageName: String, 
        user: UserHandle?, 
        component: ComponentName?, 
        rect: Rect?
    ) {
        val context = this
        try {
            if (user != null && component != null) {
                val launcherApps = context.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps
                 launcherApps.startMainActivity(component, user, rect, null)
            } else {
                 context.packageManager.getLaunchIntentForPackage(packageName)?.let {
                    it.addCategory(Intent.CATEGORY_LAUNCHER)
                    context.startActivity(it)
                }
            }
        } catch (e: Exception) {
             Toast.makeText(context, "Failed to launch app", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun DelayOverlayScreen(
    packageName: String,
    initialDelaySeconds: Int,
    onCancel: () -> Unit,
    onComplete: () -> Unit
) {
    var timeLeft by rememberSaveable { mutableStateOf(initialDelaySeconds) }
    
    // Safety: App Icon Loading
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val appInfo = remember(packageName) {
        // This is inefficient but we need to find the app info.
        // Assuming apps are loaded.
        app.apps.value?.find { it.packageName == packageName }
    }

    LaunchedEffect(key1 = true) {
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Icon
                if (appInfo != null) {
                    Image(
                        bitmap = appInfo.getIcon(context).toBitmap().asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = appInfo.getLabel(),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                } else {
                     // Fallback
                     Text(text = "App", style = MaterialTheme.typography.titleLarge)
                }

                // Digital Wellbeing Message
                Text(
                    text = "Pause. Decide if this is worth it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                // Countdown
                 Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { timeLeft.toFloat() / initialDelaySeconds.toFloat() },
                        modifier = Modifier.size(120.dp),
                        strokeWidth = 8.dp,
                        color = Color(0xFFBB86FC),
                        trackColor = Color.DarkGray,
                    )
                    Text(
                        text = "$timeLeft",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White
                    )
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = onComplete,
                        enabled = timeLeft == 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFBB86FC),
                            disabledContainerColor = Color.DarkGray
                        )
                    ) {
                        Text(if (timeLeft > 0) "Wait" else "Open App")
                    }
                }
            }
        }
    }
    
    // Back Handler
    androidx.activity.compose.BackHandler {
        onCancel()
    }
}
