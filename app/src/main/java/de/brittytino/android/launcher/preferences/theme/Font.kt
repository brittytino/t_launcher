package de.brittytino.android.launcher.preferences.theme

import android.content.res.Resources
import de.brittytino.android.launcher.R

/**
 * Changes here must also be added to @array/settings_theme_font_values
 */

@Suppress("unused")
enum class Font(val id: Int) {
    SYSTEM_DEFAULT(R.style.fontSystemDefault),
    INTER(R.style.fontInter),
    MANROPE(R.style.fontManrope),
    MONTSERRAT(R.style.fontMontserrat),
    LATO(R.style.fontLato),
    POPPINS(R.style.fontPoppins),
    NUNITO(R.style.fontNunito),
    SOURCE_SANS_3(R.style.fontSourceSans3),
    PROXIMA_NOVA(R.style.fontProximaNova),
    SYSTEM_SF(R.style.fontSystemSf),
    NOTO_SANS(R.style.fontNotoSans),
    // Kept for backward compatibility to prevent crashes on update
    HACK(R.style.fontHack),
    ;

    fun applyToTheme(theme: Resources.Theme) {
        theme.applyStyle(id, true)
    }
}