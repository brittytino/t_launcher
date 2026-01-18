package de.brittytino.android.launcher.preferences.theme

import android.content.res.Resources
import de.brittytino.android.launcher.R

/**
 * Changes here must also be added to @array/settings_theme_font_values
 */

@Suppress("unused")
enum class Font(val id: Int) {
    HACK(R.style.fontHack),
    SYSTEM_DEFAULT(R.style.fontSystemDefault),
    SANS_SERIF(R.style.fontSansSerif),
    SERIF(R.style.fontSerifMonospace),
    MONOSPACE(R.style.fontMonospace),
    SERIF_MONOSPACE(R.style.fontSerifMonospace),
    SYSTEM_SF(R.style.fontSystemSf),
    INTER(R.style.fontInter),
    PROXIMA_NOVA(R.style.fontProximaNova),
    LATO(R.style.fontLato),
    MONTSERRAT(R.style.fontMontserrat),
    NUNITO(R.style.fontNunito),
    POPPINS(R.style.fontPoppins),
    SOURCE_SANS_3(R.style.fontSourceSans3),
    MANROPE(R.style.fontManrope),
    NOTO_SANS(R.style.fontNotoSans),
    ;

    fun applyToTheme(theme: Resources.Theme) {
        theme.applyStyle(id, true)
    }
}