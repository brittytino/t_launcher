package app.olauncher.domain.repository

interface UsageStatsRepository {
    suspend fun getUsageStats(startTime: Long, endTime: Long): Map<String, Long>
    suspend fun getCurrentForegroundApp(): String?
    suspend fun getTodayUsage(packageName: String): Long
}
