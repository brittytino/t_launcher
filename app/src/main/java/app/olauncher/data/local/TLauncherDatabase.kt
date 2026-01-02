package app.olauncher.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [CategoryEntity::class, UsageSessionEntity::class, RuleEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TLauncherDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun usageDao(): UsageDao
    abstract fun ruleDao(): RuleDao
}
