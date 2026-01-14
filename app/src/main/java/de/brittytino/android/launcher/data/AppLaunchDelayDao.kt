package de.brittytino.android.launcher.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppLaunchDelayDao {
    @Query("SELECT * FROM app_launch_delay WHERE packageName = :packageName")
    suspend fun getDelayForApp(packageName: String): AppLaunchDelayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: AppLaunchDelayEntity)

    @Query("SELECT * FROM app_launch_delay")
    fun getAllDelays(): Flow<List<AppLaunchDelayEntity>>
    
    @Query("DELETE FROM app_launch_delay WHERE packageName = :packageName")
    suspend fun delete(packageName: String)
}
