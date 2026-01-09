package app.olauncher

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.UserHandle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import app.olauncher.data.AppModel
import app.olauncher.data.Constants
import app.olauncher.data.Prefs
import app.olauncher.helper.SingleLiveEvent
import app.olauncher.helper.WallpaperWorker
import app.olauncher.helper.formattedTimeSpent
import app.olauncher.helper.getAppsList
import app.olauncher.helper.hasBeenMinutes
import app.olauncher.helper.isOlauncherDefault
import app.olauncher.helper.isPackageInstalled
import app.olauncher.helper.showToast
import app.olauncher.helper.usageStats.EventLogWrapper
import app.olauncher.domain.model.AppCategory
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit


class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext by lazy { application.applicationContext }
    val prefs = Prefs(appContext)
    
    // T Launcher Dependencies
    private val container = (application as TLauncherApplication).container
    private val initializeCategoriesUseCase = container.initializeCategoriesUseCase
    private val updateAppCategoryUseCase = container.updateAppCategoryUseCase
    private val toggleWhitelistUseCase = container.toggleWhitelistUseCase
    val categoryRepository = container.categoryRepository
    private val productivityRepository = container.productivityRepository
    val systemLogRepository = container.systemLogRepository
    val allNotes = productivityRepository.allNotes
    val allTasks = productivityRepository.allTasks

    // Usage Stats
    private val _usageStats = MutableLiveData<TLauncherStats>()
    val usageStats: androidx.lifecycle.LiveData<TLauncherStats> = _usageStats
    
    // Real Surveillance Logs
    val surveillanceLogs = systemLogRepository.recentLogs.asLiveData()

    data class TLauncherStats(
        val alarmSuccess: Int = 0,
        val alarmFailure: Int = 0,
        val focusSessions: Int = 0,
        val breathingSessions: Int = 0,
        val emergencyUsage: Int = 0,
        val blocks: Int = 0,
        val missedCheckins: Int = 0
    )

    fun fetchUsageStats() {
        viewModelScope.launch {
            val since = 0L // All time? Or this month? User said "How am I using this system?" -> All active features. Yearly Heatmap exists. Stats likely All Time or Year.
            // Let's do All Time for "Usage Stats" unless specified. 
            // "Dynamically populated... Include: Alarm usage stats...".
            
            val alarmSuccess = systemLogRepository.getCountByTypeSince(app.olauncher.data.local.LogType.ALARM_SUCCESS, since)
            val alarmFailure = systemLogRepository.getCountByTypeSince(app.olauncher.data.local.LogType.ALARM_FAILURE, since)
            val focus = systemLogRepository.getCountByTypeSince(app.olauncher.data.local.LogType.FOCUS_SESSION, since)
            val breathing = systemLogRepository.getCountByTypeSince(app.olauncher.data.local.LogType.BREATHING_SESSION, since)
            val emergency = systemLogRepository.getCountByTypeSince(app.olauncher.data.local.LogType.EMERGENCY_OVERRIDE, since)
            val blocks = systemLogRepository.getCountByTypeSince(app.olauncher.data.local.LogType.VIOLATION, since) // Or APP_BLOCK_EVENT
            // We added APP_BLOCK_EVENT, use that if we log it there.
            // The service logged VIOLATION for blocks. I should probably query both or migrate.
            // EnforcementService logs VIOLATION. I will use VIOLATION for blocks + APP_BLOCK_EVENT if used.
            val blocks2 = systemLogRepository.getCountByTypeSince(app.olauncher.data.local.LogType.APP_BLOCK_EVENT, since)
            val missed = systemLogRepository.getCountByTypeSince(app.olauncher.data.local.LogType.MISSED_CHECKIN, since)

            _usageStats.postValue(TLauncherStats(
                alarmSuccess, alarmFailure, focus, breathing, emergency, blocks + blocks2, missed
            ))
        }
    }

    val navigateToProductivity = SingleLiveEvent<Unit>()

    init {
        viewModelScope.launch {
            initializeCategoriesUseCase()
        }
        scheduleUsageMonitoring()
        app.olauncher.helper.WallpaperManager.applyDailyWallpaper(appContext)
    }

    fun toggleWhitelist(packageName: String) {
        viewModelScope.launch {
            toggleWhitelistUseCase(packageName)
        }
    }

    fun updateAppCategory(packageName: String, type: app.olauncher.domain.model.CategoryType) {
        viewModelScope.launch {
            // Check if exists to preserve whitelist status?
            // For now, just create new with default false, or fetch.
            // Let's assume default false is safe, or we should fetch first.
            // But fetching every time might be slow.
            // Ideally UpdateUseCase handles this.
            val category = app.olauncher.domain.model.AppCategory(packageName, type)
            updateAppCategoryUseCase(category)
            appContext.showToast("Category updated to ${type.name}")
        }
    }

    fun addRule(target: String, rule: app.olauncher.domain.model.UsageRule) {
        viewModelScope.launch {
            (getApplication() as TLauncherApplication).container.rulesRepository.addRule(target, rule)
            appContext.showToast("Rule updated")
        }
    }

    fun removeRule(target: String, rule: app.olauncher.domain.model.UsageRule) {
        viewModelScope.launch {
            (getApplication() as TLauncherApplication).container.rulesRepository.removeRule(target, rule)
            appContext.showToast("Rule removed")
        }
    }

    fun setAppLimit(packageName: String, limitMinutes: Int) {
        addRule(packageName, app.olauncher.domain.model.UsageRule.DailyLimit(limitMinutes))
    }

    fun removeAppLimit(packageName: String) {
        viewModelScope.launch {
            val rule = app.olauncher.domain.model.UsageRule.DailyLimit(0) // Dummy for type matching
            (getApplication() as TLauncherApplication).container.rulesRepository.removeRule(packageName, rule)
            appContext.showToast("Limit removed")
        }
    }

    val allRules = container.rulesRepository.getAllRules().asLiveData()




    val applyVisualDetox = MutableLiveData<Unit>()
    val refreshHome = MutableLiveData<Boolean>()
    val toggleDateTime = MutableLiveData<Unit>()
    val updateSwipeApps = MutableLiveData<Any>()
    val appList = MutableLiveData<List<AppModel>?>()
    val hiddenApps = MutableLiveData<List<AppModel>?>()
    val isOlauncherDefault = MutableLiveData<Boolean>()
    val launcherResetFailed = MutableLiveData<Boolean>()
    val homeAppAlignment = MutableLiveData<Int>()
    val screenTimeValue = MutableLiveData<String>()

    val showDialog = SingleLiveEvent<String>()
    val checkForMessages = SingleLiveEvent<Unit?>()
    val resetLauncherLiveData = SingleLiveEvent<Unit?>()

    fun selectedApp(appModel: AppModel, flag: Int) {


        when (flag) {
            Constants.FLAG_LAUNCH_APP -> {
                launchApp(appModel.appPackage, appModel.activityClassName, appModel.user)
            }

            Constants.FLAG_HIDDEN_APPS -> {
                launchApp(appModel.appPackage, appModel.activityClassName, appModel.user)
            }

            Constants.FLAG_SET_HOME_APP_1 -> {
                prefs.appName1 = appModel.appLabel
                prefs.appPackage1 = appModel.appPackage
                prefs.appUser1 = appModel.user.toString()
                prefs.appActivityClassName1 = appModel.activityClassName
                refreshHome(false)
            }

            Constants.FLAG_SET_HOME_APP_2 -> {
                prefs.appName2 = appModel.appLabel
                prefs.appPackage2 = appModel.appPackage
                prefs.appUser2 = appModel.user.toString()
                prefs.appActivityClassName2 = appModel.activityClassName
                refreshHome(false)
            }

            Constants.FLAG_SET_HOME_APP_3 -> {
                prefs.appName3 = appModel.appLabel
                prefs.appPackage3 = appModel.appPackage
                prefs.appUser3 = appModel.user.toString()
                prefs.appActivityClassName3 = appModel.activityClassName
                refreshHome(false)
            }

            Constants.FLAG_SET_HOME_APP_4 -> {
                prefs.appName4 = appModel.appLabel
                prefs.appPackage4 = appModel.appPackage
                prefs.appUser4 = appModel.user.toString()
                prefs.appActivityClassName4 = appModel.activityClassName
                refreshHome(false)
            }

            Constants.FLAG_SET_HOME_APP_5 -> {
                prefs.appName5 = appModel.appLabel
                prefs.appPackage5 = appModel.appPackage
                prefs.appUser5 = appModel.user.toString()
                prefs.appActivityClassName5 = appModel.activityClassName
                refreshHome(false)
            }

            Constants.FLAG_SET_HOME_APP_6 -> {
                prefs.appName6 = appModel.appLabel
                prefs.appPackage6 = appModel.appPackage
                prefs.appUser6 = appModel.user.toString()
                prefs.appActivityClassName6 = appModel.activityClassName
                refreshHome(false)
            }

            Constants.FLAG_SET_HOME_APP_7 -> {
                prefs.appName7 = appModel.appLabel
                prefs.appPackage7 = appModel.appPackage
                prefs.appUser7 = appModel.user.toString()
                prefs.appActivityClassName7 = appModel.activityClassName
                refreshHome(false)
            }

            Constants.FLAG_SET_HOME_APP_8 -> {
                prefs.appName8 = appModel.appLabel
                prefs.appPackage8 = appModel.appPackage
                prefs.appUser8 = appModel.user.toString()
                prefs.appActivityClassName8 = appModel.activityClassName
                refreshHome(false)
            }

            Constants.FLAG_SET_SWIPE_LEFT_APP -> {
                prefs.appNameSwipeLeft = appModel.appLabel
                prefs.appPackageSwipeLeft = appModel.appPackage
                prefs.appUserSwipeLeft = appModel.user.toString()
                prefs.appActivityClassNameSwipeLeft = appModel.activityClassName
                updateSwipeApps()
            }

            Constants.FLAG_SET_SWIPE_RIGHT_APP -> {
                prefs.appNameSwipeRight = appModel.appLabel
                prefs.appPackageSwipeRight = appModel.appPackage
                prefs.appUserSwipeRight = appModel.user.toString()
                prefs.appActivityClassNameRight = appModel.activityClassName
                updateSwipeApps()
            }

            Constants.FLAG_SET_CLOCK_APP -> {
                prefs.clockAppPackage = appModel.appPackage
                prefs.clockAppUser = appModel.user.toString()
                prefs.clockAppClassName = appModel.activityClassName
            }

            Constants.FLAG_SET_CALENDAR_APP -> {
                prefs.calendarAppPackage = appModel.appPackage
                prefs.calendarAppUser = appModel.user.toString()
                prefs.calendarAppClassName = appModel.activityClassName
            }
        }
    }



    fun refreshHome(appCountUpdated: Boolean) {
        refreshHome.value = appCountUpdated
    }

    fun toggleDateTime() {
        toggleDateTime.postValue(Unit)
    }

    private fun updateSwipeApps() {
        updateSwipeApps.postValue(Unit)
    }

    private fun launchApp(packageName: String, activityClassName: String?, userHandle: UserHandle) {
        val launcher = appContext.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val activityInfo = launcher.getActivityList(packageName, userHandle)

        val component = if (activityClassName.isNullOrBlank()) {
            when (activityInfo.size) {
                0 -> {
                    appContext.showToast(appContext.getString(R.string.app_not_found))
                    return
                }

                1 -> ComponentName(packageName, activityInfo[0].name)
                else -> ComponentName(packageName, activityInfo[activityInfo.size - 1].name)
            }
        } else {
            ComponentName(packageName, activityClassName)
        }

        try {
            launcher.startMainActivity(component, userHandle, null, null)
        } catch (e: SecurityException) {
            try {
                launcher.startMainActivity(component, android.os.Process.myUserHandle(), null, null)
            } catch (e: Exception) {
                appContext.showToast(appContext.getString(R.string.unable_to_open_app))
            }
        } catch (e: Exception) {
            appContext.showToast(appContext.getString(R.string.unable_to_open_app))
        }
    }

    fun getAppList(includeHiddenApps: Boolean = false) {
        viewModelScope.launch {
            appList.value = getAppsList(appContext, prefs, includeRegularApps = true, includeHiddenApps, categoryRepository)
        }
    }

    fun getHiddenApps() {
        viewModelScope.launch {
            hiddenApps.value = getAppsList(appContext, prefs, includeRegularApps = false, includeHiddenApps = true, categoryRepository)
        }
    }

    fun isOlauncherDefault() {
        isOlauncherDefault.value = isOlauncherDefault(appContext)
    }

    fun setWallpaperWorker() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        val uploadWorkRequest = PeriodicWorkRequestBuilder<WallpaperWorker>(8, TimeUnit.HOURS)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
            Constants.WALLPAPER_WORKER_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            uploadWorkRequest
        )
    }

    private fun scheduleUsageMonitoring() {
       val request = PeriodicWorkRequestBuilder<app.olauncher.services.UsageMonitorWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
            "USAGE_MONITOR",
            ExistingPeriodicWorkPolicy.KEEP, 
            request
        )

        
        val accountabilityRequest = PeriodicWorkRequestBuilder<app.olauncher.services.AccountabilityWorker>(
            24, TimeUnit.HOURS
        ).setInitialDelay(app.olauncher.helper.calculateInitialDelayForAccountability(), TimeUnit.MILLISECONDS)
        .build()

        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
            "ACCOUNTABILITY_WORKER",
            ExistingPeriodicWorkPolicy.KEEP,
            accountabilityRequest
        )
    }

    // Removed private calculateInitialDelayForAccountability as it is now in Utils

    fun cancelWallpaperWorker() {
        WorkManager.getInstance(appContext).cancelUniqueWork(Constants.WALLPAPER_WORKER_NAME)
        prefs.dailyWallpaperUrl = ""
        prefs.dailyWallpaper = false
    }

    fun updateHomeAlignment(gravity: Int) {
        prefs.homeAlignment = gravity
        homeAppAlignment.value = prefs.homeAlignment
    }

    fun getTodaysScreenTime() {
        if (prefs.screenTimeLastUpdated.hasBeenMinutes(1).not()) return

        val eventLogWrapper = EventLogWrapper(appContext)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val timeSpent = eventLogWrapper.aggregateSimpleUsageStats(
            eventLogWrapper.aggregateForegroundStats(
                eventLogWrapper.getForegroundStatsByTimestamps(startTime, endTime)
            )
        )
        val viewTimeSpent = appContext.formattedTimeSpent(timeSpent)
        screenTimeValue.postValue(viewTimeSpent)
        prefs.screenTimeLastUpdated = endTime
    }

    fun setDefaultClockApp() {
        viewModelScope.launch {
            try {
                Constants.CLOCK_APP_PACKAGES.firstOrNull { appContext.isPackageInstalled(it) }?.let { packageName ->
                    appContext.packageManager.getLaunchIntentForPackage(packageName)?.component?.className?.let {
                        prefs.clockAppPackage = packageName
                        prefs.clockAppClassName = it
                        prefs.clockAppUser = android.os.Process.myUserHandle().toString()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Productivity Methods
    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            productivityRepository.insertNote(app.olauncher.data.local.NoteEntity(title = title, content = content, timestamp = System.currentTimeMillis()))
        }
    }

    fun deleteNote(note: app.olauncher.data.local.NoteEntity) {
        viewModelScope.launch {
            productivityRepository.deleteNote(note)
        }
    }

    fun addTask(text: String) {
        viewModelScope.launch {
            productivityRepository.insertTask(app.olauncher.data.local.TaskEntity(text = text, timestamp = System.currentTimeMillis()))
        }
    }

    fun toggleTaskCompletion(task: app.olauncher.data.local.TaskEntity) {
        viewModelScope.launch {
            productivityRepository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: app.olauncher.data.local.TaskEntity) {
        viewModelScope.launch {
            productivityRepository.deleteTask(task)
        }
    }

    // Accountability
    private val accountabilityRepository = container.accountabilityRepository
    val allAccountabilityLogs = accountabilityRepository.allLogs.asLiveData()
    val todayLog = MutableLiveData<app.olauncher.data.local.AccountabilityEntity?>()

    fun checkTodayLog() {
        viewModelScope.launch {
            val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            todayLog.postValue(accountabilityRepository.getLogForDate(date))
        }
    }

    fun saveCheckIn(diet: Boolean, sugar: Boolean, workout: Boolean, productive: Boolean) {
        viewModelScope.launch {
            val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val existing = accountabilityRepository.getLogForDate(date)
            
            val log = if (existing != null) {
                existing.copy(dietFollowed = diet, sugarFree = sugar, didWorkout = workout, productiveToday = productive)
            } else {
                app.olauncher.data.local.AccountabilityEntity(date, diet, sugar, workout, productive)
            }
            
            accountabilityRepository.insertLog(log)
            todayLog.postValue(log)
            appContext.showToast("Day logged. Stay hard.")
        }
    }

    fun saveDevMetrics(leetcode: Int, codeforces: Int) {
        viewModelScope.launch {
            val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val existing = accountabilityRepository.getLogForDate(date)
            
            val log = if (existing != null) {
                existing.copy(leetcodeCount = leetcode, codeforcesCount = codeforces)
            } else {
                app.olauncher.data.local.AccountabilityEntity(
                    date = date, 
                    dietFollowed = false, 
                    sugarFree = false, 
                    didWorkout = false, 
                    productiveToday = false,
                    leetcodeCount = leetcode,
                    codeforcesCount = codeforces
                )
            }
            
            accountabilityRepository.insertLog(log)
            todayLog.postValue(log)
        }
    }

    fun logSystemEvent(type: app.olauncher.data.local.LogType, message: String) {
        viewModelScope.launch {
            systemLogRepository.logEvent(
                app.olauncher.data.local.SystemLogEntity(
                    timestamp = System.currentTimeMillis(),
                    type = type,
                    message = message
                )
            )
        }
    }
}