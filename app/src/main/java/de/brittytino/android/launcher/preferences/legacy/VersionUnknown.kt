package de.brittytino.android.launcher.preferences.legacy

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import de.brittytino.android.launcher.preferences.LauncherPreferences
import de.brittytino.android.launcher.preferences.theme.Background
import de.brittytino.android.launcher.preferences.theme.ColorTheme


private fun migrateStringPreference(
    oldPrefs: SharedPreferences,
    newPreferences: SharedPreferences.Editor,
    oldKey: String,
    newKey: String,
    default: String
) {
    val s = oldPrefs.getString(oldKey, default)
    newPreferences.putString(newKey, s)
}

private fun migrateIntPreference(
    oldPrefs: SharedPreferences,
    newPreferences: SharedPreferences.Editor,
    oldKey: String,
    newKey: String,
    default: Int
) {
    val s = oldPrefs.getInt(oldKey, default)
    newPreferences.putInt(newKey, s)
}

private fun migrateBooleanPreference(
    oldPrefs: SharedPreferences,
    newPreferences: SharedPreferences.Editor,
    oldKey: String,
    newKey: String,
    default: Boolean
) {
    val s = oldPrefs.getBoolean(oldKey, default)
    newPreferences.putBoolean(newKey, s)
}

private const val TAG = "Preferences ? -> 1"

/**
 * Try to migrate from a very old preference version, where no version number was stored
 * and a different file was used.
 */
