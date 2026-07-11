package com.fahh

import android.app.Application
import com.fahh.data.model.Sound
import com.fahh.data.repository.SettingsRepository
import com.fahh.data.repository.SoundRepository
import com.fahh.data.repository.CustomSoundRepository
import com.fahh.audio.SoundManager
import com.fahh.audio.CustomSoundRecorder
import com.fahh.viewmodel.SoundViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SoundViewModelTest {

    private lateinit var viewModel: SoundViewModel
    private val application = mockk<Application>(relaxed = true)
    private val soundRepository = mockk<SoundRepository>(relaxed = true)
    private val soundManager = mockk<SoundManager>(relaxed = true)
    private val settingsRepository = mockk<SettingsRepository>(relaxed = true)
    private val customSoundRepository = mockk<CustomSoundRepository>(relaxed = true)
    private val customSoundRecorder = mockk<CustomSoundRecorder>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { soundRepository.allSounds } returns flowOf(emptyList())
        every { settingsRepository.volumeFlow } returns flowOf(1.0f)
        every { settingsRepository.walkthroughDoneFlow } returns flowOf(true)
        every { settingsRepository.streakFlow } returns flowOf(0)
        every { settingsRepository.totalFahhCountFlow } returns flowOf(0)
        every { settingsRepository.highestComboTierFlow } returns flowOf(0)
        every { settingsRepository.isFirstRunFlow } returns flowOf(false)
        every { settingsRepository.favoriteSoundFlow } returns flowOf(null)
        every { settingsRepository.selectedSoundIdFlow } returns flowOf(null)
        every { settingsRepository.mySoundsUnlockedFlow } returns flowOf(false)
        every { customSoundRepository.sounds } returns kotlinx.coroutines.flow.MutableStateFlow(emptyList())

        viewModel = SoundViewModel(
            application,
            soundRepository,
            soundManager,
            settingsRepository,
            customSoundRepository,
            customSoundRecorder
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selectSound should update selectedSound when sound is not locked`() {
        val sound = Sound("Test", 1, "🔊", isLocked = false)
        viewModel.selectSound(sound)
        assertEquals(sound, viewModel.selectedSound.value)
    }

    @Test
    fun `selectSound should NOT update selectedSound when sound is locked`() {
        val initialSound = viewModel.selectedSound.value
        val lockedSound = Sound("Locked", 2, "🔒", isLocked = true)
        viewModel.selectSound(lockedSound)
        assertEquals(initialSound, viewModel.selectedSound.value)
    }
}
