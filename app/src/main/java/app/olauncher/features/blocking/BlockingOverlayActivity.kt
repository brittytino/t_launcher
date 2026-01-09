package app.olauncher.features.blocking

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.olauncher.ui.theme.TLauncherTheme
import app.olauncher.ui.theme.TLauncherTypography
import app.olauncher.ui.theme.TChip
import app.olauncher.ui.theme.TCard

class BlockingOverlayActivity : androidx.activity.ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { }
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
    
    val titleText = if (isSessionLimit) "ENOUGH." else "NO."
    val bodyText = when {
        isSessionLimit -> "You have been staring at this screen for too long.\nDo something real."
        reason.contains("Strict") -> "You explicitly banned this.\nDon't be weak."
        reason.contains("Schedule") -> "It is not the time.\nStick to the plan."
        reason.contains("Limit") -> "Quota exceeded.\nCome back tomorrow."
        reason.contains("Bored") -> "You are doom-scrolling.\nGo outside."
        else -> "This is not what you should be doing."
    }

    TLauncherTheme {
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
                // Strict Title
                Text(
                    text = titleText,
                    style = TLauncherTypography.headlineLarge.copy(
                        fontSize = 64.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                        letterSpacing = (-2).sp
                    ),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))

                // Reasoning Card
                TCard(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = bodyText,
                            style = TLauncherTypography.bodyLarge.copy(fontSize = 18.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                             modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(64.dp))
                
                // Actions
                if (canExtend) {
                    TChip(
                        text = "RELAPSE (+2m)",
                        onClick = {
                            val intent = Intent("app.olauncher.ACTION_EXTEND_SESSION")
                            intent.setPackage(context.packageName) 
                            context.sendBroadcast(intent)
                            (context as? Activity)?.finish()
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        selected = false
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                TChip(
                    text = "ACCEPT DEFEAT",
                    onClick = onGoHome,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    selected = true
                )
            }
        }
    }
}
