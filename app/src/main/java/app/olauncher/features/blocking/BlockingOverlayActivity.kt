package app.olauncher.features.blocking

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.olauncher.R
import app.olauncher.ui.theme.TLauncherTheme
import app.olauncher.ui.theme.TLauncherTypography

class BlockingOverlayActivity : androidx.activity.ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Make it hard to dismiss
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        
        // Prevent back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing, or show toast "Blocked"
            }
        })

        setContent {
            BlockingScreen(
                onGoHome = {
                    val startMain = Intent(Intent.ACTION_MAIN)
                    startMain.addCategory(Intent.CATEGORY_HOME)
                    startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(startMain)
                }
            )
        }
    }

    override fun onPause() {
        super.onPause()
        // If we leave this activity for any reason other than going HOME, we might want to reappear?
        // Actually, the Service handles the enforcement. This activity is just the view.
    }
}

@Composable
fun BlockingScreen(onGoHome: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val reason = remember { 
        (context as? Activity)?.intent?.getStringExtra("BLOCKING_REASON") ?: "Restricted"
    }
    
    val canExtend = remember {
        (context as? Activity)?.intent?.getBooleanExtra("CAN_EXTEND", false) == true
    }
    
    val isSessionLimit = reason.contains("Session Limit") || reason.contains("ContinuousUsageLimit") || reason.contains("flies")
    val imageRes = if (isSessionLimit) app.olauncher.R.drawable.app_timer else app.olauncher.R.drawable.focus_image
    
    // Strict/Sarcastic Tone Mapping
    val titleText = if (isSessionLimit) "ENOUGH." else "NO."
    val bodyText = when {
        isSessionLimit -> "You have been staring at this screen for too long.\nDo something real."
        reason.contains("Strict") -> "You explicitly banned this.\nDon't be weak."
        reason.contains("Schedule") -> "It is not the time.\nStick to the plan."
        reason.contains("Limit") -> "Quota exceeded.\nCome back tomorrow."
        reason.contains("Bored") -> "You are doom-scrolling.\nGo outside."
        else -> "This is not what you should be doing."
    }

    app.olauncher.ui.theme.TLauncherTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                // No Image - Text First as per "Text-first" requirement to minimize clutter
                // Actually user said "Text-first", "Minimal". Images might be clutter.
                // Let's keep it extremely clean. Just strict text.
                
                Text(
                    text = titleText,
                    style = app.olauncher.ui.theme.TLauncherTypography.displayLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                        letterSpacing = (-2).sp
                    ),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = bodyText,
                    style = app.olauncher.ui.theme.TLauncherTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(64.dp))
                
                // Extension Button (Only for Session Limit & if Allowed)
                if (canExtend) {
                    Button(
                        onClick = {
                            val intent = Intent("app.olauncher.ACTION_EXTEND_SESSION")
                            intent.setPackage(context.packageName) 
                            context.sendBroadcast(intent)
                            (context as? Activity)?.finish()
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                         shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "RELAPSE (+2m)",
                            style = app.olauncher.ui.theme.TLauncherTypography.labelLarge
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = onGoHome,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background
                    ),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "ACCEPT DEFEAT",
                        style = app.olauncher.ui.theme.TLauncherTypography.labelLarge
                    )
                }
            }
        }
    }
}
