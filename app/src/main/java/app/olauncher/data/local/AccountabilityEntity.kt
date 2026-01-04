package app.olauncher.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accountability_logs")
data class AccountabilityEntity(
    @PrimaryKey
    val date: String, // Format: YYYY-MM-DD
    val dietFollowed: Boolean,
    val sugarFree: Boolean,
    val didWorkout: Boolean,
    val productiveToday: Boolean
)
