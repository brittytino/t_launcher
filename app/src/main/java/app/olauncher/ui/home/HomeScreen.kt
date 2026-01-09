package app.olauncher.ui.home

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.olauncher.MainViewModel
import app.olauncher.helper.detectSwipe
import app.olauncher.data.Constants
import app.olauncher.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToProductivity: () -> Unit,
    onNavigateToPhone: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onAppClick: (Int) -> Unit, // Location index
    onAppLongClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val prefs = viewModel.prefs
    val refreshHome by viewModel.refreshHome.observeAsState()
    val screenTime by viewModel.screenTimeValue.observeAsState()
    val dateTimeVisibility = remember { prefs.dateTimeVisibility }
    val homeAlignment = remember { prefs.homeAlignment }
    
    // Refresh trigger
    LaunchedEffect(refreshHome) {
        // Redraw logic handled by state changes
    }
    
    // Clock
    var time by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        while(true) {
            val now = Date()
            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
            date = SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(now)
            delay(1000)
        }
    }

    TScaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { 
                            if (prefs.gestureDoubleTap == "LOCK_SCREEN") {
                                 val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                                 if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                     vibrator?.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                                 } else {
                                     vibrator?.vibrate(50)
                                 }
                                 context.sendBroadcast(Intent("app.olauncher.ACTION_LOCK_SCREEN"))
                            }
                        },
                        onTap = { },
                        onLongPress = { onNavigateToSettings() }
                    )
                }
                .pointerInput(Unit) {
                     detectSwipe(
                         onSwipeLeft = onNavigateToDashboard,
                         onSwipeRight = onNavigateToPhone,
                         onSwipeDown = onNavigateToSearch
                     )
                }
        ) {
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = TLauncherTheme.spacing.medium),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                
                // DateTime Area
                if (dateTimeVisibility != Constants.DateTime.OFF) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = getAlignment(homeAlignment)) {
                        Column(horizontalAlignment = getHorizontalAlignment(homeAlignment)) {
                             if (Constants.DateTime.isTimeVisible(dateTimeVisibility)) {
                                Text(
                                    text = time,
                                    style = TLauncherTypography.headlineLarge.copy(
                                        fontSize = 64.sp, 
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Thin,
                                        letterSpacing = (-2).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.combinedClickable(
                                        onClick = { /* Check Alarm? */ },
                                        onLongClick = { onAppLongClick(Constants.FLAG_SET_CLOCK_APP) }
                                    )
                                )
                            }
                            if (Constants.DateTime.isDateVisible(dateTimeVisibility)) {
                                Text(
                                    text = date.uppercase(),
                                    style = TLauncherTypography.labelSmall.copy(letterSpacing = 2.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.combinedClickable(
                                        onClick = { app.olauncher.helper.openCalendar(context) },
                                        onLongClick = { onAppLongClick(Constants.FLAG_SET_CALENDAR_APP) }
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(64.dp))
                }

                // App List Area (Card Container)
                val homeAppsNum = prefs.homeAppsNum
                
                // We wrap the apps in a calm card for that "Control Panel" feel
                TCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(vertical = TLauncherTheme.spacing.medium, horizontal = TLauncherTheme.spacing.medium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        for (i in 1..8) {
                            if (i <= homeAppsNum) {
                                val appName = prefs.getAppName(i)
                                HomeAppItem(
                                    text = if (appName.isNotEmpty()) appName else "Tap to Select",
                                    alignment = getAlignment(homeAlignment),
                                    onClick = { onAppClick(i) },
                                    onLongClick = { onAppLongClick(i) },
                                    isEmpty = appName.isEmpty()
                                )
                                if (i < homeAppsNum) {
                                   Spacer(modifier = Modifier.height(TLauncherTheme.spacing.medium))
                                }
                            }
                        }
                    }
                }
            }
            
            // Screen Time (Top Right)
            if (screenTime != null) {
                TChip(
                    text = screenTime ?: "",
                    onClick = { onNavigateToDashboard() }, // Click to go to dashboard
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 48.dp, end = 24.dp),
                    selected = false
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeAppItem(
    text: String,
    alignment: Alignment,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isEmpty: Boolean
) {
    // Each item is a row, but aligned as per preference.
    // Since we are inside a card, we take full width of the card padding.
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Text(
            text = text,
            style = TLauncherTypography.headlineMedium.copy(
                fontSize = 20.sp,
                fontWeight = if (isEmpty) androidx.compose.ui.text.font.FontWeight.Light else androidx.compose.ui.text.font.FontWeight.Medium
            ),
            color = if (isEmpty) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.4f) else MaterialTheme.colorScheme.onSurface,
            textAlign = if (alignment == Alignment.Center) TextAlign.Center else if (alignment == Alignment.CenterStart) TextAlign.Start else TextAlign.End,
            modifier = Modifier
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(vertical = 4.dp, horizontal = 8.dp)
        )
    }
}

fun getAlignment(gravity: Int): Alignment {
    return when (gravity) {
        android.view.Gravity.START -> Alignment.CenterStart
        android.view.Gravity.END -> Alignment.CenterEnd
        else -> Alignment.Center
    }
}

fun getHorizontalAlignment(gravity: Int): Alignment.Horizontal {
    return when (gravity) {
        android.view.Gravity.START -> Alignment.Start
        android.view.Gravity.END -> Alignment.End
        else -> Alignment.CenterHorizontally
    }
}


