package app.olauncher.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM app_categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM app_categories")
    suspend fun getAllCategoriesOneShot(): List<CategoryEntity>

    @Query("SELECT * FROM app_categories WHERE packageName = :packageName")
    suspend fun getCategory(packageName: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("DELETE FROM app_categories WHERE packageName = :packageName")
    suspend fun deleteCategory(packageName: String)
}
