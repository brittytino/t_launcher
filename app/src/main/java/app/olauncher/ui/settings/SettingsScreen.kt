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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.olauncher.MainViewModel
import app.olauncher.R
import app.olauncher.ui.theme.*
import app.olauncher.data.Constants
import android.view.Gravity

enum class SettingsPage { HOME, CATEGORIES, LIMITS, SCHEDULES, USAGE }

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenUsageAccess: () -> Unit,
    onOpenNotificationListener: () -> Unit
) {
    var currentPage by remember { mutableStateOf(SettingsPage.HOME) }

    TScaffold {
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
    val scrollState = rememberScrollState()
    
    // Check-in logic states
    var strictBlocking by remember { mutableStateOf(prefs.strictBlockingEnabled) }
    var visualDetox by remember { mutableStateOf(prefs.isVisualDetox) }
    var textOnlyUi by remember { mutableStateOf(prefs.textOnlyUiEnabled) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(TLauncherTheme.spacing.medium)
    ) {
        SettingsTopBar(onNavigateBack)
        
        Spacer(modifier = Modifier.height(TLauncherTheme.spacing.medium))

        // --- Surveillance ---
        SectionHeader("SURVEILLANCE")
        TCard {
            SettingsItem(
                title = "AUDIT LOG",
                subtitle = "View system integrity & failures",
                onClick = { onNavigateTo(SettingsPage.USAGE) }
            )
        }

        // --- Core ---
        SectionHeader("CORE PROTOCOLS")
        TCard {
            Column {
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
            }
        }

        // --- Distractions ---
        SectionHeader("DISTRACTIONS")
        TCard {
            Column {
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
                SettingsItem(
                    title = "BLOCKING SCHEDULES",
                    subtitle = "Automate your focus time",
                    onClick = { onNavigateTo(SettingsPage.SCHEDULES) }
                )
            }
        }

        // --- Gestures ---
        SectionHeader("Gestures")
        TCard {
            Column {
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
                    subtitle = "Enable swipe left gesture",
                    checked = remember { mutableStateOf(prefs.swipeLeftEnabled) }.value,
                    onCheckedChange = { prefs.swipeLeftEnabled = it }
                )
                SwitchSettingsItem(
                    title = "Swipe Right to Open App",
                    subtitle = "Enable swipe right gesture",
                    checked = remember { mutableStateOf(prefs.swipeRightEnabled) }.value,
                    onCheckedChange = { prefs.swipeRightEnabled = it }
                )
            }
        }

        // --- Customization ---
        SectionHeader("CUSTOMIZATION")
        TCard {
            Column {
                // Wallpaper Selector
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
            }
        }

        // --- Visual Detox ---
        SectionHeader(stringResource(R.string.visual_detox))
        TCard {
            Column {
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
            }
        }

        // --- System Permissions ---
        SectionHeader(stringResource(R.string.system_permissions))
        TCard {
            Column {
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
            }
        }

        // --- Help & Guide ---
        var showHowToUse by remember { mutableStateOf(false) }
        Spacer(modifier = Modifier.height(TLauncherTheme.spacing.medium))
        TCard {
            SettingsItem(
                title = "How to use",
                subtitle = "Quick guide to digital minimalism",
                onClick = { showHowToUse = true }
            )
        }
        
        if (showHowToUse) {
            HowToUseDialog(onDismiss = { showHowToUse = false })
        }

        Spacer(modifier = Modifier.height(TLauncherTheme.spacing.extraLarge))

        // --- Footer ---
        SettingsFooter()
        
        Spacer(modifier = Modifier.height(TLauncherTheme.spacing.extraLarge))
    }
}

// ... Subcomponents ...

@Composable
fun SettingsFooter() {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
         Icon(
            painter = painterResource(id = R.drawable.ic_mindfulness),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), 
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.app_name).uppercase(),
            style = TLauncherTypography.labelSmall.copy(
                letterSpacing = 4.sp,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Crafted by ${stringResource(R.string.developer_name)}",
            style = TLauncherTypography.labelSmall.copy(
                fontWeight = FontWeight.Light,
                letterSpacing = 1.sp,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable { uriHandler.openUri("https://tinobritty.me") }
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun SettingsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = TLauncherTheme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            text = "Settings",
            style = TLauncherTypography.headlineMedium,
            modifier = Modifier.padding(start = TLauncherTheme.spacing.medium)
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = TLauncherTypography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            top = TLauncherTheme.spacing.large, 
            bottom = TLauncherTheme.spacing.small,
            start = TLauncherTheme.spacing.small // Align with card content roughly
        )
    )
}

