package de.brittytino.android.launcher.ui

import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import de.brittytino.android.launcher.Application
import de.brittytino.android.launcher.actions.Action
import de.brittytino.android.launcher.actions.Gesture
import de.brittytino.android.launcher.actions.LauncherAction
import de.brittytino.android.launcher.databinding.ActivityHomeBinding
import de.brittytino.android.launcher.openTutorial
import de.brittytino.android.launcher.preferences.LauncherPreferences
import de.brittytino.android.launcher.ui.tutorial.TutorialActivity
import de.brittytino.android.launcher.ui.util.LauncherGestureActivity

import de.brittytino.android.launcher.wallpaper.WallpaperManagerHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.brittytino.android.launcher.data.FocusModeRepository
import de.brittytino.android.launcher.apps.AbstractDetailedAppInfo
import de.brittytino.android.launcher.apps.AppInfo
import de.brittytino.android.launcher.apps.PinnedShortcutInfo
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

/**
 * [HomeActivity] is the actual application Launcher,
 * what makes this application special / unique.
 *
 * In this activity we display the date and time,
 * and we listen for actions like tapping, swiping or button presses.
 *
 * As it also is the first thing that is started when someone opens Launcher,
 * it also contains some logic related to the overall application:
 * - Setting global variables (preferences etc.)
 * - Opening the [TutorialActivity] on new installations
 */
class HomeActivity : UIObject, LauncherGestureActivity() {

    private lateinit var binding: ActivityHomeBinding

    private val appListObserver = androidx.lifecycle.Observer<List<AbstractDetailedAppInfo>> {
        updateHomeWithFocus()
    }

