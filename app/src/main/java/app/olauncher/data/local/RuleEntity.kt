package app.olauncher.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usage_rules")
data class RuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val ruleType: String, // STRICT, DAILY, SCHEDULE
    val ruleData: String // Serialized data
)
