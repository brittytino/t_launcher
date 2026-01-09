package app.olauncher.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.olauncher.domain.model.CategoryType

@Entity(tableName = "app_categories")
data class CategoryEntity(
    @PrimaryKey val packageName: String,
    val type: CategoryType,
    val isWhitelisted: Boolean,
    val isManualOverride: Boolean = false
)
