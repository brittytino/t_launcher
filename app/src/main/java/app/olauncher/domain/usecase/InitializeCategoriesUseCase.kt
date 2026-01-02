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
        return when {
            packageName.contains("dialer") -> CategoryType.ESSENTIAL
            packageName.contains("mms") -> CategoryType.ESSENTIAL
            packageName.contains("contacts") -> CategoryType.ESSENTIAL
            packageName.contains("maps") -> CategoryType.ESSENTIAL
            packageName.contains("settings") -> CategoryType.NEUTRAL
            packageName.contains("camera") -> CategoryType.NEUTRAL
            packageName.contains("calculator") -> CategoryType.NEUTRAL
            packageName.contains("calendar") -> CategoryType.PRODUCTIVE
            packageName.contains("clock") -> CategoryType.ESSENTIAL
            // Social/Procrastination defaults
            packageName.contains("facebook") -> CategoryType.PROCRASTINATING
            packageName.contains("instagram") -> CategoryType.PROCRASTINATING
            packageName.contains("tiktok") -> CategoryType.PROCRASTINATING
            packageName.contains("youtube") -> CategoryType.PROCRASTINATING
            packageName.contains("twitter") -> CategoryType.PROCRASTINATING
            packageName.contains("reddit") -> CategoryType.PROCRASTINATING
            packageName.contains("game") -> CategoryType.PROCRASTINATING
            else -> CategoryType.NEUTRAL
        }
    }
}
