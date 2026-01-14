package de.brittytino.android.launcher.ui.settings.wellbeing

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import de.brittytino.android.launcher.R
import de.brittytino.android.launcher.ui.settings.delay.AppLaunchDelayActivity

class SettingsFragmentWellbeing : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.wellbeing, rootKey)
        
        findPreference<Preference>(getString(R.string.settings_wellbeing_delay_key))?.apply {
            setOnPreferenceClickListener {
                startActivity(Intent(activity, AppLaunchDelayActivity::class.java))
                true
            }
        }
    }
}
