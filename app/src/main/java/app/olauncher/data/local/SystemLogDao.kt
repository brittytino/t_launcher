package app.olauncher.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SystemLogDao {
    @Insert
    suspend fun insertLog(log: SystemLogEntity)

    @Query("SELECT * FROM system_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 50): Flow<List<SystemLogEntity>>

    @Query("SELECT COUNT(*) FROM system_logs WHERE type = :type AND timestamp >= :since")
    suspend fun getCountByTypeSince(type: LogType, since: Long): Int
}
