package app.olauncher.data.local

import app.olauncher.domain.model.UsageRule
import java.time.format.DateTimeFormatter

object RuleSerializer {
    private val TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME

    fun serialize(rule: UsageRule): Pair<String, String> {
        return when (rule) {
            is UsageRule.StrictBlock -> "STRICT" to ""
            is UsageRule.DailyLimit -> "DAILY" to rule.limitMinutes.toString()
            is UsageRule.ScheduledBlock -> {
                val start = rule.startTime.format(TIME_FORMATTER)
                val end = rule.endTime.format(TIME_FORMATTER)
                val days = rule.daysOfWeek.joinToString(",") { it.name }
                "SCHEDULE" to "$start|$end|$days"
            }
        }
    }

    fun deserialize(type: String, data: String): UsageRule? {
        return try {
            when (type) {
                "STRICT" -> UsageRule.StrictBlock
                "DAILY" -> UsageRule.DailyLimit(data.toInt())
                "SCHEDULE" -> {
                    val parts = data.split("|")
                    val start = java.time.LocalTime.parse(parts[0], TIME_FORMATTER)
                    val end = java.time.LocalTime.parse(parts[1], TIME_FORMATTER)
                    val days = parts[2].split(",").map { java.time.DayOfWeek.valueOf(it) }
                    UsageRule.ScheduledBlock(start, end, days)
                }
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
