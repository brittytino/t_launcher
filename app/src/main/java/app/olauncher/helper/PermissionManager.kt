package app.olauncher.helper

import android.content.Context
import android.app.AppOpsManager
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.net.Uri
import android.content.ComponentName
import android.app.NotificationManager
import android.os.PowerManager
import androidx.core.content.ContextCompat

class PermissionManager(private val context: Context) {

    // --- State Checks ---

    fun isAccessibilityServiceEnabled(): Boolean {
        // More robust check
        val expectedComponentName = ComponentName(context, app.olauncher.services.EnforcementService::class.java)
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        
        val stringColonSplitter = android.text.TextUtils.SimpleStringSplitter(':')
        stringColonSplitter.setString(enabledServicesSetting)
        
        while (stringColonSplitter.hasNext()) {
            val componentNameString = stringColonSplitter.next()
            val enabledComponent = ComponentName.unflattenFromString(componentNameString)
            if (enabledComponent != null && enabledComponent == expectedComponentName)
                return true
        }
        return false
    }

    fun isUsageStatsGranted(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun isNotificationListenerGranted(): Boolean {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(packageName)
    }

    fun isBatteryOptimizationIgnored(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            return powerManager.isIgnoringBatteryOptimizations(context.packageName)
        }
        return true
    }
    
    fun isOverlayGranted(): Boolean {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context)
        }
        return true
    }

    // --- Intent Builders ---

    fun getAccessibilityIntent(): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun getUsageStatsIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun getNotificationListenerIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
    
    fun getBatteryOptimizationIntent(): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                 data = Uri.parse("package:${context.packageName}")
                 addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        return Intent(Settings.ACTION_SETTINGS)
    }
    
    fun getOverlayIntent(): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
             return Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        return Intent(Settings.ACTION_SETTINGS)
    }
    
    fun getDefaultLauncherIntent(): Intent {
         return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
             Intent(Settings.ACTION_HOME_SETTINGS).apply {
                 addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
             }
        }
    }
}
