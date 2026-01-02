package app.olauncher.domain.model

enum class CategoryType {
    ESSENTIAL,      // Phone, Maps, Banking, Urgent Tools
    PRODUCTIVE,     // Work, Study, Reading, Notes
    NEUTRAL,        // System apps, Calculators, benign tools
    PROCRASTINATING // Social Media, Games, Video Streaming, News
}

data class AppCategory(
    val packageName: String,
    val type: CategoryType,
    val isWhitelisted: Boolean = false // User override for specific needs
)
