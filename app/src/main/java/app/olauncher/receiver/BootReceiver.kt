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

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Schedule Usage Monitor
            val request = PeriodicWorkRequestBuilder<UsageMonitorWorker>(
                15, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "USAGE_MONITOR",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
