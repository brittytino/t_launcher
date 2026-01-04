package app.olauncher.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val isCompleted: Boolean = false,
    val timestamp: Long
)
