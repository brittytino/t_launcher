package de.brittytino.android.launcher.ui.widgets

import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import de.brittytino.android.launcher.actions.AppAction
import de.brittytino.android.launcher.apps.AppInfo
import de.brittytino.android.launcher.getUserFromId
import de.brittytino.android.launcher.ui.list.SelectActionActivity
import de.brittytino.android.launcher.widgets.FavoritesWidget

import de.brittytino.android.launcher.apps.AbstractAppInfo

class FavoritesView(
    private val activity: Activity,
    val widget: FavoritesWidget
) : LinearLayout(activity) {

    private val iconViews = mutableListOf<ImageView>()

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        
        // Ensure we take up space but center items
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        layoutParams = params
        
        // Add 4 slots
        for (i in 0 until 4) {
            val icon = ImageView(context)
            // Use dp for height
            val size = (56 * context.resources.displayMetrics.density).toInt()
            val lp = LayoutParams(0, size) 
            lp.weight = 1f
            lp.gravity = Gravity.CENTER
            
            icon.layoutParams = lp
            icon.scaleType = ImageView.ScaleType.FIT_CENTER
            
            val pad = (8 * context.resources.displayMetrics.density).toInt()
            icon.setPadding(pad, pad, pad, pad)
            
            icon.setOnClickListener {
                onSlotClick(i)
            }
            
            icon.setOnLongClickListener {
                onSlotLongClick(i)
                true
            }

            addView(icon)
            iconViews.add(icon)
        }
    }
    
    private fun refresh() {
        for (i in 0 until 4) {
            val data = widget.favorites[i]
            val view = iconViews[i]
            
            if (data != null) {
                try {
                    val parts = data.split("|")
                    val packageName = parts[0]
                    val userHash = parts.getOrNull(1)?.toIntOrNull() ?: AbstractAppInfo.INVALID_USER
                    
                    val appInfo = AppInfo(packageName, null, userHash)
                    val lai = appInfo.getLauncherActivityInfo(context)
                    
                    if (lai != null) {
                        view.setImageDrawable(lai.getBadgedIcon(0))
                    } else {
                        // Fallback if app not found/user not valid
                        view.setImageResource(android.R.drawable.sym_def_app_icon)
                    }
                } catch (e: Exception) {
                    view.setImageResource(android.R.drawable.sym_def_app_icon)
                }
            } else {
                view.setImageResource(android.R.drawable.ic_input_add) // Placeholder
                view.setColorFilter(Color.WHITE) // Ensure visible
            }
        }
    }
    
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        refresh()
    }
    
    private fun onSlotClick(index: Int) {
        val data = widget.favorites[index]
        if (data != null) {
            try {
                val parts = data.split("|")
                val pkg = parts[0]
                // Default to current user hash if missing (migration)
                // getUserFromId(INVALID_USER) might fail or handle it.
                // context.user.hashCode()?
                val userHash = parts.getOrNull(1)?.toIntOrNull() ?: android.os.Process.myUserHandle().hashCode()
                
                val appInfo = AppInfo(pkg, null, userHash)
                AppAction(appInfo).invoke(context)
                
            } catch (e: Exception) {
               Toast.makeText(context, "Cannot launch app", Toast.LENGTH_SHORT).show()
            }
        } else {
            SelectActionActivity.selectForWidget(context, widget.id, index)
        }
    }
    
    private fun onSlotLongClick(index: Int) {
         if (widget.favorites[index] != null) {
             SelectActionActivity.selectForWidget(context, widget.id, index)
         }
    }
}