fun migratePreferencesFromVersionUnknown(context: Context) {

    Log.i(
        TAG,
        "Unknown preference version, trying to restore preferences from old version."
    )

    val oldPrefs = context.getSharedPreferences(
        "V3RYR4ND0MK3YCR4P",
        Context.MODE_PRIVATE
    )
    if (!oldPrefs.contains("startedBefore")) {
        Log.i(TAG, "No old preferences found. Probably this is a fresh installation.")
        return
    }

    LauncherPreferences.getSharedPreferences().edit {

        migrateBooleanPreference(
            oldPrefs,
            this,
            "startedBefore",
            "internal.started_before",
            false
        )

        migrateStringPreference(
            oldPrefs,
            this,
            "action_volumeUpApp",
            "action.volume_up.app",
            ""
        )
        migrateIntPreference(
            oldPrefs,
            this,
            "action_volumeUpApp_user",
            "action.volume_up.user",
            -1
        )
        migrateStringPreference(
            oldPrefs,
            this,
            "action_volumeDownApp",
            "action.volume_down.app",
            ""
        )
        migrateIntPreference(
            oldPrefs,
            this,
            "action_volumeDownApp_user",
            "action.volume_down.user",
            -1
        )
        migrateStringPreference(oldPrefs, this, "action_timeApp", "action.time.app", "")
        migrateIntPreference(oldPrefs, this, "action_timeApp_user", "action.time.user", -1)
        migrateStringPreference(oldPrefs, this, "action_dateApp", "action.date.app", "")
        migrateIntPreference(oldPrefs, this, "action_dateApp_user", "action.date.user", -1)
        migrateStringPreference(
            oldPrefs,
            this,
            "action_longClickApp",
            "action.long_click.app",
            ""
        )
        migrateIntPreference(
            oldPrefs,
            this,
            "action_longClickApp_user",
            "action.long_click.user",
            -1
        )
        migrateStringPreference(
            oldPrefs,
            this,
            "action_doubleClickApp",
            "action.double_click.app",
            ""
        )
        migrateIntPreference(
            oldPrefs,
            this,
            "action_doubleClickApp_user",
            "action.double_click.user",
            -1
        )
        migrateStringPreference(oldPrefs, this, "action_upApp", "action.up.app", "")
        migrateIntPreference(oldPrefs, this, "action_upApp_user", "action.up.user", -1)
        migrateStringPreference(
            oldPrefs,
            this,
            "action_up_leftApp",
            "action.up_left.app",
            ""
        )
        migrateIntPreference(
            oldPrefs,
            this,
            "action_up_leftApp_user",
            "action.up_left.user",
            -1
        )
        migrateStringPreference(
            oldPrefs,
            this,
            "action_up_rightApp",
            "action.up_right.app",
            ""
        )
        migrateIntPreference(
            oldPrefs,
            this,
            "action_up_rightApp_user",
            "action.up_right.user",
            -1
        )
        migrateStringPreference(
            oldPrefs,
            this,
            "action_doubleUpApp",
            "action.double_up.app",
            ""
        )
        migrateIntPreference(
            oldPrefs,
            this,
            "action_doubleUpApp_user",
            "action.double_up.user",
            -1
        )
        migrateStringPreference(oldPrefs, this, "action_downApp", "action.down.app", "")
        migrateIntPreference(oldPrefs, this, "action_downApp_user", "action.down.user", -1)
        migrateStringPreference(
            oldPrefs,
            this,
            "action_down_leftApp",
            "action.down_left.app",
            ""
        )
        migrateIntPreference(
            oldPrefs,
            this,
            "action_down_leftApp_user",
            "action.down_left.user",
            -1
        )
        migrateStringPreference(
            oldPrefs,
            this,
            "action_down_rightApp",
            "action.down_right.app",
            ""
        )
        migrateIntPreference(
            oldPrefs,
            this,
            "action_down_rightApp_user",
            "action.down_right.user",
            -1
        )
        migrateStringPreference(
            oldPrefs,
            this,
            "action_doubleDownApp",
            "action.double_down.app",
            ""
        )
        migrateIntPreference(
            oldPrefs,
            this,
            "action_doubleDownApp_user",
            "action.double_down.user",
            -1
        )
        migrateStringPreference(oldPrefs, this, "action_leftApp", "action.left.app", "")
        migrateIntPreference(oldPrefs, this, "action_leftApp_user", "action.left.user", -1)
        migrateStringPreference(
            oldPrefs,
            this,
            "action_left_topApp",
            "action.left_top.app",
            ""
        )
        migrateIntPreference(
            oldPrefs,
            this,
            "action_left_topApp_user",
            "action.left_top.user",
            -1
        )
        migrateStringPreference(
            oldPrefs,
            this,
            "action_left_bottomApp",
            "action.left_bottom.app",
            ""
        )
        migrateIntPreference(
            oldPrefs,
            this,
            "action_left_bottomApp_user",
            "action.left_bottom.user",
            -1
        )
        migrateStringPreference(
            oldPrefs,
            this,
            "action_doubleLeftApp",
            "action.double_left.app",
            ""
        )
        migrateIntPreference(
            oldPrefs,
            this,
            "action_doubleLeftApp_user",
            "action.double_left.user",
            -1
        )
        migrateStringPreference(oldPrefs, this, "action_rightApp", "action.right.app", "")
        migrateIntPreference(
            oldPrefs,
            this,
            "action_rightApp_user",
            "action.right.user",
            -1
        )
        migrateStringPreference(
            oldPrefs,
            this,
            "action_right_topApp",
            "action.right_top.app",
            ""
        )
        migrateIntPreference(
            oldPrefs,
            this,
            "action_right_topApp_user",
            "action.right_top.user",
            -1
        )
        migrateStringPreference(
            oldPrefs,
            this,
            "action_right_bottomApp",
            "action.right_bottom.app",
            ""
        )
        migrateIntPreference(
            oldPrefs,
            this,
            "action_right_bottomApp_user",
            "action.right_bottom.user",
            -1
        )
        migrateStringPreference(
            oldPrefs,
            this,
            "action_doubleRightApp",
            "action.double_right.app",
            ""
        )
        migrateIntPreference(
            oldPrefs,
            this,
            "action_doubleRightApp_user",
            "action.double_right.user",
            -1
        )
        migrateBooleanPreference(oldPrefs, this, "timeVisible", "clock.time_visible", true)
        migrateBooleanPreference(oldPrefs, this, "dateVisible", "clock.date_visible", true)
        migrateBooleanPreference(
            oldPrefs,
            this,
            "dateLocalized",
            "clock.date_localized",
            false
        )
        migrateBooleanPreference(
            oldPrefs,
            this,
            "dateTimeFlip",
            "clock.date_time_flip",
            false
        )
        migrateBooleanPreference(
            oldPrefs,
            this,
            "disableTimeout",
            "display.disable_timeout",
            false
        )
        migrateBooleanPreference(
            oldPrefs,
            this,
            "useFullScreen",
            "display.use_full_screen",
            true
        )
        migrateBooleanPreference(
            oldPrefs,
            this,
            "enableDoubleActions",
            "enabled_gestures.double_actions",
            true
        )
        migrateBooleanPreference(
            oldPrefs,
            this,
            "enableEdgeActions",
            "enabled_gestures.edge_actions",
            true
        )
        migrateBooleanPreference(
            oldPrefs,
            this,
            "searchAutoLaunch",
            "functionality.search_auto_launch",
            true
        )
        migrateBooleanPreference(
            oldPrefs,
            this,
            "searchAutoKeyboard",
            "functionality.search_auto_keyboard",
            true
        )
    }

    when (oldPrefs.getString("theme", "finn")) {
        "finn" -> {
            LauncherPreferences.theme().colorTheme(ColorTheme.DEFAULT)
            LauncherPreferences.theme().monochromeIcons(false)
            LauncherPreferences.theme().background(Background.DIM)
        }

        "dark" -> {
            LauncherPreferences.theme().colorTheme(ColorTheme.DARK)
            LauncherPreferences.theme().monochromeIcons(true)
            LauncherPreferences.theme().background(Background.DIM)
        }
    }
    LauncherPreferences.internal().versionCode(1)
    Log.i(TAG, "migrated preferences to version 1.")

    migratePreferencesFromVersion1(context)
}