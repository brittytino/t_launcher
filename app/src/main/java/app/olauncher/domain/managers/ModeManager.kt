package app.olauncher.domain.managers

import app.olauncher.data.Prefs
import app.olauncher.domain.model.AppMode
import app.olauncher.data.local.LogType
import app.olauncher.data.local.SystemLogEntity
import app.olauncher.data.repository.SystemLogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ModeManager(
    private val prefs: Prefs,
    private val systemLogRepository: SystemLogRepository? = null // Optional for now to avoid circular deps if any, but better mandatory
) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val _currentMode = MutableStateFlow(AppMode.valueOf(prefs.currentMode))
    val currentMode: StateFlow<AppMode> = _currentMode.asStateFlow()

    fun setMode(mode: AppMode) {
        if (_currentMode.value != mode) {
            _currentMode.value = mode
            prefs.currentMode = mode.name
            
            // Log it
            systemLogRepository?.let { repo ->
                scope.launch {
                    repo.logEvent(
                        SystemLogEntity(
                            timestamp = System.currentTimeMillis(),
                            type = LogType.MODE_CHANGE,
                            message = "Mode changed to ${mode.name}",
                            payload = mode.name
                        )
                    )
                }
            }
        }
    }

    fun getMode(): AppMode {
        return _currentMode.value
    }
}
