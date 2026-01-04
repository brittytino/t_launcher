package app.olauncher.data.repository

import app.olauncher.data.local.SystemLogDao
import app.olauncher.data.local.SystemLogEntity
import kotlinx.coroutines.flow.Flow

class SystemLogRepository(private val dao: SystemLogDao) {
    
    val recentLogs: Flow<List<SystemLogEntity>> = dao.getRecentLogs()

    suspend fun logEvent(log: SystemLogEntity) {
        dao.insertLog(log)
    }

    suspend fun getViolationCountToday(): Int {
        val todayStart = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }.timeInMillis
        // Assuming VIOLATION type logic will be handled by filtering or specific query
        // For now, simpler to use the DAO method if added, or rely on caller for filter.
        // Let's use the DAO method we added: getCountByTypeSince
        return dao.getCountByTypeSince(app.olauncher.data.local.LogType.VIOLATION, todayStart)
    }
    suspend fun getCountByTypeSince(type: app.olauncher.data.local.LogType, since: Long): Int {
        return dao.getCountByTypeSince(type, since)
    }
}
