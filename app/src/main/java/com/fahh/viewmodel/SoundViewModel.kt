package com.fahh.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fahh.R
import com.fahh.audio.SoundManager
import com.fahh.data.model.Sound
import com.fahh.data.repository.SettingsRepository
import com.fahh.data.repository.SoundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SoundViewModel @Inject constructor(
    application: Application,
    private val repository: SoundRepository,
    private val soundManager: SoundManager,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

    val volume = settingsRepository.volumeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 1.0f
    )

    val allSounds: StateFlow<List<Sound>> = repository.allSounds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val walkthroughDone = settingsRepository.walkthroughDoneFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true // default true so walkthrough doesn't flash on existing users
    )

    val watermarkEnabled = settingsRepository.watermarkEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val streak: StateFlow<Int> = settingsRepository.streakFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val totalFahhCount: StateFlow<Int> = settingsRepository.totalFahhCountFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val highestComboTier: StateFlow<Int> = settingsRepository.highestComboTierFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val isFirstRun = settingsRepository.isFirstRunFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    /** Reads the actual DataStore value (not the stateIn initial). */
    suspend fun isFirstRunResolved(): Boolean = settingsRepository.isFirstRunFlow.first()

    private val _selectedSound = MutableStateFlow(Sound("Fahh", R.raw.fahh, "F"))
    val selectedSound: StateFlow<Sound> = _selectedSound.asStateFlow()

    /** Emits true when a rating prompt should be shown */
    private val _showRatingPrompt = MutableStateFlow(false)
    val showRatingPrompt: StateFlow<Boolean> = _showRatingPrompt.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.recordDailyActivity()
            val existing = repository.allSounds.first()
            if (existing.isEmpty()) {
                repository.insertAll(defaultSounds())
            } else {
                // Rename for existing users who have the old name
                repository.renameSound("Romance Sax", "Romantic")
            }
            // Auto-select last used sound on launch
            val lastName = settingsRepository.favoriteSoundFlow.first()
            if (lastName != null) {
                val sounds = repository.allSounds.first()
                val lastSound = sounds.find { it.name == lastName && !it.isLocked }
                if (lastSound != null) {
                    _selectedSound.value = lastSound
                }
            }
        }
    }

    fun updateVolume(newVolume: Float) {
        viewModelScope.launch {
            settingsRepository.updateVolume(newVolume)
        }
    }

    fun selectSound(sound: Sound) {
        if (!sound.isLocked) {
            _selectedSound.value = sound
            // Persist last selected sound so app opens with it
            viewModelScope.launch {
                settingsRepository.setFavoriteSound(sound.name)
            }
        }
    }

    fun updateHighestComboTier(tier: Int) {
        viewModelScope.launch {
            settingsRepository.setHighestComboTier(tier)
        }
    }

    fun completeWalkthrough() {
        viewModelScope.launch {
            settingsRepository.setWalkthroughDone()
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted()
        }
    }

    fun setWatermarkEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setWatermarkEnabled(enabled)
        }
    }

    fun playSelectedSound() {
        soundManager.playSound(_selectedSound.value.resId, volume.value)
        viewModelScope.launch {
            settingsRepository.incrementTotalFahhCount()
            checkSoundPlayMilestone()
        }
    }

    fun playSoundPreview(sound: Sound) {
        soundManager.playSound(sound.resId, volume.value)
    }

    fun unlockPack(packName: String) {
        viewModelScope.launch {
            repository.unlockPack(packName)
        }
    }

    fun unlockSound(soundName: String) {
        viewModelScope.launch {
            repository.unlockSound(soundName)
            checkUnlockMilestone()
        }
    }

    /**
     * Increments recording count and returns whether an ad should be shown.
     * Recordings 6–11: every 3rd (6, 9)
     * Recordings 12–19: every 2nd (12, 14, 16, 18)
     * Recordings 20+: every recording
     */
    suspend fun onRecordingFinished(): Boolean {
        val count = settingsRepository.incrementRecordingCount()
        return when {
            count >= 20 -> true
            count >= 12 -> count % 2 == 0
            count >= 6 -> (count - 6) % 3 == 0
            else -> false
        }
    }

    /** Call after a successful share */
    fun onShareCompleted() {
        viewModelScope.launch {
            val count = settingsRepository.incrementShareCount()
            if (count == 3) maybeShowRating()
        }
    }

    /** Check if sound play count hit a milestone (20th play for soundboard-only users) */
    private suspend fun checkSoundPlayMilestone() {
        val count = settingsRepository.incrementSoundPlayCount()
        if (count == 20) maybeShowRating()
    }

    /** Check if unlock count hit a milestone (2nd unlock) */
    private suspend fun checkUnlockMilestone() {
        val count = settingsRepository.incrementUnlockCount()
        if (count == 2) maybeShowRating()
    }

    /** Only show if user hasn't rated and hasn't dismissed too many times */
    private suspend fun maybeShowRating() {
        if (settingsRepository.hasRated()) return
        if (settingsRepository.getRatingDismissCount() >= 3) return
        _showRatingPrompt.value = true
    }

    fun onRatingAccepted() {
        _showRatingPrompt.value = false
        viewModelScope.launch { settingsRepository.setHasRated() }
    }

    fun onRatingDismissed() {
        _showRatingPrompt.value = false
        viewModelScope.launch { settingsRepository.incrementRatingDismissCount() }
    }

    override fun onCleared() {
        super.onCleared()
    }

    private fun defaultSounds(): List<Sound> = listOf(
        Sound("Fahh", R.raw.fahh, "F", isLocked = false, packName = "Free"),
        Sound("Bruh", R.raw.bruh, "B", isLocked = false, packName = "Free"),
        Sound("Vine Boom", R.raw.vine_boom, "V", isLocked = false, packName = "Free"),
        Sound("Wow", R.raw.wow, "W", isLocked = false, packName = "Free"),
        Sound("Air Horn", R.raw.air_horn, "A", isLocked = true, packName = "Chaos"),
        Sound("Dun Dunnn", R.raw.dun_dun_dunn, "D", isLocked = true, packName = "Reaction"),
        Sound("Oh My God", R.raw.oh_my_god_wow, "O", isLocked = true, packName = "Reaction"),
        Sound("Directed By", R.raw.directed_by, "R", isLocked = true, packName = "Classic"),
        Sound("Sudden Suspense", R.raw.sudden_suspense, "S", isLocked = true, packName = "Reaction"),
        Sound("Yoooo Japan", R.raw.yoooooo_japan, "Y", isLocked = true, packName = "Chaos"),
        Sound("Gop Gop Gop", R.raw.gop_gop_gop, "G", isLocked = true, packName = "Chaos"),
        Sound("Romantic", R.raw.romance_saxophone, "X", isLocked = true, packName = "Classic")
    )
}
