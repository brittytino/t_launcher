package app.olauncher.domain.model

import java.time.LocalTime

sealed class UsageRule {
    data class DailyLimit(
        val limitMinutes: Int
    ) : UsageRule()

    data class ScheduledBlock(
        val startTime: LocalTime,
        val endTime: LocalTime,
        val daysOfWeek: List<java.time.DayOfWeek>
    ) : UsageRule()

    data object StrictBlock : UsageRule() // Always blocked
}
