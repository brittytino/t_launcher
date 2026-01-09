package app.olauncher.helper

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.SearchManager
import android.app.WallpaperManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate
import app.olauncher.BuildConfig
import app.olauncher.R
import app.olauncher.data.AppModel
import app.olauncher.data.Constants
import app.olauncher.data.Prefs
import app.olauncher.services.EnforcementService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.Collator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Scanner
import kotlin.math.pow
import kotlin.math.sqrt

fun calculateInitialDelayForAccountability(): Long {
    val calendar = Calendar.getInstance()
    val now = System.currentTimeMillis()
    
    calendar.set(Calendar.HOUR_OF_DAY, 21) // 9 PM
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    
    var delay = calendar.timeInMillis - now
    if (delay <= 0) {
        delay += java.util.concurrent.TimeUnit.DAYS.toMillis(1)
    }
    return delay
}

fun Context.showToast(message: String?, duration: Int = Toast.LENGTH_SHORT) {
    if (message.isNullOrBlank()) return
    Toast.makeText(this, message, duration).show()
}

fun Context.showToast(stringResource: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, getString(stringResource), duration).show()
}

suspend fun getAppsList(
    context: Context,
    prefs: Prefs,
    includeRegularApps: Boolean = true,
    includeHiddenApps: Boolean = false,
    categoryRepository: app.olauncher.domain.repository.CategoryRepository? = null
): MutableList<AppModel> {
    return withContext(Dispatchers.IO) {
        val appList: MutableList<AppModel> = mutableListOf()

        try {
            if (!Prefs(context).hiddenAppsUpdated) upgradeHiddenApps(Prefs(context))
            val hiddenApps = Prefs(context).hiddenApps
            
            // Optimization: Fetch all categories once
            val categoriesMap = categoryRepository?.getAllCategoriesSync()?.associateBy { it.packageName } ?: emptyMap()

            val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
            val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            val collator = Collator.getInstance()

            for (profile in userManager.userProfiles) {
                for (app in launcherApps.getActivityList(null, profile)) {

                    val appLabelShown = prefs.getAppRenameLabel(app.applicationInfo.packageName).ifBlank { app.label.toString() }
                    val packageName = app.applicationInfo.packageName
                    val isWhitelisted = categoriesMap[packageName]?.isWhitelisted == true
                    
                    val appModel = AppModel(
                        appLabelShown,
                        collator.getCollationKey(app.label.toString()),
                        packageName,
                        app.componentName.className,
                        (System.currentTimeMillis() - app.firstInstallTime) < Constants.ONE_HOUR_IN_MILLIS,
                        isWhitelisted,
                        profile
                    )

                    // if the current app is not T Launcher
                    if (packageName != BuildConfig.APPLICATION_ID) {
                        // is this a hidden app?
                        if (hiddenApps.contains(packageName + "|" + profile.toString())) {
                            if (includeHiddenApps) {
                                appList.add(appModel)
                            }
                        } else {
                            // this is a regular app
                            if (includeRegularApps) {
                                appList.add(appModel)
                            }
                        }
                    }
                }
            }
            appList.sortBy { it.appLabel.lowercase() }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        appList
    }
}

// This is to ensure backward compatibility with older app versions
// which did not support multiple user profiles
private fun upgradeHiddenApps(prefs: Prefs) {
    val hiddenAppsSet = prefs.hiddenApps
    val newHiddenAppsSet = mutableSetOf<String>()
    for (hiddenPackage in hiddenAppsSet) {
        if (hiddenPackage.contains("|")) newHiddenAppsSet.add(hiddenPackage)
        else newHiddenAppsSet.add(hiddenPackage + android.os.Process.myUserHandle().toString())
    }
    prefs.hiddenApps = newHiddenAppsSet
    prefs.hiddenAppsUpdated = true
}

fun isPackageInstalled(context: Context, packageName: String, userString: String): Boolean {
    val launcher = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    val activityInfo = launcher.getActivityList(packageName, getUserHandleFromString(context, userString))
    if (activityInfo.size > 0) return true
    return false
}

fun getUserHandleFromString(context: Context, userHandleString: String): UserHandle {
    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    for (userHandle in userManager.userProfiles) {
        if (userHandle.toString() == userHandleString) {
            return userHandle
        }
    }
    return android.os.Process.myUserHandle()
}

fun isOlauncherDefault(context: Context): Boolean {
    val launcherPackageName = getDefaultLauncherPackage(context)
    return BuildConfig.APPLICATION_ID == launcherPackageName
}

fun getDefaultLauncherPackage(context: Context): String {
    val intent = Intent()
    intent.action = Intent.ACTION_MAIN
    intent.addCategory(Intent.CATEGORY_HOME)
    val packageManager = context.packageManager
    val result = packageManager.resolveActivity(intent, 0)
    return if (result?.activityInfo != null) {
        result.activityInfo.packageName
    } else "android"
}

fun setPlainWallpaperByTheme(context: Context, appTheme: Int) {
    val colorRes = when (appTheme) {
        AppCompatDelegate.MODE_NIGHT_YES -> android.R.color.black
        AppCompatDelegate.MODE_NIGHT_NO -> android.R.color.white
        else -> {
            if (context.isDarkThemeOn()) android.R.color.black
            else android.R.color.white
        }
    }
    setPlainWallpaper(context, context.getColor(colorRes))
}

fun setPlainWallpaper(context: Context, color: Int) {
    try {
        val (width, height) = getScreenDimensions(context)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(color)

        val manager = WallpaperManager.getInstance(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            manager.setBitmap(bitmap, null, false, WallpaperManager.FLAG_SYSTEM)
            manager.setBitmap(bitmap, null, false, WallpaperManager.FLAG_LOCK)
        } else
            manager.setBitmap(bitmap)
        
        bitmap.recycle()
    } catch (e: Exception) {
         e.printStackTrace()
    }
}

fun getChangedAppTheme(context: Context, currentAppTheme: Int): Int {
    return when (currentAppTheme) {
        AppCompatDelegate.MODE_NIGHT_YES -> AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.MODE_NIGHT_NO -> AppCompatDelegate.MODE_NIGHT_YES
        else -> {
            if (context.isDarkThemeOn())
                AppCompatDelegate.MODE_NIGHT_NO
            else AppCompatDelegate.MODE_NIGHT_YES
        }
    }
}

fun openAppInfo(context: Context, userHandle: UserHandle, packageName: String) {
    val launcher = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    val intent: Intent? = context.packageManager.getLaunchIntentForPackage(packageName)

    intent?.let {
        launcher.startAppDetailsActivity(intent.component, userHandle, null, null)
    } ?: context.showToast(context.getString(R.string.unable_to_open_app))
}



suspend fun getWallpaperBitmap(originalImage: Bitmap, width: Int, height: Int): Bitmap {
    return withContext(Dispatchers.IO) {

        val background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val originalWidth: Float = originalImage.width.toFloat()
        val originalHeight: Float = originalImage.height.toFloat()

        val canvas = Canvas(background)
        val heightScale: Float = height / originalHeight
        val widthScale: Float = width / originalWidth
        val scale = maxOf(heightScale, widthScale)

        val (xTranslation, yTranslation) = if (heightScale > widthScale)
            Pair((width - originalWidth * heightScale) / 2.0f, 0f)
        else
            Pair(0f, (height - originalHeight * widthScale) / 2.0f)

        val transformation = Matrix()
        transformation.postTranslate(xTranslation, yTranslation)
        transformation.preScale(scale, scale)

        val paint = Paint()
        paint.isFilterBitmap = true
        canvas.drawBitmap(originalImage, transformation, paint)

        background
    }
}


val DAILY_WALLPAPER_PALETTE = listOf(
    0xFF264653.toInt(), // Charcoal
    0xFF2A9D8F.toInt(), // Teal
    0xFFE9C46A.toInt(), // Sandy
    0xFFF4A261.toInt(), // Orange Muted
    0xFFE76F51.toInt(), // Burnt Sienna
    0xFF606c38.toInt(), // Olive
    0xFF283618.toInt(), // Forest
    0xFFdda15e.toInt(), // Earth
    0xFFa98467.toInt(), // Coffee
    0xFF457b9d.toInt(), // Blue Muted
    0xFF1d3557.toInt(), // Navy
    0xFF3d405b.toInt(), // Dark Slate
    0xFF81b29a.toInt(), // Sage
    0xFFf2cc8f.toInt(), // Muted Yellow
    0xFF9A8C98.toInt(), // Muted Purple
    0xFF4A4E69.toInt()  // Deep Purple
)

suspend fun setWallpaper(appContext: Context, color: Int): Boolean {
    return withContext(Dispatchers.IO) {
        if (appContext.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE && isTablet(appContext).not())
            return@withContext false

        val wallpaperManager = WallpaperManager.getInstance(appContext)
        val (width, height) = getScreenDimensions(appContext)
        
        // Create Solid Base Bitmap
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(color)

        try {
            wallpaperManager.setBitmap(bitmap, null, false, WallpaperManager.FLAG_SYSTEM)
            wallpaperManager.setBitmap(bitmap, null, false, WallpaperManager.FLAG_LOCK)
        } catch (e: Exception) {
            return@withContext false
        }

        try {
            bitmap.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        true
    }
}

fun getScreenDimensions(context: Context): Pair<Int, Int> {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val point = Point()
    windowManager.defaultDisplay.getRealSize(point)
    return Pair(point.x, point.y)
}

fun getTodaysColor(): Int {
    val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    val index = dayOfYear % DAILY_WALLPAPER_PALETTE.size
    return DAILY_WALLPAPER_PALETTE[index]
}




fun openSearch(context: Context) {
    val intent = Intent(Intent.ACTION_WEB_SEARCH)
    intent.putExtra(SearchManager.QUERY, "")
    context.startActivity(intent)
}

@SuppressLint("WrongConstant", "PrivateApi")
fun expandNotificationDrawer(context: Context) {
    // Source: https://stackoverflow.com/a/51132142
    try {
        val statusBarService = context.getSystemService("statusbar")
        val statusBarManager = Class.forName("android.app.StatusBarManager")
        val method = statusBarManager.getMethod("expandNotificationsPanel")
        method.invoke(statusBarService)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun openDialerApp(context: Context) {
    try {
        val sendIntent = Intent(Intent.ACTION_DIAL)
        context.startActivity(sendIntent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun openCameraApp(context: Context) {
    try {
        val sendIntent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
        context.startActivity(sendIntent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// Alarm app removed per requirements

fun openCalendar(context: Context) {
    try {
        val calendarUri = CalendarContract.CONTENT_URI
            .buildUpon()
            .appendPath("time")
            .build()
        context.startActivity(Intent(Intent.ACTION_VIEW, calendarUri))
    } catch (e: Exception) {
        try {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_APP_CALENDAR)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun isAccessServiceEnabled(context: Context): Boolean {
    val expectedComponentName = ComponentName(context, app.olauncher.services.EnforcementService::class.java)
    val enabledServicesSetting = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    val stringColonSplitter = android.text.TextUtils.SimpleStringSplitter(':')
    stringColonSplitter.setString(enabledServicesSetting)

    while (stringColonSplitter.hasNext()) {
        val componentNameString = stringColonSplitter.next()
        val enabledComponent = ComponentName.unflattenFromString(componentNameString)
        if (enabledComponent != null && enabledComponent == expectedComponentName)
            return true
    }
    return false
}

fun isTablet(context: Context): Boolean {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val metrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(metrics)
    val widthInches = metrics.widthPixels / metrics.xdpi
    val heightInches = metrics.heightPixels / metrics.ydpi
    val diagonalInches = sqrt(widthInches.toDouble().pow(2.0) + heightInches.toDouble().pow(2.0))
    if (diagonalInches >= 7.0) return true
    return false
}

fun Context.isDarkThemeOn(): Boolean {
    return resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
}

fun Context.copyToClipboard(text: String) {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText(getString(R.string.app_name), text)
    clipboardManager.setPrimaryClip(clipData)
    showToast("")
}

fun Context.openUrl(url: String) {
    if (url.isEmpty()) return
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    startActivity(intent)
}

fun Context.isSystemApp(packageName: String): Boolean {
    if (packageName.isBlank()) return true
    return try {
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        ((applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0)
                || (applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0))
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun Context.uninstall(packageName: String) {
    val intent = Intent(Intent.ACTION_DELETE)
    intent.data = Uri.parse("package:$packageName")
    startActivity(intent)
}

@ColorInt
fun Context.getColorFromAttr(
    @AttrRes attrColor: Int,
    typedValue: TypedValue = TypedValue(),
    resolveRefs: Boolean = true,
): Int {
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}

fun View.animateAlpha(alpha: Float = 1.0f) {
    this.animate().apply {
        interpolator = LinearInterpolator()
        duration = 200
        alpha(alpha)
        start()
    }
}

fun Context.shareApp() {
    val message = getString(R.string.are_you_using_your_phone_or_is_your_phone_using_you)
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, message)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}

fun Context.rateApp() {
    android.widget.Toast.makeText(this, "Thank you for using T Launcher!", android.widget.Toast.LENGTH_SHORT).show()
}

fun appUsagePermissionGranted(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
    } else {
        @Suppress("DEPRECATION")
        appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
    }
    return mode == AppOpsManager.MODE_ALLOWED
}
