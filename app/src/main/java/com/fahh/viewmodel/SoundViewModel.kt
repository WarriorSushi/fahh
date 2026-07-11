package com.fahh.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fahh.audio.SoundManager
import com.fahh.audio.CustomSoundRecorder
import com.fahh.data.catalog.SoundCatalog
import com.fahh.data.model.Sound
import com.fahh.data.repository.SettingsRepository
import com.fahh.data.repository.SoundRepository
import com.fahh.data.repository.CustomSoundRepository
import com.fahh.utils.AudioTrimUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@HiltViewModel
class SoundViewModel @Inject constructor(
    application: Application,
    private val repository: SoundRepository,
    private val soundManager: SoundManager,
    private val settingsRepository: SettingsRepository,
    private val customSoundRepository: CustomSoundRepository,
    private val customSoundRecorder: CustomSoundRecorder
) : AndroidViewModel(application) {

    companion object {
        private const val CURRENT_UPDATE_ONBOARDING_VERSION = 1
    }

    val volume = settingsRepository.volumeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 1.0f
    )

    val allSounds: StateFlow<List<Sound>> = combine(repository.allSounds, customSoundRepository.sounds) { catalog, custom ->
        catalog + custom
    }
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

    val mySoundsUnlocked = settingsRepository.mySoundsUnlockedFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val customSoundSlots = settingsRepository.customSoundSlotsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    /** Reads the actual DataStore value (not the stateIn initial). */
    suspend fun isFirstRunResolved(): Boolean = settingsRepository.isFirstRunFlow.first()

    suspend fun shouldShowUpdateOnboarding(): Boolean =
        settingsRepository.updateOnboardingVersionFlow.first() < CURRENT_UPDATE_ONBOARDING_VERSION

    private val _selectedSound = MutableStateFlow(SoundCatalog.defaultSelectedSound)
    val selectedSound: StateFlow<Sound> = _selectedSound.asStateFlow()

    /** Emits true when a rating prompt should be shown */
    private val _showRatingPrompt = MutableStateFlow(false)
    val showRatingPrompt: StateFlow<Boolean> = _showRatingPrompt.asStateFlow()
    private val _isCustomRecording = MutableStateFlow(false)
    val isCustomRecording: StateFlow<Boolean> = _isCustomRecording.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.recordDailyActivity()
            repository.syncCatalog(SoundCatalog.sounds)

            // Prefer the stable selected ID, then fall back to the legacy name preference.
            val lastSoundId = settingsRepository.selectedSoundIdFlow.first()
            val lastName = settingsRepository.favoriteSoundFlow.first()
            val legacySoundId = when (lastName) {
                "Romance Sax" -> "romantic"
                else -> null
            }
            val sounds = combine(repository.allSounds, customSoundRepository.sounds) { catalog, custom ->
                catalog + custom
            }.first()
            val lastSound = sounds.find { it.id == lastSoundId && !it.isLocked }
                ?: sounds.find { it.id == legacySoundId && !it.isLocked }
                ?: sounds.find { it.name == lastName && !it.isLocked }
            if (lastSound != null) {
                _selectedSound.value = lastSound
                settingsRepository.setSelectedSoundId(lastSound.id)
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
                settingsRepository.setSelectedSoundId(sound.id)
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
            settingsRepository.setUpdateOnboardingVersion(CURRENT_UPDATE_ONBOARDING_VERSION)
        }
    }

    fun completeUpdateOnboarding() {
        viewModelScope.launch {
            settingsRepository.setUpdateOnboardingVersion(CURRENT_UPDATE_ONBOARDING_VERSION)
        }
    }

    fun setWatermarkEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setWatermarkEnabled(enabled)
        }
    }

    fun playSelectedSound() {
        soundManager.playSound(_selectedSound.value, volume.value)
        viewModelScope.launch {
            settingsRepository.incrementTotalFahhCount()
            checkSoundPlayMilestone()
        }
    }

    fun playSoundPreview(sound: Sound) {
        soundManager.playSound(sound, volume.value)
    }

    fun startCustomSoundRecording(): Result<Unit> = runCatching {
        check(customSoundRepository.sounds.value.size < customSoundSlots.value) {
            "Watch an ad to add a custom sound slot first."
        }
        customSoundRecorder.start()
        _isCustomRecording.value = true
    }

    fun stopCustomSoundRecording(name: String): Result<Sound> = runCatching {
        check(customSoundRepository.sounds.value.size < customSoundSlots.value) {
            "No custom sound slot is available."
        }
        val file = customSoundRecorder.stop()
        _isCustomRecording.value = false
        customSoundRepository.save(name, file)
    }.onFailure {
        _isCustomRecording.value = false
    }

    fun cancelCustomSoundRecording() {
        customSoundRecorder.cancel()
        _isCustomRecording.value = false
    }

    fun unlockMySounds() {
        viewModelScope.launch { settingsRepository.unlockMySounds() }
    }

    fun unlockNextCustomSoundSlot() {
        viewModelScope.launch { settingsRepository.unlockNextCustomSoundSlot() }
    }

    fun deleteCustomSound(soundId: String) {
        customSoundRepository.delete(soundId)
        if (_selectedSound.value.id == soundId) {
            selectSound(SoundCatalog.defaultSelectedSound)
        }
    }

    /** Keeps the selected local portion of a custom recording and updates its displayed name. */
    fun editCustomSound(soundId: String, name: String, startMs: Long, endMs: Long): Result<Sound> = runCatching {
        val sound = customSoundRepository.sounds.value.firstOrNull { it.id == soundId }
            ?: error("Custom sound no longer exists.")
        val source = File(checkNotNull(sound.filePath))
        val replacement = File(source.parentFile, "trim_${UUID.randomUUID()}.m4a")
        AudioTrimUtils.trimAudio(source, replacement, startMs, endMs)
        val updated = customSoundRepository.update(soundId, name, replacement)
        if (_selectedSound.value.id == soundId) _selectedSound.value = updated
        updated
    }

    fun unlockPack(packName: String) {
        viewModelScope.launch {
            repository.unlockPack(packName)
        }
    }

    fun unlockSound(soundId: String) {
        viewModelScope.launch {
            repository.unlockSound(soundId)
            checkUnlockMilestone()
        }
    }

    /** Records product usage without interrupting the saved-video review flow. */
    fun onRecordingFinished() {
        viewModelScope.launch {
            settingsRepository.incrementRecordingCount()
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
        customSoundRecorder.cancel()
        super.onCleared()
    }

}
