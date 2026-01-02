package app.olauncher.domain.repository

import android.content.pm.ResolveInfo

interface AppInfoRepository {
    suspend fun getAllInstalledApps(): List<AppInfo>
}

data class AppInfo(
    val packageName: String,
    val label: String
)
