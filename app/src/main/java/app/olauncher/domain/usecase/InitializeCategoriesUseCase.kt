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
            packageName.contains("dialer") -> CategoryType.PHONE
            packageName.contains("contacts") -> CategoryType.PHONE
            packageName.contains("telecom") -> CategoryType.PHONE
            
            packageName.contains("maps") -> CategoryType.MAPS
            packageName.contains("waze") -> CategoryType.MAPS
            packageName.contains("nav") -> CategoryType.MAPS
            
            packageName.contains("mms") -> CategoryType.MESSAGING
            packageName.contains("message") -> CategoryType.MESSAGING
            packageName.contains("whatsapp") -> CategoryType.MESSAGING
            packageName.contains("signal") -> CategoryType.MESSAGING
            packageName.contains("telegram") -> CategoryType.MESSAGING
            
            packageName.contains("music") -> CategoryType.MUSIC
            packageName.contains("spotify") -> CategoryType.MUSIC
            packageName.contains("audio") -> CategoryType.MUSIC
            packageName.contains("podcast") -> CategoryType.MUSIC
            
            packageName.contains("settings") -> CategoryType.UTILITY
            packageName.contains("camera") -> CategoryType.UTILITY
            packageName.contains("calculator") -> CategoryType.UTILITY
            packageName.contains("clock") -> CategoryType.UTILITY
            
            packageName.contains("calendar") -> CategoryType.PRODUCTIVITY
            packageName.contains("note") -> CategoryType.PRODUCTIVITY
            packageName.contains("task") -> CategoryType.PRODUCTIVITY
            packageName.contains("doc") -> CategoryType.PRODUCTIVITY
            packageName.contains("drive") -> CategoryType.PRODUCTIVITY
            
            // Social/Procrastination defaults
            packageName.contains("facebook") -> CategoryType.SOCIAL
            packageName.contains("instagram") -> CategoryType.SOCIAL
            packageName.contains("tiktok") -> CategoryType.SOCIAL
            packageName.contains("twitter") -> CategoryType.SOCIAL
            packageName.contains("reddit") -> CategoryType.NEWS
            packageName.contains("youtube") -> CategoryType.NEWS // Video = News/Ent
            packageName.contains("netflix") -> CategoryType.NEWS // Video
            packageName.contains("game") -> CategoryType.GAME
            
            else -> CategoryType.OTHER
        }
    }
}
