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
            val isWhitelisted = category?.isWhitelisted == true

            // Check system defaults
            val defaultDialer = getSystemService(android.telecom.TelecomManager::class.java)?.defaultDialerPackage
            val defaultSms = android.provider.Telephony.Sms.getDefaultSmsPackage(applicationContext)

            val isEssential = packageName == defaultDialer || packageName == defaultSms

            // Strict Filter: Allow ONLY Whitelisted apps and Essential Communication
            if (!isWhitelisted && !isEssential) {
                cancelNotification(sbn.key)
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        // Optional: Rescan existing notifications
    }
}
