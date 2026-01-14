package de.brittytino.android.launcher.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import de.brittytino.android.launcher.leetcode.data.LeetCodeDao
import de.brittytino.android.launcher.leetcode.data.LeetCodeUserEntity

import de.brittytino.android.launcher.leetcode.data.DailyProblemEntity

@Database(entities = [AppLaunchDelayEntity::class, LeetCodeUserEntity::class, DailyProblemEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appLaunchDelayDao(): AppLaunchDelayDao
    abstract fun leetCodeDao(): LeetCodeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "launcher_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
