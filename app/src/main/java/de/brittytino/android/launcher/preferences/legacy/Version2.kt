package de.brittytino.android.launcher.preferences.legacy

import android.content.Context
import de.brittytino.android.launcher.actions.Action
import de.brittytino.android.launcher.actions.Gesture
import de.brittytino.android.launcher.actions.LauncherAction
import de.brittytino.android.launcher.preferences.LauncherPreferences
import de.brittytino.android.launcher.preferences.PREFERENCE_VERSION


/**
 * Migrate preferences from version 2 (used until version 0.0.21) to the current format
 * (see [PREFERENCE_VERSION])
 */
fun migratePreferencesFromVersion2(context: Context) {
    assert(LauncherPreferences.internal().versionCode() == 2)
    // previously there was no setting for this
    Action.setActionForGesture(Gesture.BACK, LauncherAction.CHOOSE)
    LauncherPreferences.internal().versionCode(3)
    migratePreferencesFromVersion3(context)
}