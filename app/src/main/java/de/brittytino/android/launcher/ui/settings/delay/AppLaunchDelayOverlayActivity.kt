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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import de.brittytino.android.launcher.Application
import de.brittytino.android.launcher.R
import de.brittytino.android.launcher.apps.AbstractDetailedAppInfo
import de.brittytino.android.launcher.apps.AppInfo
import de.brittytino.android.launcher.ui.UIObjectActivity
import kotlinx.coroutines.delay

import androidx.compose.runtime.saveable.rememberSaveable

import de.brittytino.android.launcher.ui.settings.SettingsTheme

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
            SettingsTheme {
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
    
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val appInfo = remember(packageName) {
        app.apps.value?.find { (it.getRawInfo() as? AppInfo)?.packageName == packageName }
    }

    LaunchedEffect(key1 = true) {
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
    }

    val progress by animateFloatAsState(
        targetValue = if (initialDelaySeconds > 0) timeLeft.toFloat() / initialDelaySeconds.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1000, easing = androidx.compose.animation.core.LinearEasing),
        label = "ProgressAnimation"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 40.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Header: App Icon & Name
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (appInfo != null) {
                        Image(
                            bitmap = appInfo.getIcon(context).toBitmap().asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = appInfo.getLabel(),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    } else {
                         Box(modifier = Modifier.size(72.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha=0.2f), CircleShape))
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Take a breath.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.6f)
                    )
                }

                // Countdown Circle
                 Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { 1f }, // Track
                        modifier = Modifier.size(160.dp),
                        strokeWidth = 12.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        trackColor = Color.Transparent,
                    )
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(160.dp),
                        strokeWidth = 12.dp,
                        color = MaterialTheme.colorScheme.primary,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$timeLeft",
                            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "seconds",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.6f)
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha=0.5f))
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = onComplete,
                        enabled = timeLeft == 0,
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha=0.12f)
                        )
                    ) {
                        Text(if (timeLeft > 0) "Wait" else "Open")
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
