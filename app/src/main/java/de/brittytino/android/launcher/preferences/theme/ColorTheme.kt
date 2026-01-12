package de.brittytino.android.launcher.preferences.theme

import android.content.Context
import android.content.res.Resources
import com.google.android.material.color.DynamicColors
import de.brittytino.android.launcher.R

enum class ColorTheme(
    private val id: Int,
    private val labelResource: Int,
    private val shadowId: Int,
    val isAvailable: () -> Boolean
) {
    DEFAULT(
        R.style.colorThemeDefault,
        R.string.settings_theme_color_theme_item_default,
        R.style.textShadow,
        { true }),
    DARK(
        R.style.colorThemeDark,
        R.string.settings_theme_color_theme_item_dark,
        R.style.textShadow,
        { true }),
    LIGHT(
        R.style.colorThemeLight,
        R.string.settings_theme_color_theme_item_light,
        R.style.textShadowLight,
        { true }),
    DYNAMIC(
        R.style.colorThemeDynamic,
        R.string.settings_theme_color_theme_item_dynamic,
        R.style.textShadow,
        { DynamicColors.isDynamicColorAvailable() }),
    ;

    fun applyToTheme(theme: Resources.Theme, shadow: Boolean) {
        val colorTheme = if (this.isAvailable()) this else DEFAULT
        theme.applyStyle(colorTheme.id, true)

        if (shadow) {
            theme.applyStyle(colorTheme.shadowId, true)
        }
    }

    fun getLabel(context: Context): String {
        return context.getString(labelResource)
    }
}