    private var sharedPreferencesListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, prefKey ->
            if (prefKey?.startsWith("clock.") == true ||
                prefKey?.startsWith("display.") == true
            ) {
                recreate()
            } else if (prefKey?.startsWith("action.") == true) {
                updateSettingsFallbackButtonVisibility()
            } else if (prefKey == LauncherPreferences.widgets().keys().widgets() || 
                       prefKey == "focus_state" || 
                       prefKey == "is_active") {
                updateHomeWithFocus()
            }
        }

    private fun updateHomeWithFocus() {
        val repo = FocusModeRepository(this)
        val state = repo.focusState
        
        if (!this::binding.isInitialized) return
        
        val isDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val bgColor = if (isDark) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        val textColor = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
        
        val window = this.window

        // Helper to set Immersive Window State
        fun setImmersiveFocus(enable: Boolean) {
            if (enable) {
                // FORCE WALLPAPER OFF: This is critical for solid background
                window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                
                // Force full screen coverage
                @Suppress("DEPRECATION")
                binding.root.fitsSystemWindows = false
                
                // Set opaque background on window to block any leaks
                window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(bgColor))
                
                window.statusBarColor = bgColor
                window.navigationBarColor = bgColor
                
                if (!isDark) {
                     @Suppress("DEPRECATION")
                     window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                } else {
                     @Suppress("DEPRECATION")
                     window.decorView.systemUiVisibility = 0 
                }
            } else {
                 // Restore Standard Launcher State (Wallpaper visible)
                 window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
                 
                 @Suppress("DEPRECATION")
                 binding.root.fitsSystemWindows = true
                 window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
                 
                 window.statusBarColor = android.graphics.Color.TRANSPARENT
                 window.navigationBarColor = android.graphics.Color.TRANSPARENT
                 
                 @Suppress("DEPRECATION")
                 window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
        }
        
        when (state) {
            FocusModeRepository.FocusState.ACTIVE -> {
                setImmersiveFocus(true)
                // strict focus mode: pure background, no widgets, dedicated clock
                binding.root.setBackgroundColor(bgColor)
                binding.homeWidgetContainer.visibility = View.GONE
                
                binding.focusClockLayout.visibility = View.VISIBLE
                binding.focusClockTime.setTextColor(textColor)
                binding.focusClockDate.setTextColor(textColor)
                
                binding.focusAppsList.visibility = View.VISIBLE
                binding.pausedOverlay.visibility = View.GONE
                
                val allApps = (application as Application).apps.value ?: emptyList()
                val filtered = allApps.filter { 
                   val raw = it.getRawInfo()
                   val pkg = when(raw) {
                       is AppInfo -> raw.packageName
                       is PinnedShortcutInfo -> raw.packageName
                       else -> null
                   }
                   pkg != null && repo.isAppAllowed(pkg)
                }.sortedBy { it.getLabel().lowercase() }
                
                // Refresh adapter
                binding.focusAppsList.layoutManager = LinearLayoutManager(this)
                binding.focusAppsList.adapter = FocusAppsAdapter(filtered)
            }
            FocusModeRepository.FocusState.PAUSED -> {
                setImmersiveFocus(true)
                // Paused: Keep Focus UI but dimmed
                binding.root.setBackgroundColor(bgColor)
                binding.homeWidgetContainer.visibility = View.GONE
                
                binding.focusClockLayout.visibility = View.VISIBLE
                binding.focusClockTime.setTextColor(textColor)
                binding.focusClockDate.setTextColor(textColor)
                
                binding.focusAppsList.visibility = View.VISIBLE // Keep list visible
                
                // Show dimmed overlay
                binding.pausedOverlay.visibility = View.VISIBLE
                
                // Note: To access other apps, user swipes up for Drawer.
                // AppFilter allows all apps in PAUSED state.
            }
            else -> { // INACTIVE or UNLOCK_PENDING
                 
                 if (state == FocusModeRepository.FocusState.UNLOCK_PENDING) {
                     // Keep Active look
                     setImmersiveFocus(true)
                     binding.root.setBackgroundColor(bgColor)
                     binding.homeWidgetContainer.visibility = View.GONE
                     binding.focusClockLayout.visibility = View.VISIBLE
                     binding.focusAppsList.visibility = View.VISIBLE
                     binding.pausedOverlay.visibility = View.GONE
                 } else {
                     // INACTIVE - Normal Launcher
                     setImmersiveFocus(false)
                     binding.root.background = null // Transparent/Wallpaper
                     binding.homeWidgetContainer.visibility = View.VISIBLE
                     binding.focusClockLayout.visibility = View.GONE
                     binding.focusAppsList.visibility = View.GONE
                     binding.pausedOverlay.visibility = View.GONE
                     binding.homeWidgetContainer.updateWidgets(this, LauncherPreferences.widgets().widgets())
                 }
            }
        }
    }

    inner class FocusAppsAdapter(private val apps: List<AbstractDetailedAppInfo>) : 
        RecyclerView.Adapter<FocusAppsAdapter.ViewHolder>() {
        
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(de.brittytino.android.launcher.R.id.list_apps_row_name)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(de.brittytino.android.launcher.R.layout.list_apps_row_variant_text, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = apps[position]
            holder.name.text = app.getLabel()
            holder.itemView.setOnClickListener {
                try {
                   val raw = app.getRawInfo()
                   val pkg = when(raw) {
                       is AppInfo -> raw.packageName
                       is PinnedShortcutInfo -> raw.packageName
                       else -> null
                   }
                   if(pkg != null) {
                        val intent = packageManager.getLaunchIntentForPackage(pkg)
                        if (intent != null) startActivity(intent)
                   }
                } catch(e: Exception) { e.printStackTrace() }
            }
        }

        override fun getItemCount() = apps.size
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super<LauncherGestureActivity>.onCreate(savedInstanceState)
        super<UIObject>.onCreate()

        // Initialise layout
        binding = ActivityHomeBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.buttonFallbackSettings.setOnClickListener {
            LauncherAction.SETTINGS.invoke(this)
        }
        
        (application as Application).apps.observeForever(appListObserver)
    }

    override fun onStart() {
        super<LauncherGestureActivity>.onStart()
        super<UIObject>.onStart()
        
        // Automatic Wallpaper Rotation
        Thread {
            WallpaperManagerHelper.checkAndRotateWallpaper(this)
        }.start()

        // If the tutorial was not finished, start it
        if (!LauncherPreferences.internal().started()) {
            openTutorial(this)
        }

        LauncherPreferences.getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus && LauncherPreferences.display().hideNavigationBar()) {
            hideNavigationBar()
        }
    }

    private fun updateSettingsFallbackButtonVisibility() {
        // If T Launcher settings can not be reached from any action bound to an enabled gesture,
        // show the fallback button.
        binding.buttonFallbackSettings.visibility = if (
            !Gesture.entries.any { g ->
                g.isEnabled() && Action.forGesture(g)?.canReachSettings() == true
            }
        ) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun getTheme(): Resources.Theme {
        return modifyTheme(super.getTheme())
    }

    override fun onPause() {
        try {
            (application as Application).appWidgetHost.stopListening()
        } catch (e: Exception) {
            // Throws a NullPointerException on Android 12 an earlier, see #172
            e.printStackTrace()
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        updateSettingsFallbackButtonVisibility()

        updateHomeWithFocus()

        (application as Application).appWidgetHost.startListening()
    }


    override fun onDestroy() {
        (application as Application).apps.removeObserver(appListObserver)
        LauncherPreferences.getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)
        super.onDestroy()
    }

    override fun handleBack() {
        Gesture.BACK(this)
    }

    override fun getRootView(): View {
        return binding.root
    }

    override fun isHomeScreen(): Boolean {
        return true
    }
}
