package app.olauncher.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.olauncher.MainViewModel
import app.olauncher.R
import app.olauncher.ui.theme.TLauncherTheme
import app.olauncher.ui.theme.TLauncherTypography

enum class SettingsPage { HOME, CATEGORIES, LIMITS, SCHEDULES, USAGE }

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenUsageAccess: () -> Unit,
    onOpenNotificationListener: () -> Unit
) {
    val prefs = viewModel.prefs
    val context = LocalContext.current
    var currentPage by remember { mutableStateOf(SettingsPage.HOME) }

    TLauncherTheme {
        when(currentPage) {
            SettingsPage.USAGE -> {
                 TLauncherUsageScreen(viewModel = viewModel, onBack = { currentPage = SettingsPage.HOME })
            }
            SettingsPage.CATEGORIES -> {
                AppCategoryScreen(viewModel = viewModel, onBack = { currentPage = SettingsPage.HOME })
            }
            SettingsPage.LIMITS -> {
                TimeLimitScreen(viewModel = viewModel, onBack = { currentPage = SettingsPage.HOME })
            }
            SettingsPage.SCHEDULES -> {
                ScheduleScreen(viewModel = viewModel, onBack = { currentPage = SettingsPage.HOME })
            }
            SettingsPage.HOME -> {
                MainSettingsList(
                    viewModel, 
                    onNavigateBack, 
                    onOpenAccessibility, 
                    onOpenUsageAccess, 
                    onOpenNotificationListener,
                    onNavigateTo = { currentPage = it }
                )
            }
        }
    }
}

