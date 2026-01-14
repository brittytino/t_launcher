package de.brittytino.android.launcher.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.brittytino.android.launcher.apps.AbstractDetailedAppInfo
import de.brittytino.android.launcher.data.AppDatabase
import de.brittytino.android.launcher.data.AppLaunchDelayEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AppLaunchDelayViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).appLaunchDelayDao()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Assuming we have a way to get list of installed apps. 
    // Usually via LauncherApps or PackageManager.
    // existing logic uses de.brittytino.android.launcher.Application.apps LiveData
    // We can observe it or just load apps here.
    
    // For now, let's just use a placeholder or better, expose the delays from DB 
    // and merge it with installed apps list in the UI. 
    // Since I need "App list with App icon, App name", I definitely need access to AppInfo.
    
    val delays: Flow<List<AppLaunchDelayEntity>> = dao.getAllDelays()

    fun updateDelay(packageName: String, delay: Int, enabled: Boolean) {
        viewModelScope.launch {
            dao.insertOrUpdate(AppLaunchDelayEntity(packageName, delay, enabled))
        }
    }

    fun removeDelay(packageName: String) {
        viewModelScope.launch {
            dao.delete(packageName)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
