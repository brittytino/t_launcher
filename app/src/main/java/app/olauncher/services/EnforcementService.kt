package app.olauncher.services

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import android.os.Build
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
import kotlinx.coroutines.withContext
import kotlinx.coroutines.isActive

class EnforcementService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    
    // Dependencies
    private val blockingChecker by lazy { BlockingChecker() }
    private val container by lazy { (application as TLauncherApplication).container }
    private val categoryRepository by lazy { container.categoryRepository }
    private val usageStatsRepository by lazy { container.usageStatsRepository }
    private val modeManager by lazy { container.modeManager }
    private val rulesRepository by lazy { container.rulesRepository }

    private val lockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "app.olauncher.ACTION_LOCK_SCREEN") {
                performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            }
        }
    }
    
    private var sessionDurationLimit: Long = 5 * 60 * 1000L
    private var isExtensionGranted: Boolean = false

    private val extensionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "app.olauncher.ACTION_EXTEND_SESSION") {
                if (!isExtensionGranted) {
                    isExtensionGranted = true
                    sessionDurationLimit += 2 * 60 * 1000L // Add 2 minutes
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val prefs = Prefs(applicationContext)
        prefs.lockModeOn = true
        
        val filter = IntentFilter("app.olauncher.ACTION_LOCK_SCREEN")
        val extendFilter = IntentFilter("app.olauncher.ACTION_EXTEND_SESSION")
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(lockReceiver, filter, RECEIVER_NOT_EXPORTED)
            registerReceiver(extensionReceiver, extendFilter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(lockReceiver, filter)
            registerReceiver(extensionReceiver, extendFilter)
        }
        
        // Restore state or detection
        serviceScope.launch(Dispatchers.IO) {
            val foreground = ForegroundDetector.getForegroundPackage(applicationContext)
            if (foreground != null) {
                withContext(Dispatchers.Main) {
                    currentAppPackage = foreground
                    startSessionMonitor(foreground)
                }
            }
        }
    }

    private var currentAppPackage: String? = null
    private var currentSessionStart: Long = 0
    private var sessionMonitorJob: Job? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Update detection
            currentAppPackage = packageName
            
            // Check for boredom (rapid switching)
            checkForBoredom(packageName)
            
            // Run Blocking Checks
            serviceScope.launch(Dispatchers.IO) {
                checkAndBlock(packageName)
            }
            
            // Start/Switch Session Monitor
            startSessionMonitor(packageName)
        }
    }

    private fun startSessionMonitor(packageName: String) {
        sessionMonitorJob?.cancel()
        sessionMonitorJob = serviceScope.launch(Dispatchers.IO) {
            // STRICT EXCLUSION: Never block the launcher itself
            if (packageName == app.olauncher.BuildConfig.APPLICATION_ID) return@launch
            
            val category = categoryRepository.getCategory(packageName)?.type ?: app.olauncher.domain.model.CategoryType.DISTRACTING
            
            // 5-MINUTE HARD RULE: ONLY APPLIES TO DISTRACTING APPS
            val isDistracting = when (category) {
                app.olauncher.domain.model.CategoryType.DISTRACTING,
                app.olauncher.domain.model.CategoryType.SOCIAL,
                app.olauncher.domain.model.CategoryType.GAME,
                app.olauncher.domain.model.CategoryType.NEWS -> true
                else -> false
            }

            if (!isDistracting) {
                return@launch
            }

            // Session Logic with Persistence
            val prefs = Prefs(applicationContext)
            val now = System.currentTimeMillis()
            val lastActive = prefs.sessionLastActiveTime
            val savedPackage = prefs.sessionPackage
            
            // Resume if same package and < 1 min gap (brief switch)
            val isResume = (packageName == savedPackage) && (now - lastActive < 60_000L)
            
            if (isResume) {
                currentSessionStart = prefs.sessionStartTime
                sessionDurationLimit = if (prefs.isExtensionGranted) 7 * 60 * 1000L else 5 * 60 * 1000L
                isExtensionGranted = prefs.isExtensionGranted
            } else {
                // New Session
                currentSessionStart = now
                sessionDurationLimit = 5 * 60 * 1000L
                isExtensionGranted = false
                
                prefs.sessionPackage = packageName
                prefs.sessionStartTime = now
                prefs.isExtensionGranted = false
            }

            while (isActive) {
                val currentNow = System.currentTimeMillis()
                val elapsed = currentNow - currentSessionStart
                
                // Update heartbeat
                prefs.sessionLastActiveTime = currentNow
                
                if (elapsed > sessionDurationLimit) {
                    withContext(Dispatchers.Main) {
                        val strict = prefs.strictBlockingEnabled
                        showBlockingOverlay("ContinuousUsageLimit", !isExtensionGranted && !strict)
                    }
                    break 
                }
                kotlinx.coroutines.delay(2_000) // Check every 2s
            }
        }
    }

    private val switchHistory = java.util.ArrayDeque<Pair<String, Long>>()
    private val BORED_THRESHOLD_COUNT = 6
    private val BORED_WINDOW_MS = 60 * 1000L // 1 minute
    
    private fun checkForBoredom(packageName: String) {
        val now = System.currentTimeMillis()
        
        // Remove old entries
        while (switchHistory.isNotEmpty() && (now - switchHistory.peekFirst().second > BORED_WINDOW_MS)) {
            switchHistory.pollFirst()
        }

        // Add current (if different from last)
        if (switchHistory.isEmpty() || switchHistory.peekLast().first != packageName) {
            switchHistory.addLast(packageName to now)
        }

        if (switchHistory.size >= BORED_THRESHOLD_COUNT) {
            // Trigger Bored Mode
            serviceScope.launch(Dispatchers.IO) {
                container.systemLogRepository.logEvent(
                    app.olauncher.data.local.SystemLogEntity(
                        timestamp = System.currentTimeMillis(),
                        type = app.olauncher.data.local.LogType.VIOLATION,
                        message = "Bored Mode Triggered via Heuristic",
                        payload = "Rapid switching: ${switchHistory.map { it.first }}"
                    )
                )
            }
            modeManager.setMode(app.olauncher.domain.model.AppMode.BORED)
            switchHistory.clear() // Reset
        }
    }

    private suspend fun checkAndBlock(packageName: String) {
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
            val message = when (result.reason) {
                BlockingChecker.Reason.StrictBlock -> "This app is strictly blocked."
                BlockingChecker.Reason.Scheduled -> "Blocked by Schedule."
                BlockingChecker.Reason.TimeLimitExceeded -> "Daily limit reached."
                BlockingChecker.Reason.CategoryRestricted -> "Category Restricted."
                BlockingChecker.Reason.BoredomIntervention -> "Bored Mode Active.\nGo for a walk."
                BlockingChecker.Reason.ProductivityMode -> "Productivity Mode Active.\nFocus on essentials."
                BlockingChecker.Reason.DrivingSafety -> "Driving Mode Active.\nEyes on the road."
                BlockingChecker.Reason.EmergencyLockdown -> "Emergency Mode Limit."
                BlockingChecker.Reason.ContinuousUsageLimit -> "Session Limit."
            }
            showBlockingOverlay(message)
            
            // Log Violation
            serviceScope.launch(Dispatchers.IO) {
                container.systemLogRepository.logEvent(
                    app.olauncher.data.local.SystemLogEntity(
                        timestamp = System.currentTimeMillis(),
                        type = app.olauncher.data.local.LogType.APP_BLOCK_EVENT,
                        message = "Blocked $packageName",
                        payload = message
                    )
                )
            }
        }
    }

    private fun showBlockingOverlay(reason: String, canExtend: Boolean = false) {
        val intent = Intent(this, BlockingOverlayActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtra("BLOCKING_REASON", reason)
        intent.putExtra("CAN_EXTEND", canExtend)
        startActivity(intent)
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(lockReceiver)
            unregisterReceiver(extensionReceiver)
        } catch (e: Exception) {}
        serviceScope.cancel()
    }
}
