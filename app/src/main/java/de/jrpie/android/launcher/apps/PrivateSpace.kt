package de.jrpie.android.launcher.apps

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Build
import android.os.UserHandle
import android.os.UserManager
import android.provider.Settings
import android.widget.Toast
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.isDefaultHomeScreen
import de.jrpie.android.launcher.setDefaultHomeScreen


/*
 * Checks whether the device supports private space.
 */
fun isPrivateSpaceSupported(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
}

fun getPrivateSpaceUser(context: Context): UserHandle? {
    if (!isPrivateSpaceSupported()) {
        return null
    }
    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    return userManager.userProfiles.firstOrNull { u ->
        launcherApps.getLauncherUserInfo(u)?.userType == UserManager.USER_TYPE_PROFILE_PRIVATE
    }
}

/**
 * Check whether the user has created a private space and whether µLauncher can access it.
 */
fun isPrivateSpaceSetUp(
    context: Context,
    showToast: Boolean = false,
    launchSettings: Boolean = false
): Boolean {
    if (!isPrivateSpaceSupported()) {
        if (showToast) {
            Toast.makeText(
                context,
                context.getString(R.string.alert_requires_android_v),
                Toast.LENGTH_LONG
            ).show()
        }
        return false
    }
    val privateSpaceUser = getPrivateSpaceUser(context)
    if (privateSpaceUser != null) {
        return true
    }
    if (!isDefaultHomeScreen(context)) {
        if (showToast) {
            Toast.makeText(
                context,
                context.getString(R.string.toast_private_space_default_home_screen),
                Toast.LENGTH_LONG
            ).show()
        }
        if (launchSettings) {
            setDefaultHomeScreen(context)
        }
    } else {
        if (showToast) {
            Toast.makeText(
                context,
                context.getString(R.string.toast_private_space_not_available),
                Toast.LENGTH_LONG
            ).show()
        }
        if (launchSettings) {
            try {
                context.startActivity(Intent(Settings.ACTION_PRIVACY_SETTINGS))
            } catch (_: ActivityNotFoundException) {
            }
        }
    }
    return false
}

fun isPrivateSpaceLocked(context: Context): Boolean {
    if (!isPrivateSpaceSupported()) {
        return false
    }
    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    val privateSpaceUser = getPrivateSpaceUser(context) ?: return false
    return userManager.isQuietModeEnabled(privateSpaceUser)
}

fun lockPrivateSpace(context: Context, lock: Boolean) {
    if (!isPrivateSpaceSupported()) {
        return
    }

    // silently return when trying to unlock but hide when locked is set
    if (!lock && hidePrivateSpaceWhenLocked(context)) {
        return
    }

    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    val privateSpaceUser = getPrivateSpaceUser(context) ?: return
    userManager.requestQuietModeEnabled(lock, privateSpaceUser)
}

fun togglePrivateSpaceLock(context: Context) {
    if (!isPrivateSpaceSetUp(context, showToast = true, launchSettings = true)) {
        return
    }

    val lock = isPrivateSpaceLocked(context)
    lockPrivateSpace(context, !lock)
    if (!lock) {
        Toast.makeText(
            context,
            context.getString(R.string.toast_private_space_locked),
            Toast.LENGTH_LONG
        ).show()
    }
}

@Suppress("SameReturnValue")
fun hidePrivateSpaceWhenLocked(context: Context): Boolean {
    // Trying to access the setting as a 3rd party launcher raises a security exception.
    // This is an Android bug: https://issuetracker.google.com/issues/352276244#comment5
    // The logic for this is implemented.
    // TODO: replace this once the Android bug is fixed
    return false

    // TODO: perhaps this should be cached
    // https://cs.android.com/android/platform/superproject/main/+/main:packages/apps/Launcher3/src/com/android/launcher3/util/SettingsCache.java;l=61;drc=56bf7ad33bc9d5ed3c18e7abefeec5c177ec75d7

    // val key = "hide_privatespace_entry_point"
    // return Settings.Secure.getInt(context.contentResolver, key, 0) == 1
}

