package app.olauncher.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import app.olauncher.domain.repository.AppInfo
import app.olauncher.domain.repository.AppInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppInfoRepositoryImpl(
    private val context: Context
) : AppInfoRepository {

    override suspend fun getAllInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        val resolvedInfos = pm.queryIntentActivities(mainIntent, 0)
        
        resolvedInfos.map { resolveInfo ->
            AppInfo(
                packageName = resolveInfo.activityInfo.packageName,
                label = resolveInfo.loadLabel(pm).toString()
            )
        }
    }
}
