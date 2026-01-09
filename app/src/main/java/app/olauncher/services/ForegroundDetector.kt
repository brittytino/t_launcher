package app.olauncher.services

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build

object ForegroundDetector {

    fun getForegroundPackage(context: Context): String? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return null

        val endTime = System.currentTimeMillis()
        val startTime = endTime - 10000 // Look back 10 seconds

        val events = usageStatsManager.queryEvents(startTime, endTime) ?: return null
        val usageEvent = UsageEvents.Event()

        var lastEventTime = 0L
        var foregroundPackage: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(usageEvent)
            if (usageEvent.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                if (usageEvent.timeStamp > lastEventTime) {
                    lastEventTime = usageEvent.timeStamp
                    foregroundPackage = usageEvent.packageName
                }
            }
        }
        
        return foregroundPackage
    }
}
