package app.olauncher.domain.model

enum class CategoryType {
    ESSENTIAL,      // Phone, Maps, Messages, Clock, Calendar, Settings
    PRODUCTIVE,     // Notes, Tasks, Banking, Utilities, Learning
    DISTRACTING,    // Social Media, Games, News, Streaming
    SYSTEM,         // Icons, Launchers, Wallpapers
    
    // Granular Types (Required by BondingChecker / Enforcement)
    PHONE,
    MAPS,
    MESSAGING,
    MUSIC,
    UTILITY,
    SOCIAL,
    GAME,
    NEWS,
    OTHER
}

data class AppCategory(
    val packageName: String,
    val type: CategoryType,
    val isWhitelisted: Boolean = false,
    val isManualOverride: Boolean = false
)