@Composable
fun SettingsItem(title: String, subtitle: String? = null, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp) // Card padding
    ) {
        Text(text = title, style = TLauncherTypography.titleMedium)
        if (subtitle != null) {
            Text(text = subtitle, style = TLauncherTypography.bodyMedium)
        }
    }
}

@Composable
fun SwitchSettingsItem(title: String, subtitle: String? = null, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = TLauncherTypography.titleMedium)
            if (subtitle != null) {
                Text(text = subtitle, style = TLauncherTypography.bodyMedium)
            }
        }
        TSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// ... Dropdowns and other helpers would follow similar patterns ...
// For brevity, keeping them simple but styled.

@Composable
fun DateTimeDropdown(currentState: Int, onStateSelected: (Int) -> Unit) {
    val labels = mapOf(
        Constants.DateTime.OFF to "Hidden",
        Constants.DateTime.ON to "Time & Date",
        Constants.DateTime.DATE_ONLY to "Date Only"
    )
    val nextState = when(currentState) {
        Constants.DateTime.OFF -> Constants.DateTime.ON
        Constants.DateTime.ON -> Constants.DateTime.DATE_ONLY
        else -> Constants.DateTime.OFF
    }
    
    SettingsItem(
        title = "Home Screen Clock", 
        subtitle = labels[currentState] ?: "Unknown",
        onClick = { onStateSelected(nextState) }
    )
}

@Composable
fun AlignmentDropdown(currentAlign: Int, onAlignSelected: (Int) -> Unit) {
    val labels = mapOf(
        android.view.Gravity.CENTER to "Center",
        android.view.Gravity.START to "Left",
        android.view.Gravity.END to "Right"
    )
    val nextAlign = when(currentAlign) {
        android.view.Gravity.CENTER -> android.view.Gravity.START
        android.view.Gravity.START -> android.view.Gravity.END
        else -> android.view.Gravity.CENTER
    }

    SettingsItem(
        title = "App Label Alignment", 
        subtitle = labels[currentAlign] ?: "Center",
        onClick = { onAlignSelected(nextAlign) }
    )
}

@Composable
fun SwipeActionDropdown(label: String, currentAction: Int, onActionSelected: (Int) -> Unit) {
     val labels = mapOf(
         Constants.SwipeDownAction.SEARCH to "Search Apps",
         Constants.SwipeDownAction.NOTIFICATIONS to "Open Notifications"
     )
     val nextAction = if (currentAction == Constants.SwipeDownAction.SEARCH) Constants.SwipeDownAction.NOTIFICATIONS else Constants.SwipeDownAction.SEARCH

     SettingsItem(
         title = label, 
         subtitle = labels[currentAction] ?: "Search Apps",
         onClick = { onActionSelected(nextAction) }
     )
}

@Composable
fun PermissionItem(title: String, subtitle: String, isEnabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = TLauncherTypography.titleMedium)
            Text(text = subtitle, style = TLauncherTypography.bodyMedium)
        }
        Text(
            text = if (isEnabled) "ACTIVE" else "GRANT",
            style = TLauncherTypography.labelSmall,
            color = if (isEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun HowToUseDialog(onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        TCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("WELCOME TO T LAUNCHER", style = TLauncherTypography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "T Launcher is designed to be boring.\n\n" +
                    "• Long press apps to rename or hide them.\n" +
                    "• Use Focus Mode to block distractions.\n" +
                    "• Check 'Surveillance' to see what the system is doing.\n\n" +
                    "Strictness is the only way forward.",
                    style = TLauncherTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(24.dp))
                TChip(
                    text = "GOT IT",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    selected = true
                )
            }
        }
    }
}

// ... Placeholder Stubs for missing Subscreens ...
// Sub-screens are imported from their respective files