@Composable
fun MainSettingsList(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenUsageAccess: () -> Unit,
    onOpenNotificationListener: () -> Unit,
    onNavigateTo: (SettingsPage) -> Unit
) {
    val prefs = viewModel.prefs
    val context = LocalContext.current
    
    // State
    var strictBlocking by remember { mutableStateOf(prefs.strictBlockingEnabled) }
    var cognitiveAlarm by remember { mutableStateOf(prefs.cognitiveAlarmEnabled) }
    var visualDetox by remember { mutableStateOf(prefs.isVisualDetox) }
    var textOnlyUi by remember { mutableStateOf(prefs.textOnlyUiEnabled) }
    
    // Productivity States
    var todoEnabled by remember { mutableStateOf(prefs.productivityTodoEnabled) }
    var notesEnabled by remember { mutableStateOf(prefs.productivityNotesEnabled) }
    var remindersEnabled by remember { mutableStateOf(prefs.productivityRemindersEnabled) }
    


    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            SettingsTopBar(onNavigateBack)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            
            // --- T Launcher Usage ---
            SectionHeader("SURVEILLANCE")
            SettingsItem(
                title = "AUDIT LOG",
                subtitle = "View system integrity & failures",
                onClick = { onNavigateTo(SettingsPage.USAGE) }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // --- Core ---
            SectionHeader("CORE PROTOCOLS")

            SwitchSettingsItem(
                title = "Status Bar Visibility",
                subtitle = "Toggle top bar distractions",
                checked = remember { mutableStateOf(prefs.showStatusBar) }.value,
                onCheckedChange = { 
                    prefs.showStatusBar = it
                    (context as? android.app.Activity)?.recreate()
                }
            )

             var dateTimeState by remember { mutableIntStateOf(prefs.dateTimeVisibility) }
             DateTimeDropdown(
                currentState = dateTimeState,
                onStateSelected = { 
                    dateTimeState = it
                    prefs.dateTimeVisibility = it
                }
             )

             var alignState by remember { mutableIntStateOf(prefs.appLabelAlignment) }
             AlignmentDropdown(
                currentAlign = alignState,
                onAlignSelected = {
                    alignState = it
                    prefs.appLabelAlignment = it
                }
             )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // --- Distractions ---
            SectionHeader("DISTRACTIONS")
            
            SettingsItem(
                title = "CATEGORIZATION",
                subtitle = "Label your vices",
                onClick = { onNavigateTo(SettingsPage.CATEGORIES) }
            )
            
            SwitchSettingsItem(
                title = "STRICT BLOCKING",
                subtitle = "No overrides allowed",
                checked = strictBlocking,
                onCheckedChange = { 
                    strictBlocking = it
                    prefs.strictBlockingEnabled = it
                }
            )

             SettingsItem(
                title = "HARD LIMITS",
                subtitle = "Set daily caps",
                onClick = { onNavigateTo(SettingsPage.LIMITS) }
            )
            
            // --- Schedules ---
            SettingsItem(
                title = "BLOCKING SCHEDULES",
                subtitle = "Automate your focus time",
                onClick = { onNavigateTo(SettingsPage.SCHEDULES) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
             HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // --- Gestures (Legacy) ---
            SectionHeader("Gestures")

             var swipeDownState by remember { mutableIntStateOf(prefs.swipeDownAction) }
             SwipeActionDropdown(
                label = "Swipe Down Action",
                currentAction = swipeDownState,
                onActionSelected = { 
                    swipeDownState = it
                    prefs.swipeDownAction = it
                }
             )

            SwitchSettingsItem(
                title = "Swipe Left to Open App",
                subtitle = "Enable swipe left gesture (Camera by default)",
                checked = remember { mutableStateOf(prefs.swipeLeftEnabled) }.value,
                onCheckedChange = { prefs.swipeLeftEnabled = it }
            )

            SwitchSettingsItem(
                title = "Swipe Right to Open App",
                subtitle = "Enable swipe right gesture (Phone by default)",
                checked = remember { mutableStateOf(prefs.swipeRightEnabled) }.value,
                onCheckedChange = { prefs.swipeRightEnabled = it }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))            



            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // --- Customization ---
            SectionHeader("CUSTOMIZATION")
            
            // Wallpaper Selector
            val context = LocalContext.current
            var wallpaperName by remember { mutableStateOf("Tap to Change") }
            
            SettingsItem(
                title = "Wallpaper Style",
                subtitle = wallpaperName,
                onClick = {
                    val count = app.olauncher.helper.WallpaperManager.getWallpaperCount()
                    val randomIdx = (0 until count).random()
                    app.olauncher.helper.WallpaperManager.applyWallpaper(context, randomIdx)
                    wallpaperName = app.olauncher.helper.WallpaperManager.getWallpaperName(randomIdx)
                    android.widget.Toast.makeText(context, "Applied: $wallpaperName", android.widget.Toast.LENGTH_SHORT).show()
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // --- Visual Detox ---
            SectionHeader(stringResource(R.string.visual_detox))
             SwitchSettingsItem(
                title = stringResource(R.string.grayscale_mode),
                subtitle = stringResource(R.string.grayscale_mode_subtitle),
                checked = visualDetox,
                onCheckedChange = { 
                    visualDetox = it
                    prefs.isVisualDetox = it
                    viewModel.applyVisualDetox.postValue(Unit)
                }
            )
            SwitchSettingsItem(
                title = stringResource(R.string.text_only_ui),
                subtitle = stringResource(R.string.text_only_ui_subtitle),
                checked = textOnlyUi,
                onCheckedChange = { 
                    textOnlyUi = it
                    prefs.textOnlyUiEnabled = it
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // --- System Permissions ---
            SectionHeader(stringResource(R.string.system_permissions))
            
            PermissionItem(
                title = stringResource(R.string.accessibility_service),
                subtitle = stringResource(R.string.accessibility_service_subtitle),
                isEnabled = app.olauncher.helper.isAccessServiceEnabled(context)
            ) { onOpenAccessibility() }
            
            PermissionItem(
                title = stringResource(R.string.usage_access),
                subtitle = stringResource(R.string.usage_access_subtitle),
                isEnabled = app.olauncher.helper.appUsagePermissionGranted(context)
            ) { onOpenUsageAccess() }
            
             PermissionItem(
                title = stringResource(R.string.notification_filter),
                subtitle = stringResource(R.string.notification_filter_subtitle),
                isEnabled = true 
            ) { onOpenNotificationListener() }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // --- Help & Guide ---
            var showHowToUse by remember { mutableStateOf(false) }
            
            SettingsItem(
                title = "How to use",
                subtitle = "Quick guide to digital minimalism",
                onClick = { showHowToUse = true }
            )
            
            if (showHowToUse) {
                HowToUseDialog(onDismiss = { showHowToUse = false })
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // --- Branding (Premium Footer) ---
            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp), // Minimal top spacing
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                 // Minimalist Logo
                 Icon(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_mindfulness),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), 
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // App Name
                Text(
                    text = stringResource(R.string.app_name).uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                        letterSpacing = 4.sp,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Credit
                Text(
                    text = "Crafted by ${stringResource(R.string.developer_name)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Light,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 10.sp
                    ),
                    modifier = Modifier
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                        .clickable { uriHandler.openUri("https://tinobritty.me") }
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun HowToUseDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "WELCOME TO T LAUNCHER",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                GuideItem("Clean Home", "Your home screen is text-only. Tap text to open apps. Swipe left/right for quick actions.")
                GuideItem("Productivity", "Swipe LEFT to access Notes, Tasks, and Focus Tools.")
                GuideItem("Digital Detox", "Use the 'Journal' to track your daily digital habits and consistency.")
                GuideItem("Privacy", "No data leaves your device. Everything is offline.")
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("GOT IT")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun GuideItem(title: String, desc: String) {
    Column {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
        Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
            
// End of MainSettingsList logic managed above.

@Composable
fun SettingsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
        Text(
            text = "Settings",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}



@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = TLauncherTypography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = TLauncherTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = TLauncherTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SwitchSettingsItem(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TLauncherTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = TLauncherTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
fun HorizontalDivider(modifier: Modifier = Modifier) {
    androidx.compose.material3.HorizontalDivider(
        modifier = modifier,
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 1.dp
    )
}

@Composable
fun PermissionItem(title: String, subtitle: String, isEnabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, fontSize = 14.sp, color = Color.Gray)
        }
        Text(
            text = if (isEnabled) "ACTIVE" else "REQUIRED",
            color = if (isEnabled) Color(0xFF4CAF50) else Color(0xFFF44336),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}



@Composable
fun DateTimeDropdown(currentState: Int, onStateSelected: (Int) -> Unit) {
    val options = listOf(
        app.olauncher.data.Constants.DateTime.ON to "Date & Time",
        app.olauncher.data.Constants.DateTime.DATE_ONLY to "Date Only",
        app.olauncher.data.Constants.DateTime.OFF to "Hidden"
    )
    
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = "Home Screen Clock", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(vertical = 8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                .padding(12.dp)
        ) {
            Text(text = options.find { it.first == currentState }?.second ?: "Unknown")
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (valInt, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onStateSelected(valInt)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AlignmentDropdown(currentAlign: Int, onAlignSelected: (Int) -> Unit) {
    val options = listOf(
        android.view.Gravity.START to "Left Align",
        android.view.Gravity.CENTER to "Center Align",
        android.view.Gravity.END to "Right Align"
    )
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = "App Label Alignment", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(vertical = 8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                .padding(12.dp)
        ) {
            Text(text = options.find { it.first == currentAlign }?.second ?: "Left")
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (valInt, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onAlignSelected(valInt)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SwipeActionDropdown(label: String, currentAction: Int, onActionSelected: (Int) -> Unit) {
    val options = listOf(
        app.olauncher.data.Constants.SwipeDownAction.SEARCH to "Search",
        app.olauncher.data.Constants.SwipeDownAction.NOTIFICATIONS to "Notifications"
    )
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(vertical = 8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                .padding(12.dp)
        ) {
            Text(text = options.find { it.first == currentAction }?.second ?: "Unknown")
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (valInt, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onActionSelected(valInt)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
