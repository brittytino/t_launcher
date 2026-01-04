package app.olauncher

import android.app.Application
import app.olauncher.di.AppContainer
import app.olauncher.di.AppContainerImpl
import app.olauncher.data.Prefs

class TLauncherApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)
        // Prefs.init(this) // Prefs doesn't have an init method in the provided file, removing this if it causes issues
    }
}
