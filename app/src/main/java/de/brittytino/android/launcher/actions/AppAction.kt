package de.brittytino.android.launcher.actions

import android.app.AlertDialog
import android.app.Service
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import de.brittytino.android.launcher.R
import de.brittytino.android.launcher.apps.AppInfo
import de.brittytino.android.launcher.apps.AbstractAppInfo.Companion.INVALID_USER
import de.brittytino.android.launcher.apps.DetailedAppInfo
import de.brittytino.android.launcher.data.AppDatabase
import de.brittytino.android.launcher.ui.list.apps.openSettings
import de.brittytino.android.launcher.ui.settings.delay.AppLaunchDelayOverlayActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("action:app")
class AppAction(val app: AppInfo) : Action {

    override fun invoke(context: Context, rect: Rect?): Boolean {
        val packageName = app.packageName
        
        if (packageName != context.packageName) {
             val db = AppDatabase.getDatabase(context)
             CoroutineScope(Dispatchers.Main).launch {
                 try {
                     val delayEntity = db.appLaunchDelayDao().getDelayForApp(packageName)
                     
                     val pm = context.packageManager
                     val isSystem = try {
                        (pm.getApplicationInfo(packageName, 0).flags and ApplicationInfo.FLAG_SYSTEM) != 0
                     } catch (e: Exception) { false }

                     val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? android.telecom.TelecomManager
                     val defaultDialer = telecomManager?.defaultDialerPackage
                     val isDefaultDialer = defaultDialer == packageName
                     
                     // Allow system apps, dialer, and self to bypass
                     if (!isSystem && !isDefaultDialer && delayEntity != null && delayEntity.enabled && delayEntity.delaySeconds > 0) {
                          val activityInfo = app.getLauncherActivityInfo(context)
                          val intent = Intent(context, AppLaunchDelayOverlayActivity::class.java).apply {
                              putExtra("EXTRA_PACKAGE_NAME", packageName)
                              putExtra("EXTRA_DELAY_SECONDS", delayEntity.delaySeconds)
                              putExtra("EXTRA_SOURCE_RECT", rect)
                              if (activityInfo != null) {
                                   putExtra("EXTRA_USER_HANDLE", activityInfo.user)
                                   putExtra("EXTRA_COMPONENT_NAME", activityInfo.componentName)
                              }
                              addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                          }
                          try {
                            context.startActivity(intent)
                          } catch (e: Exception) {
                            launchOrPrompt(context, rect)
                          }
                     } else {
                         launchOrPrompt(context, rect)
                     }
                 } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Launch check failed", Toast.LENGTH_SHORT).show()
                 }
            }
            return true
        }

        return launchOrPrompt(context, rect)
    }

    private fun launchOrPrompt(context: Context, rect: Rect?): Boolean {
        if (performLaunch(context, rect)) {
            return true
        }
        return showLaunchFailureDialog(context)
    }

    private fun performLaunch(context: Context, rect: Rect?): Boolean {
        val packageName = app.packageName
        if (app.user != INVALID_USER) {
            val launcherApps =
                context.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps
            app.getLauncherActivityInfo(context)?.let { app ->
                Log.i("Launcher", "Starting ${this.app}")
                try {
                    launcherApps.startMainActivity(app.componentName, app.user, rect, null)
                } catch (e: SecurityException) {
                    Log.i("Launcher", "Unable to start ${this.app}: ${e.message}")
                    Toast.makeText(context, context.getString(R.string.toast_cant_launch_app), Toast.LENGTH_LONG).show()
                    return false
                }
                return true
            }
        }

        context.packageManager.getLaunchIntentForPackage(packageName)?.let {
            it.addCategory(Intent.CATEGORY_LAUNCHER)
            try {
                context.startActivity(it)
            } catch (_: ActivityNotFoundException) {
                return false
            }
            return true
        }
        return false
    }

    private fun showLaunchFailureDialog(context: Context): Boolean {
        /* check if app is installed */
        if (isAvailable(context)) {
            AlertDialog.Builder(
                context,
                R.style.AlertDialogCustom
            )
                .setTitle(context.getString(R.string.alert_cant_open_title))
                .setMessage(context.getString(R.string.alert_cant_open_message))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    app.openSettings(context)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show()
            return true
        }
        return false
    }

    // kept for compatibility if needed, though override is above
    fun old_invoke(context: Context, rect: Rect?): Boolean {
        return false // stub
    }

    override fun label(context: Context): String {
        return DetailedAppInfo.fromAppInfo(app, context)?.getCustomLabel(context).toString()
    }

    override fun getIcon(context: Context): Drawable? {
        return DetailedAppInfo.fromAppInfo(app, context)?.getIcon(context)
    }

    override fun isAvailable(context: Context): Boolean {
        // check if app is installed
        return DetailedAppInfo.fromAppInfo(app, context) != null
    }

    override fun canReachSettings(): Boolean {
        return false
    }
}