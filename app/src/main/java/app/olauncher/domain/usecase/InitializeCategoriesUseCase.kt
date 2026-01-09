package app.olauncher.domain.usecase

import app.olauncher.domain.model.AppCategory
import app.olauncher.domain.model.CategoryType
import app.olauncher.domain.repository.AppInfoRepository
import app.olauncher.domain.repository.CategoryRepository

class InitializeCategoriesUseCase(
    private val categoryRepository: CategoryRepository,
    private val appInfoRepository: AppInfoRepository
) {
    suspend operator fun invoke() {
        val installedApps = appInfoRepository.getAllInstalledApps()
        val existingCategories = categoryRepository.getAllCategoriesSync().associateBy { it.packageName }
        
        // Optimize: Bulk check/insert
        val newCategories = mutableListOf<AppCategory>()

        installedApps.forEach { app ->
            if (!existingCategories.containsKey(app.packageName)) {
                // Heuristic for default category
                val type = determineDefaultCategory(app.packageName)
                newCategories.add(
                    AppCategory(
                        packageName = app.packageName,
                        type = type,
                        isWhitelisted = false
                    )
                )
            }
        }

        if (newCategories.isNotEmpty()) {
            categoryRepository.insertCategories(newCategories)
        }
    }

    private fun determineDefaultCategory(packageName: String): CategoryType {
        val lower = packageName.lowercase()
        return when {
            // Essential
            lower.contains("dialer") -> CategoryType.ESSENTIAL
            lower.contains("contacts") -> CategoryType.ESSENTIAL
            lower.contains("telecom") -> CategoryType.ESSENTIAL
            lower.contains("mms") -> CategoryType.ESSENTIAL
            lower.contains("message") -> CategoryType.ESSENTIAL
            lower.contains("clock") -> CategoryType.ESSENTIAL // Alarm/Timer needed
            lower.contains("calendar") -> CategoryType.ESSENTIAL
            lower.contains("settings") -> CategoryType.ESSENTIAL
            lower.contains("maps") -> CategoryType.ESSENTIAL // Navigation is essential
            lower.contains("waze") -> CategoryType.ESSENTIAL
            
            // Productive
            lower.contains("note") -> CategoryType.PRODUCTIVE
            lower.contains("task") -> CategoryType.PRODUCTIVE
            lower.contains("doc") -> CategoryType.PRODUCTIVE
            lower.contains("drive") -> CategoryType.PRODUCTIVE
            lower.contains("sheet") -> CategoryType.PRODUCTIVE
            lower.contains("slide") -> CategoryType.PRODUCTIVE
            lower.contains("calculator") -> CategoryType.PRODUCTIVE
            lower.contains("bank") -> CategoryType.PRODUCTIVE
            lower.contains("wallet") -> CategoryType.PRODUCTIVE
            lower.contains("pay") -> CategoryType.PRODUCTIVE
            lower.contains("mail") -> CategoryType.PRODUCTIVE // Email can be distracting but usually work
            lower.contains("files") -> CategoryType.PRODUCTIVE
            lower.contains("camera") -> CategoryType.PRODUCTIVE // Creative tool

            // System
            lower.contains("android") -> CategoryType.SYSTEM
            lower.contains("systemui") -> CategoryType.SYSTEM
            lower.contains("launcher") -> CategoryType.SYSTEM
            lower.contains("olauncher") -> CategoryType.SYSTEM
            lower.contains("google.android.gms") -> CategoryType.SYSTEM
            lower.contains("vending") -> CategoryType.SYSTEM // Play Store

            // Distracting (Default for everything else to be safe/strict)
            lower.contains("social") -> CategoryType.DISTRACTING
            lower.contains("instagram") -> CategoryType.DISTRACTING
            lower.contains("facebook") -> CategoryType.DISTRACTING
            lower.contains("twitter") -> CategoryType.DISTRACTING
            lower.contains("reddit") -> CategoryType.DISTRACTING
            lower.contains("tiktok") -> CategoryType.DISTRACTING
            lower.contains("youtube") -> CategoryType.DISTRACTING
            lower.contains("netflix") -> CategoryType.DISTRACTING
            lower.contains("prime") -> CategoryType.DISTRACTING
            lower.contains("hulu") -> CategoryType.DISTRACTING
            lower.contains("disney") -> CategoryType.DISTRACTING
            lower.contains("game") -> CategoryType.DISTRACTING
            lower.contains("news") -> CategoryType.DISTRACTING
            lower.contains("shop") -> CategoryType.DISTRACTING
            lower.contains("browser") -> CategoryType.DISTRACTING // Chrome etc often distracting. User can override.
            lower.contains("chrome") -> CategoryType.DISTRACTING
            
            else -> CategoryType.DISTRACTING // Default to Distracting to enforce potential white-listing
        }
    }
}
