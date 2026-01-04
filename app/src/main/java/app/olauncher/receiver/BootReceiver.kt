package app.olauncher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.olauncher.TLauncherApplication
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import app.olauncher.services.UsageMonitorWorker
import java.util.concurrent.TimeUnit

import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            
            // Schedule Usage Monitor
            val request = PeriodicWorkRequestBuilder<UsageMonitorWorker>(
                15, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "USAGE_MONITOR",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )

            // Reschedule Daily Alarm
            app.olauncher.helper.AlarmScheduler.scheduleNextAlarm(context)

            // Log Reboot
            val repo = (context.applicationContext as app.olauncher.TLauncherApplication).container.systemLogRepository
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    repo.logEvent(
                        app.olauncher.data.local.SystemLogEntity(
                            timestamp = System.currentTimeMillis(),
                            type = app.olauncher.data.local.LogType.SYSTEM_EVENT,
                            message = "Device Booted"
                        )
                    )
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
