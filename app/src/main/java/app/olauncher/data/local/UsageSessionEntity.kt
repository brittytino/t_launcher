package app.olauncher.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usage_sessions")
data class UsageSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val startTime: Long,
    val endTime: Long,
    val durationMillis: Long,
    val date: String // ISO-8601 date string (YYYY-MM-DD) for easy grouping
)
