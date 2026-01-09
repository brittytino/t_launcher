package app.olauncher.data.repository

import app.olauncher.data.local.CategoryDao
import app.olauncher.data.local.CategoryEntity
import app.olauncher.domain.model.AppCategory
import app.olauncher.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<AppCategory>> {
        return categoryDao.getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAllCategoriesSync(): List<AppCategory> {
        return categoryDao.getAllCategoriesOneShot().map { it.toDomain() }
    }

    override suspend fun getCategory(packageName: String): AppCategory? {
        return categoryDao.getCategory(packageName)?.toDomain()
    }

    override suspend fun updateCategory(category: AppCategory) {
        categoryDao.insertCategory(category.toEntity())
    }

    override suspend fun insertCategories(categories: List<AppCategory>) {
        categoryDao.insertCategories(categories.map { it.toEntity() })
    }

    private fun CategoryEntity.toDomain(): AppCategory {
        return AppCategory(
            packageName = this.packageName,
            type = this.type,
            isWhitelisted = this.isWhitelisted,
            isManualOverride = this.isManualOverride
        )
    }

    private fun AppCategory.toEntity(): CategoryEntity {
        return CategoryEntity(
            packageName = this.packageName,
            type = this.type,
            isWhitelisted = this.isWhitelisted,
            isManualOverride = this.isManualOverride
        )
    }
}
