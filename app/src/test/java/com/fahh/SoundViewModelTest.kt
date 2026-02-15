package com.fahh

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.fahh.data.model.Sound
import com.fahh.viewmodel.SoundViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SoundViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SoundViewModel
    private val application = mockk<Application>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Note: Real DB would be mocked in a more complex setup
        viewModel = SoundViewModel(application)
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
