package de.brittytino.android.launcher.preferences.legacy

import android.content.Context
import de.brittytino.android.launcher.preferences.LauncherPreferences
import de.brittytino.android.launcher.widgets.ClockWidget
import de.brittytino.android.launcher.widgets.WidgetPanel
import de.brittytino.android.launcher.widgets.WidgetPosition
import de.brittytino.android.launcher.widgets.generateInternalId

fun migratePreferencesFromVersion4(context: Context) {
    assert(LauncherPreferences.internal().versionCode() < 100)

    LauncherPreferences.widgets().widgets(
        setOf(
            ClockWidget(
                generateInternalId(),
                WidgetPosition(1, 3, 10, 4),
                WidgetPanel.HOME.id
            )
        )
    )
    LauncherPreferences.internal().versionCode(100)
    migratePreferencesFromVersion100(context)
}