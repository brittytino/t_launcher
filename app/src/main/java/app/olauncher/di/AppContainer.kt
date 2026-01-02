package app.olauncher.di

import android.content.Context
import androidx.room.Room
import app.olauncher.data.local.TLauncherDatabase
import app.olauncher.data.repository.AppInfoRepositoryImpl
import app.olauncher.data.repository.CategoryRepositoryImpl
import app.olauncher.data.repository.RulesRepositoryImpl
import app.olauncher.data.repository.UsageStatsRepositoryImpl
import app.olauncher.domain.repository.AppInfoRepository
import app.olauncher.domain.repository.CategoryRepository
import app.olauncher.domain.repository.RulesRepository
import app.olauncher.domain.repository.UsageStatsRepository
import app.olauncher.domain.usecase.InitializeCategoriesUseCase
import app.olauncher.domain.usecase.ToggleWhitelistUseCase
import app.olauncher.domain.usecase.UpdateAppCategoryUseCase

interface AppContainer {
    val categoryRepository: CategoryRepository
    val appInfoRepository: AppInfoRepository
    val usageStatsRepository: UsageStatsRepository
    val rulesRepository: RulesRepository
    val initializeCategoriesUseCase: InitializeCategoriesUseCase
    val updateAppCategoryUseCase: UpdateAppCategoryUseCase
    val toggleWhitelistUseCase: ToggleWhitelistUseCase
}

class AppContainerImpl(private val applicationContext: Context) : AppContainer {

    private val database: TLauncherDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            TLauncherDatabase::class.java,
            "tlauncher_db"
        ).build()
    }

    override val categoryRepository: CategoryRepository by lazy {
        CategoryRepositoryImpl(database.categoryDao())
    }

    override val appInfoRepository: AppInfoRepository by lazy {
        AppInfoRepositoryImpl(applicationContext)
    }

    override val usageStatsRepository: UsageStatsRepository by lazy {
        UsageStatsRepositoryImpl(applicationContext)
    }

    override val rulesRepository: RulesRepository by lazy {
        RulesRepositoryImpl(database.ruleDao())
    }

    override val initializeCategoriesUseCase: InitializeCategoriesUseCase by lazy {
        InitializeCategoriesUseCase(categoryRepository, appInfoRepository)
    }

    override val updateAppCategoryUseCase: UpdateAppCategoryUseCase by lazy {
        UpdateAppCategoryUseCase(categoryRepository)
    }

    override val toggleWhitelistUseCase: ToggleWhitelistUseCase by lazy {
        ToggleWhitelistUseCase(categoryRepository)
    }
}
