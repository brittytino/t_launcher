package de.brittytino.android.launcher.leetcode.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LeetCodeDao {
    @Query("SELECT * FROM leetcode_users WHERE isMe = 1 LIMIT 1")
    fun getMyProfile(): Flow<LeetCodeUserEntity?>

    @Query("SELECT * FROM leetcode_users WHERE isMe = 0")
    fun getFriends(): Flow<List<LeetCodeUserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: LeetCodeUserEntity)

    @Query("SELECT * FROM leetcode_users WHERE username = :username")
    suspend fun getUser(username: String): LeetCodeUserEntity?
    
    @Query("DELETE FROM leetcode_users WHERE username = :username")
    suspend fun deleteUser(username: String)

    @Query("DELETE FROM leetcode_users WHERE isMe = 0")
    suspend fun clearFriends()

    @Query("DELETE FROM leetcode_users")
    suspend fun clearAll()

    @Query("SELECT * FROM daily_problems ORDER BY date DESC LIMIT 1")
    fun getDailyProblem(): Flow<DailyProblemEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyProblem(problem: DailyProblemEntity)
}
