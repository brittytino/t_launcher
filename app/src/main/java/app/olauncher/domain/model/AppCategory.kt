package app.olauncher.domain.model

enum class CategoryType {
    PHONE,          // Dialer, Contacts
    MAPS,           // Maps, Navigation
    MUSIC,          // Spotify, Music Players
    MESSAGING,      // SMS, WhatsApp, Signal (Text only)
    SOCIAL,         // Instagram, Twitter, Facebook
    GAME,           // Games
    NEWS,           // News Apps, Reddit
    PRODUCTIVITY,   // Notes, Docs, Calendar
    UTILITY,        // Calculator, Clock, Camera, Settings
    SYSTEM,         // System UI
    OTHER           // Default for unknown
}

data class AppCategory(
    val packageName: String,
    val type: CategoryType,
    val isWhitelisted: Boolean = false
)
