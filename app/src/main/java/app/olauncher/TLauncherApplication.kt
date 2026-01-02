package app.olauncher

import android.app.Application
import app.olauncher.di.AppContainer
import app.olauncher.di.AppContainerImpl
import app.olauncher.helper.Prefs

class TLauncherApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)
        Prefs.init(this) // Initialize legacy Olauncher prefs if needed
    }
}
