package app.olauncher.domain.engine

import app.olauncher.domain.model.AppCategory
import app.olauncher.domain.model.CategoryType
import app.olauncher.domain.model.UsageRule
import java.time.LocalTime

class BlockingChecker {

    fun isAppBlocked(
        category: AppCategory,
        rules: List<UsageRule>,
        usageDuration: Long, // Today's usage in millis
        currentTime: LocalTime = LocalTime.now()
    ): BlockingResult {
        if (category.isWhitelisted) return BlockingResult.Allowed

        // 1. Check Strict Block
        if (rules.any { it is UsageRule.StrictBlock }) {
            return BlockingResult.Blocked(Reason.StrictBlock)
        }

        // 2. Check Category-based implicit rules
        if (category.type == CategoryType.PROCRASTINATING) {
            return BlockingResult.Blocked(Reason.CategoryRestricted)
        }

        // 3. Check Scheduled Blocks
        rules.filterIsInstance<UsageRule.ScheduledBlock>().forEach { rule ->
            if (currentTime.isAfter(rule.startTime) && currentTime.isBefore(rule.endTime)) {
                // Check Day of Week if needed, assuming rules passed here are active for today
                return BlockingResult.Blocked(Reason.Scheduled)
            }
        }

        // 4. Check Daily Limit
        rules.filterIsInstance<UsageRule.DailyLimit>().forEach { rule ->
            if (usageDuration >= rule.limitMinutes * 60 * 1000) {
                return BlockingResult.Blocked(Reason.TimeLimitExceeded)
            }
        }
        
        return BlockingResult.Allowed
    }

    sealed class BlockingResult {
        data object Allowed : BlockingResult()
        data class Blocked(val reason: Reason) : BlockingResult()
    }

    enum class Reason {
        StrictBlock,
        Scheduled,
        TimeLimitExceeded,
        CategoryRestricted
    }
}
