package app.olauncher.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(
    entities = [
        CategoryEntity::class,
        UsageSessionEntity::class,
        RuleEntity::class,
        NoteEntity::class,
        TaskEntity::class,
        AccountabilityEntity::class,
        SystemLogEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TLauncherDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun usageDao(): UsageDao
    abstract fun ruleDao(): RuleDao
    abstract fun productivityDao(): ProductivityDao
    abstract fun accountabilityDao(): AccountabilityDao
    abstract fun systemLogDao(): SystemLogDao
}
