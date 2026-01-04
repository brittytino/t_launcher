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
import app.olauncher.data.repository.ProductivityRepository
import app.olauncher.data.local.NoteEntity
import app.olauncher.data.local.TaskEntity

interface AppContainer {
    val categoryRepository: CategoryRepository
    val appInfoRepository: AppInfoRepository
    val usageStatsRepository: UsageStatsRepository
    val rulesRepository: RulesRepository
    val initializeCategoriesUseCase: InitializeCategoriesUseCase
    val updateAppCategoryUseCase: UpdateAppCategoryUseCase
    val toggleWhitelistUseCase: ToggleWhitelistUseCase
    val productivityRepository: app.olauncher.data.repository.ProductivityRepository
    val accountabilityRepository: app.olauncher.data.repository.AccountabilityRepository
    val modeManager: app.olauncher.domain.managers.ModeManager
    val systemLogRepository: app.olauncher.data.repository.SystemLogRepository
}

class AppContainerImpl(private val applicationContext: Context) : AppContainer {

    private val database: TLauncherDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            TLauncherDatabase::class.java,
            "tlauncher_db"
        ).fallbackToDestructiveMigration().build()
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
        RulesRepositoryImpl(database.ruleDao(), categoryRepository)
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

    override val productivityRepository: app.olauncher.data.repository.ProductivityRepository by lazy {
        app.olauncher.data.repository.ProductivityRepository(database.productivityDao())
    }

    override val accountabilityRepository: app.olauncher.data.repository.AccountabilityRepository by lazy {
        app.olauncher.data.repository.AccountabilityRepository(database.accountabilityDao())
    }

    override val modeManager: app.olauncher.domain.managers.ModeManager by lazy {
        app.olauncher.domain.managers.ModeManager(
            app.olauncher.data.Prefs(applicationContext),
            systemLogRepository
        )
    }

    override val systemLogRepository: app.olauncher.data.repository.SystemLogRepository by lazy {
        app.olauncher.data.repository.SystemLogRepository(database.systemLogDao())
    }
}
