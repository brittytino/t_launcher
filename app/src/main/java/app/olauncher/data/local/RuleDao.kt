package app.olauncher.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {
    @Query("SELECT * FROM usage_rules WHERE packageName = :packageName")
    suspend fun getRules(packageName: String): List<RuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: RuleEntity)

    @Query("DELETE FROM usage_rules WHERE packageName = :packageName")
    suspend fun clearRules(packageName: String)
}
