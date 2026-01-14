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
            // Fetch daily problem in background
            repository.syncDailyProblem()
            
            // Auto-refresh if stale (e.g. > 6 hours old) when VM is created (screen opened)
            myProfile.collect { profile ->
                if (profile != null) {
                    val staleness = System.currentTimeMillis() - profile.lastUpdated
                    if (staleness > 6 * 60 * 60 * 1000) {
                        sync(profile.username, true)
                    }
                }
            }
        }
    }

    fun sync(username: String, isMe: Boolean = false) {
        viewModelScope.launch {
            _loadingState.value = true
            _errorState.value = null
            val result = repository.syncUser(username, isMe)
            if (result.isFailure) {
                _errorState.value = result.exceptionOrNull()?.message ?: "Unknown error"
            }
            _loadingState.value = false
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
