package app.olauncher.domain.usecase

import app.olauncher.domain.repository.CategoryRepository

class ToggleWhitelistUseCase(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(packageName: String): Boolean {
        val category = categoryRepository.getCategory(packageName) ?: return false
        val newStatus = !category.isWhitelisted
        categoryRepository.updateCategory(category.copy(isWhitelisted = newStatus))
        return newStatus
    }
}
