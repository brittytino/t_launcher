package app.olauncher.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.olauncher.TLauncherApplication
import app.olauncher.domain.engine.BlockingChecker
import app.olauncher.features.blocking.BlockingOverlayActivity
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsageMonitorWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val container = (applicationContext as TLauncherApplication).container
    private val categoryRepository = container.categoryRepository
    private val rulesRepository = container.rulesRepository
    private val usageStatsRepository = container.usageStatsRepository
    private val modeManager = container.modeManager
    private val blockingChecker = BlockingChecker()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 1. Get foreground app
            val foregroundPackage = usageStatsRepository.getCurrentForegroundApp()
            
            if (foregroundPackage != null && foregroundPackage != applicationContext.packageName) {
                checkAndEnforce(foregroundPackage)
            }
            
            // 2. We could also check for daily limit resets here if needed, 
            // but that's better handled by a midnight alarm or just lazy calculation.

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun checkAndEnforce(packageName: String) {
        val category = categoryRepository.getCategory(packageName) ?: return
        val rules = rulesRepository.getRulesForPackage(packageName)
        val usage = usageStatsRepository.getTodayUsage(packageName)
        val currentMode = modeManager.getMode()

        val result = blockingChecker.isAppBlocked(
            category = category,
            rules = rules,
            usageDuration = usage,
            currentMode = currentMode
        )

        if (result is BlockingChecker.BlockingResult.Blocked) {
            // If blocked, launch overlay
            val intent = Intent(applicationContext, BlockingOverlayActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.putExtra("BLOCKING_REASON", result.reason.name)
            applicationContext.startActivity(intent)
        }
    }
}
