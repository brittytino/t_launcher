package app.olauncher.data.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import app.olauncher.domain.repository.UsageStatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class UsageStatsRepositoryImpl(
    private val context: Context
) : UsageStatsRepository {

    private val usageStatsManager: UsageStatsManager? by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    }

    override suspend fun getUsageStats(startTime: Long, endTime: Long): Map<String, Long> = withContext(Dispatchers.IO) {
        val stats = usageStatsManager?.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: emptyList()

        stats.associate { it.packageName to it.totalTimeInForeground }
    }

    override suspend fun getCurrentForegroundApp(): String? = withContext(Dispatchers.IO) {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 10000 // Look back 10 seconds

        val events = usageStatsManager?.queryEvents(startTime, endTime) ?: return@withContext null
        val event = android.app.usage.UsageEvents.Event()
        
        var lastPackage: String? = null
        var lastEventTime = 0L

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND) {
                if (event.timeStamp > lastEventTime) {
                    lastEventTime = event.timeStamp
                    lastPackage = event.packageName
                }
            }
        }
        lastPackage
    }

    override suspend fun getTodayUsage(packageName: String): Long = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val stats = usageStatsManager?.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: return@withContext 0L

        // Aggregation might be needed if multiple entries exist
        stats.filter { it.packageName == packageName }
            .sumOf { it.totalTimeInForeground }
    }

    override suspend fun getTodayTotalUsage(): Long = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val stats = usageStatsManager?.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: return@withContext 0L

        stats.sumOf { it.totalTimeInForeground }
    }
}
