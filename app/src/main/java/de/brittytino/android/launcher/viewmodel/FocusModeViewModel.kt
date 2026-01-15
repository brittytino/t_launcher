package de.brittytino.android.launcher.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import de.brittytino.android.launcher.apps.AbstractDetailedAppInfo
import de.brittytino.android.launcher.data.FocusModeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.SecureRandom
import android.os.CountDownTimer

class FocusModeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FocusModeRepository(application)
    private val _appList = (application as de.brittytino.android.launcher.Application).apps 
    val appList: LiveData<List<AbstractDetailedAppInfo>> = _appList

    private val _focusState = MutableStateFlow(FocusStateModel(
        isActive = repository.focusState != FocusModeRepository.FocusState.INACTIVE,
        state = repository.focusState,
        focusApps = repository.focusApps,
        lockType = repository.lockType,
        isQuietMode = repository.isQuietMode,
        customPassword = repository.customPassword,
        pauseTimeRemaining = repository.pauseTimeRemaining,
        lastPauseTimestamp = repository.lastPauseTimestamp
    ))
    val focusState: StateFlow<FocusStateModel> = _focusState.asStateFlow()

    private val _unlockPhrase = MutableStateFlow(repository.currentSessionPhrase ?: generateRandomPhrase())
    val unlockPhrase: StateFlow<String> = _unlockPhrase.asStateFlow()

    private var activePauseTimer: CountDownTimer? = null

    init {
        // Reload state on init to ensure we are in sync with repository/UI
        refreshState()
        checkPauseExpiration()
    }

    private fun checkPauseExpiration() {
        if (repository.focusState == FocusModeRepository.FocusState.PAUSED) {
            val elapsed = System.currentTimeMillis() - repository.lastPauseTimestamp
            if (elapsed >= repository.pauseTimeRemaining) {
                 repository.pauseTimeRemaining = 0
                 // Auto-resume if time expired
                 resumeFocus() 
            } else {
                 val remaining = repository.pauseTimeRemaining - elapsed
                 startPauseTimer(remaining)
            }
        }
    }

    private fun startPauseTimer(duration: Long) {
        activePauseTimer?.cancel()
        activePauseTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                 repository.pauseTimeRemaining = millisUntilFinished
                 // Update state periodically if needed, but for now we just rely on end
            }
            override fun onFinish() {
                resumeFocus()
            }
        }.start()
    }

    private fun refreshState() {
         _focusState.value = FocusStateModel(
            isActive = repository.focusState != FocusModeRepository.FocusState.INACTIVE,
            state = repository.focusState,
            focusApps = repository.focusApps,
            lockType = repository.lockType,
            isQuietMode = repository.isQuietMode,
            customPassword = repository.customPassword,
            pauseTimeRemaining = repository.pauseTimeRemaining,
            lastPauseTimestamp = repository.lastPauseTimestamp
        )
        if (repository.focusState != FocusModeRepository.FocusState.INACTIVE) {
            _unlockPhrase.value = repository.currentSessionPhrase ?: ""
        }
    }

    fun updateFocusApps(packageName: String, isSelected: Boolean) {
        val current = _focusState.value.focusApps.toMutableSet()
        if (isSelected) current.add(packageName) else current.remove(packageName)
        repository.focusApps = current
        _focusState.value = _focusState.value.copy(focusApps = current)
    }

    fun setLockType(type: FocusModeRepository.LockType) {
        repository.lockType = type
        _focusState.value = _focusState.value.copy(lockType = type)
    }

    fun setCustomPassword(password: String?) {
        repository.customPassword = password
        _focusState.value = _focusState.value.copy(customPassword = password)
    }

    fun setQuietMode(enabled: Boolean) {
        repository.isQuietMode = enabled
        _focusState.value = _focusState.value.copy(isQuietMode = enabled)
    }

    // Returns null if success, else error message (e.g. password required)
    fun confirmStartFocus(): String? {
        if (_focusState.value.lockType == FocusModeRepository.LockType.CUSTOM_PASSWORD && 
            _focusState.value.customPassword.isNullOrEmpty()) {
            return "Password setup required" 
        }

        val phrase = generateRandomPhrase()
        repository.currentSessionPhrase = phrase
        _unlockPhrase.value = phrase
        
        repository.pauseTimeRemaining = 120000L // Reset to 2 minutes
        
        // Ensure last pause timestamp is far in the past so first pause is allowed immediately? or strict?
        // Requirement: "Focus may only be paused once every 15 minutes." 
        // If I start fresh, I should probably allow pause.
        // If I just finished a session where I paused, and I start again immediately... 
        // Strict: "Persist... compare against current time". So it carries over sessions.
        
        repository.focusState = FocusModeRepository.FocusState.ACTIVE
        refreshState()
        return null
    }

    // Renamed because it's now internal logic after confirmation
    fun attemptStartFocus(): Boolean {
         return confirmStartFocus() == null
    }
    
    // Returns remaining cooldown in millis, or 0 if allowed
    fun getPauseCooldownCallback(): Long {
        val now = System.currentTimeMillis()
        val elapsed = now - repository.lastPauseTimestamp
        val cooldown = 15 * 60 * 1000L // 15 minutes
        return if (elapsed < cooldown) (cooldown - elapsed) else 0L
    }

    fun pauseFocus() {
        if (repository.focusState == FocusModeRepository.FocusState.ACTIVE) {
            val cooldown = getPauseCooldownCallback()
            if (cooldown <= 0) {
                 repository.lastPauseTimestamp = System.currentTimeMillis()
                 repository.focusState = FocusModeRepository.FocusState.PAUSED
                 repository.pauseTimeRemaining = 120000L // Fresh 2m? 
                 // Req: "start a non-extendable 2-minute timer".
                 // "Do not allow the pause timer to be reset or extended."
                 // This implies 2 minutes fixed.
                 
                 startPauseTimer(repository.pauseTimeRemaining)
                 refreshState()
            }
        }
    }

    fun resumeFocus() {
        if (repository.focusState == FocusModeRepository.FocusState.PAUSED) {
            activePauseTimer?.cancel()
            val elapsed = System.currentTimeMillis() - repository.lastPauseTimestamp
            val remaining = repository.pauseTimeRemaining - elapsed
            repository.pauseTimeRemaining = if (remaining < 0) 0 else remaining

            repository.focusState = FocusModeRepository.FocusState.ACTIVE
            refreshState()
        }
    }

    fun stopFocus() {
        activePauseTimer?.cancel()
        repository.focusState = FocusModeRepository.FocusState.INACTIVE
        repository.currentSessionPhrase = null
        refreshState()
    }

    fun requestUnlock() {
        // Store previous state (ACTIVE or PAUSED) to restore if cancelled
        repository.previousFocusState = repository.focusState
        repository.focusState = FocusModeRepository.FocusState.UNLOCK_PENDING
        refreshState()
    }

    fun cancelUnlock() {
        if (repository.focusState == FocusModeRepository.FocusState.UNLOCK_PENDING) {
             // Restore previous state. Default to ACTIVE if something is wrong.
             val prev = repository.previousFocusState
             repository.focusState = if (prev == FocusModeRepository.FocusState.INACTIVE) FocusModeRepository.FocusState.ACTIVE else prev
             refreshState()
        }
    }

    fun regeneratePhrase() {
        _unlockPhrase.value = generateRandomPhrase()
    }

    private fun generateRandomPhrase(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val sr = SecureRandom()
        return (1..12)
            .map { chars[sr.nextInt(chars.length)] }
            .joinToString("")
    }

    data class FocusStateModel(
        val isActive: Boolean,
        val state: FocusModeRepository.FocusState,
        val focusApps: Set<String>,
        val lockType: FocusModeRepository.LockType,
        val isQuietMode: Boolean,
        val customPassword: String?,
        val pauseTimeRemaining: Long,
        val lastPauseTimestamp: Long
    )
}

