package app.olauncher.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountabilityDao {
    @Query("SELECT * FROM accountability_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<AccountabilityEntity>>

    @Query("SELECT * FROM accountability_logs WHERE date = :date LIMIT 1")
    suspend fun getLogForDate(date: String): AccountabilityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AccountabilityEntity)
}
