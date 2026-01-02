package app.olauncher.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: UsageSessionEntity)

    @Query("SELECT SUM(durationMillis) FROM usage_sessions WHERE packageName = :packageName AND date = :date")
    fun getUsageDurationForPackage(packageName: String, date: String): Flow<Long?>

    @Query("SELECT * FROM usage_sessions WHERE date = :date")
    fun getSessionsForDate(date: String): Flow<List<UsageSessionEntity>>
}
