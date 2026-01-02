package app.olauncher.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import app.olauncher.TLauncherApplication
import app.olauncher.domain.model.CategoryType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationBlockerService : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        if (packageName == applicationContext.packageName) return // Don't block ourselves

        scope.launch {
            val container = (applicationContext as TLauncherApplication).container
            val category = container.categoryRepository.getCategory(packageName)
            
            // Block notifications from Procrastinating apps unless they are whitelisted
            if (category != null && category.type == CategoryType.PROCRASTINATING && !category.isWhitelisted) {
                cancelNotification(sbn.key)
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        // Optional: Rescan existing notifications
    }
}
