package app.olauncher.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import app.olauncher.TLauncherApplication
import app.olauncher.data.Prefs
import app.olauncher.domain.engine.BlockingChecker
import app.olauncher.domain.repository.UsageStatsRepository
import app.olauncher.features.blocking.BlockingOverlayActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class EnforcementService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    
    // Dependencies
    private val blockingChecker by lazy { BlockingChecker() }
    private val container by lazy { (application as TLauncherApplication).container }
    private val categoryRepository by lazy { container.categoryRepository }
    private val usageStatsRepository by lazy { container.usageStatsRepository }
    private val rulesRepository by lazy { container.rulesRepository }

    // Cache to avoid db hits on every event (critical for performance)
    private val blockedPackagesCache = mutableSetOf<String>()
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        Prefs(applicationContext).lockModeOn = true
        // Here we should load initial rules/categories
        // For now, let's just listen.
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Optimization: Ignore self
            if (packageName == applicationContext.packageName) return 

            serviceScope.launch {
                checkAndBlock(packageName)
            }
        }
    }

    private suspend fun checkAndBlock(packageName: String) {
        val category = categoryRepository.getCategory(packageName) ?: return
        val rules = rulesRepository.getRulesForPackage(packageName)
        val usage = usageStatsRepository.getTodayUsage(packageName)

        val result = blockingChecker.isAppBlocked(
            category = category,
            rules = rules,
            usageDuration = usage
        )

        if (result is BlockingChecker.BlockingResult.Blocked) {
            showBlockingOverlay()
        }
    }

    private fun showBlockingOverlay() {
        val intent = Intent(this, BlockingOverlayActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) // Make sure it's fresh
        startActivity(intent)
    }

    override fun onInterrupt() {
        // Service interrupted
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
