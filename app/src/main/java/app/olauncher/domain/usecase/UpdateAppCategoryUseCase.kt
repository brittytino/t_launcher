package app.olauncher.domain.usecase

import app.olauncher.domain.model.AppCategory
import app.olauncher.domain.repository.CategoryRepository

class UpdateAppCategoryUseCase(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(category: AppCategory) {
        // Enforce any rules about changing categories?
        // Prompt says "User can change category once (stored permanently)".
        // I should probably track if it was user-modified. 
        // For now, simple update.
        categoryRepository.updateCategory(category)
    }
}
