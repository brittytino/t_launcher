package de.brittytino.android.launcher.data

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.brittytino.android.launcher.preferences.LauncherPreferences

class FocusModeRepository(context: Context) {
    // Use the main default shared preferences so listeners in the launcher (like AppList) 
    // are notified when we change "is_active", triggering a UI refresh automatically.
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val gson = Gson()

    var focusState: FocusState
        get() = FocusState.valueOf(prefs.getString(KEY_FOCUS_STATE, FocusState.INACTIVE.name) ?: FocusState.INACTIVE.name)
        set(value) {
            prefs.edit()
                .putString(KEY_FOCUS_STATE, value.name)
                // Backward compatibility: ACTIVE/PAUSED/UNLOCK_PENDING are all "active" sessions logic-wise
                // but consumers should check focusState for granular control.
                .putBoolean(KEY_IS_ACTIVE, value != FocusState.INACTIVE)
                .commit() 
        }

    var previousFocusState: FocusState
        get() = FocusState.valueOf(prefs.getString(KEY_PREVIOUS_FOCUS_STATE, FocusState.INACTIVE.name) ?: FocusState.INACTIVE.name)
        set(value) {
            prefs.edit().putString(KEY_PREVIOUS_FOCUS_STATE, value.name).commit()
        }

    // Deprecated: use focusState instead
    val isFocusActive: Boolean
        get() = focusState != FocusState.INACTIVE

    var focusApps: Set<String>
        get() = prefs.getStringSet(KEY_FOCUS_APPS, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_FOCUS_APPS, value).apply()

    var lockType: LockType
        get() = LockType.valueOf(prefs.getString(KEY_LOCK_TYPE, LockType.RANDOM_STRING.name) ?: LockType.RANDOM_STRING.name)
        set(value) = prefs.edit().putString(KEY_LOCK_TYPE, value.name).apply()

    var customPassword: String?
        get() = prefs.getString(KEY_CUSTOM_PASSWORD, null)
        set(value) {
            prefs.edit().putString(KEY_CUSTOM_PASSWORD, value).commit()
        }

    var isQuietMode: Boolean
        get() = prefs.getBoolean(KEY_QUIET_MODE, false)
        set(value) { 
            prefs.edit().putBoolean(KEY_QUIET_MODE, value).apply() 
        }
        
    var currentSessionPhrase: String?
        get() = prefs.getString(KEY_SESSION_PHRASE, null)
        set(value) {
            prefs.edit().putString(KEY_SESSION_PHRASE, value).commit()
        }

    var pauseTimeRemaining: Long
        get() = prefs.getLong(KEY_PAUSE_TIME_REMAINING, 120000L) // 2 minutes default
        set(value) = prefs.edit().putLong(KEY_PAUSE_TIME_REMAINING, value).apply()

    var lastPauseTimestamp: Long
        get() = prefs.getLong(KEY_LAST_PAUSE_TIMESTAMP, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_PAUSE_TIMESTAMP, value).apply()

    fun isAppAllowed(packageName: String): Boolean {
        // Always allow system essentials and the launcher itself
        if (packageName == "com.android.systemui" || 
            packageName == "com.android.settings" || // Often needed
            packageName == "com.google.android.dialer" || // Phone
            packageName == "com.android.dialer" ||
            packageName == "de.brittytino.android.launcher") {
            return true
        }
        return focusApps.contains(packageName)
    }

    companion object {
        private const val KEY_FOCUS_STATE = "focus_state"
        private const val KEY_IS_ACTIVE = "is_active" // Legacy bool
        private const val KEY_FOCUS_APPS = "focus_apps"
        private const val KEY_LOCK_TYPE = "lock_type"
        private const val KEY_CUSTOM_PASSWORD = "custom_password"
        private const val KEY_QUIET_MODE = "quiet_mode"
        private const val KEY_SESSION_PHRASE = "session_phrase"
        private const val KEY_PREVIOUS_FOCUS_STATE = "previous_focus_state"
        private const val KEY_PAUSE_TIME_REMAINING = "pause_time_remaining"
        private const val KEY_LAST_PAUSE_TIMESTAMP = "last_pause_timestamp"
    }

    enum class LockType {
        RANDOM_STRING,
        CUSTOM_PASSWORD
    }

    enum class FocusState {
        INACTIVE,
        ACTIVE,
        PAUSED,
        UNLOCK_PENDING
    }
}
