package app.olauncher.helper

import android.content.ContentResolver
import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.provider.Settings
import android.view.View
import android.widget.Toast
import app.olauncher.data.Prefs

object GrayscaleManager {

    fun applyGrayscale(context: Context, windowDecorView: View?, enable: Boolean) {
        val prefs = Prefs(context)
        
        // 1. Try System-Level (Preferred)
        val success = setSystemGrayscale(context, enable)
        
        // 2. If System-Level fails (no permission), apply App-Level Overlay to this window
        if (!success) {
            applyAppLevelGrayscale(windowDecorView, enable)
            if (enable && !prefs.hasShownGrayscaleWarning) {
                Toast.makeText(context, "For system-wide grayscale, grant WRITE_SECURE_SETTINGS via ADB.", Toast.LENGTH_LONG).show()
                prefs.hasShownGrayscaleWarning = true
            }
        } else {
            // If system level worked, ensure app level is cleared to avoid double dimming/computation
            applyAppLevelGrayscale(windowDecorView, false)
        }
    }

    private fun setSystemGrayscale(context: Context, enable: Boolean): Boolean {
        return try {
            val contentResolver = context.contentResolver
            if (canWriteSecureSettings(context)) {
                Settings.Secure.putInt(contentResolver, "accessibility_display_daltonizer_enabled", if (enable) 1 else 0)
                Settings.Secure.putInt(contentResolver, "accessibility_display_daltonizer", if (enable) 0 else -1) // 0 is Monochromacy usually
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun canWriteSecureSettings(context: Context): Boolean {
        return context.checkCallingOrSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun applyAppLevelGrayscale(view: View?, enable: Boolean) {
        view?.let {
            if (enable) {
                val cm = ColorMatrix()
                cm.setSaturation(0f)
                val paint = Paint()
                paint.colorFilter = ColorMatrixColorFilter(cm)
                it.setLayerType(View.LAYER_TYPE_HARDWARE, paint)
            } else {
                it.setLayerType(View.LAYER_TYPE_NONE, null)
            }
        }
    }
}
