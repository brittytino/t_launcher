package app.olauncher.helper

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.olauncher.data.Constants
import app.olauncher.data.Prefs
import kotlinx.coroutines.coroutineScope

class WallpaperWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    private val prefs = Prefs(applicationContext)

    override suspend fun doWork(): Result = coroutineScope {
        if (!prefs.dailyWallpaper || !isOlauncherDefault(applicationContext)) {
             return@coroutineScope Result.success()
        }

        val todayColor = getTodaysColor()
        // If we want checking mechanism to avoid re-applying same color, we can add a check here.
        // However, setting the same bitmap is relatively cheap once a day.
        
        val success = try {
            setPlainWallpaper(applicationContext, todayColor)
            true
        } catch (e: Exception) {
            false
        }

        if (success) Result.success() else Result.retry()
    }
}
