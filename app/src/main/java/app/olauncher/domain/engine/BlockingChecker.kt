package app.olauncher.domain.engine

import app.olauncher.domain.model.AppCategory
import app.olauncher.domain.model.AppMode
import app.olauncher.domain.model.CategoryType
import app.olauncher.domain.model.UsageRule
import java.time.LocalTime

class BlockingChecker {

    fun isAppBlocked(
        category: AppCategory,
        rules: List<UsageRule>,
        usageDuration: Long, // Today's usage in millis
        currentMode: AppMode,
        currentTime: LocalTime = LocalTime.now()
    ): BlockingResult {
        // 0. EMERGENCY MODE:
        if (currentMode == AppMode.EMERGENCY) {
            // Allow Phone, Maps, System, Utilities (Clock/Calc)
            // Explicit Block of Social/Games/News
            return if (category.type == CategoryType.PHONE || 
                       category.type == CategoryType.MAPS || 
                       category.type == CategoryType.UTILITY || 
                       category.type == CategoryType.SYSTEM ||
                       category.type == CategoryType.MESSAGING) {
                 BlockingResult.Allowed
            } else {
                 BlockingResult.Blocked(Reason.EmergencyLockdown)
            }
        }
        
        // 1. DRIVING MODE: Strict whitelist
        if (currentMode == AppMode.DRIVING) {
             return if (category.type == CategoryType.PHONE || 
                        category.type == CategoryType.MAPS || 
                        category.type == CategoryType.MUSIC) {
                 BlockingResult.Allowed
             } else {
                 BlockingResult.Blocked(Reason.DrivingSafety)
             }
        }

        // 2. PRODUCTIVITY MODE: Block Social, Games, News
        if (currentMode == AppMode.PRODUCTIVITY) {
            if (category.type == CategoryType.SOCIAL || 
                category.type == CategoryType.GAME || 
                category.type == CategoryType.NEWS) {
                 if (!category.isWhitelisted) return BlockingResult.Blocked(Reason.ProductivityMode)
            }
            // Strict enforcement: Maybe block Unknown/Other unless whitelisted? 
            // For now, focus on known distractions.
        }
        
        // 3. BORED MODE: Block Social/Games
        if (currentMode == AppMode.BORED) {
             if (category.type == CategoryType.SOCIAL || 
                 category.type == CategoryType.GAME || 
                 category.type == CategoryType.NEWS) {
                 return BlockingResult.Blocked(Reason.BoredomIntervention)
             }
        }

        if (category.isWhitelisted) return BlockingResult.Allowed

        // 4. Check Strict Block (User Defined Rules)
        if (rules.any { it is UsageRule.StrictBlock }) {
            return BlockingResult.Blocked(Reason.StrictBlock)
        }

        // 5. Check Category-based implicit rules
        if (category.type == CategoryType.SOCIAL || category.type == CategoryType.GAME) {
            // By default, maybe allow unless in specific mode? 
            // Or strictly block if user set a global rule. 
            // In this architecture, implicit rules are handled by Mode.
            // But if user manually added a rule, it fell into step 4 (StrictBlock).
        }

        // 6. Check Scheduled Blocks
        rules.filterIsInstance<UsageRule.ScheduledBlock>().forEach { rule ->
            if (currentTime.isAfter(rule.startTime) && currentTime.isBefore(rule.endTime)) {
                return BlockingResult.Blocked(Reason.Scheduled)
            }
        }

        // 7. Check Daily Limit
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
        CategoryRestricted,
        BoredomIntervention,
        ProductivityMode,
        DrivingSafety,
        EmergencyLockdown,
        ContinuousUsageLimit
    }
}
