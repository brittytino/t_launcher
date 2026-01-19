package de.brittytino.android.launcher.leetcode.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.brittytino.android.launcher.Application
import de.brittytino.android.launcher.leetcode.data.LeetCodeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DeveloperPanelViewModel(
    private val repository: LeetCodeRepository
) : ViewModel() {
    
    val myProfile = repository.myProfile.stateIn(viewModelScope, SharingStarted.Lazily, null)
    val friends = repository.friends.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val dailyProblem = repository.dailyProblem.stateIn(viewModelScope, SharingStarted.Lazily, null)
    
    private val _loadingState = MutableStateFlow<Boolean>(false)
    val loadingState: StateFlow<Boolean> = _loadingState.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    init {
        viewModelScope.launch {
            // Fetch daily problem in background silently
            try {
                repository.syncDailyProblem()
            } catch (e: Exception) {
                // Ignore network errors for background sync
            }
            
            // Auto-refresh if stale (e.g. > 6 hours old) when VM is created (screen opened)
            myProfile.collect { profile ->
                if (profile != null) {
                    val staleness = System.currentTimeMillis() - profile.lastUpdated
                    if (staleness > 6 * 60 * 60 * 1000) {
                        sync(profile.username, true, silent = true)
                    }
                }
            }
        }
    }

    fun sync(username: String, isMe: Boolean = false, silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) _loadingState.value = true
            _errorState.value = null
            val result = repository.syncUser(username, isMe)
            if (result.isFailure) {
                // Only show error if explicit user action or if we have no data at all
                if (!silent || (isMe && myProfile.value == null)) {
                    val msg = result.exceptionOrNull()?.message ?: "Unknown error"
                    // User friendly error mapping
                    _errorState.value = if (msg.contains("Unable to resolve host")) {
                         // Only show offline error if we truly have no data to show
                         if (isMe && myProfile.value != null) null else "Offline Mode: Unable to connect"
                    } else msg
                }
            }
            if (!silent) _loadingState.value = false
        }
    }

    fun removeFriend(username: String) {
        viewModelScope.launch {
            repository.removeFriend(username)
        }
    }
    
    fun clearError() {
        _errorState.value = null
    }
}

class DeveloperPanelViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DeveloperPanelViewModel(app.leetCodeRepository) as T
    }
}
