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

    val isFirstRun = settingsRepository.isFirstRunFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private val _selectedSound = MutableStateFlow(Sound("Fahh", R.raw.fahh, "F"))
    val selectedSound: StateFlow<Sound> = _selectedSound.asStateFlow()

    init {
        viewModelScope.launch {
            val existing = repository.allSounds.first()
            if (existing.isEmpty()) {
                repository.insertAll(defaultSounds())
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
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted()
        }
    }

    fun playSelectedSound() {
        soundManager.playSound(_selectedSound.value.resId, volume.value)
    }

    fun playSoundPreview(sound: Sound) {
        soundManager.playSound(sound.resId, volume.value)
    }

    fun unlockPack(packName: String) {
        viewModelScope.launch {
            repository.unlockPack(packName)
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
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
        Sound("Romance Sax", R.raw.romance_saxophone, "X", isLocked = true, packName = "Classic")
    )
}
