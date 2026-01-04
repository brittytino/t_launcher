package app.olauncher.data.local

import androidx.room.TypeConverter
import app.olauncher.domain.model.CategoryType

class Converters {
    @TypeConverter
    fun fromCategoryType(value: CategoryType): String {
        return value.name
    }

    @TypeConverter
    fun toCategoryType(value: String): CategoryType {
        return try {
            CategoryType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            CategoryType.OTHER // Default fallback
        }
    }
}
