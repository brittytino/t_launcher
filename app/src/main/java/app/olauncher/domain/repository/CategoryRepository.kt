package app.olauncher.domain.repository

import app.olauncher.domain.model.AppCategory
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<AppCategory>>
    suspend fun getAllCategoriesSync(): List<AppCategory>
    suspend fun getCategory(packageName: String): AppCategory?
    suspend fun updateCategory(category: AppCategory)
    suspend fun insertCategories(categories: List<AppCategory>)
}
