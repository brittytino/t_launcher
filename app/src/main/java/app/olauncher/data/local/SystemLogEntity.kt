package app.olauncher.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "system_logs")
data class SystemLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val type: LogType,
    val message: String,
    val payload: String? = null // Optional JSON or extra data
)

enum class LogType {
    MODE_CHANGE,
    VIOLATION,
    ALARM_FAILURE,
    ALARM_SUCCESS,
    EMERGENCY_OVERRIDE,
    SYSTEM_EVENT,
    FOCUS_SESSION,
    BREATHING_SESSION,
    MISSED_CHECKIN,
    CHECKIN_COMPLETE,
    APP_BLOCK_EVENT
}
