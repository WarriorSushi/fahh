package com.fahh.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private companion object {
        const val SOUND_PRESS_PREFIX = "sound_press_"
    }

    private object PreferencesKeys {
        val VOLUME = floatPreferencesKey("volume")
        val FIRST_RUN = androidx.datastore.preferences.core.booleanPreferencesKey("first_run")
        val RECORDING_COUNT = intPreferencesKey("recording_count")
        val WALKTHROUGH_DONE = androidx.datastore.preferences.core.booleanPreferencesKey("walkthrough_done")
        val SHARE_COUNT = intPreferencesKey("share_count")
        val SOUND_PLAY_COUNT = intPreferencesKey("sound_play_count")
        val UNLOCK_COUNT = intPreferencesKey("unlock_count")
        val HAS_RATED = androidx.datastore.preferences.core.booleanPreferencesKey("has_rated")
        val RATING_DISMISS_COUNT = intPreferencesKey("rating_dismiss_count")
        val FAVORITE_SOUND = stringPreferencesKey("favorite_sound")
        val SELECTED_SOUND_ID = stringPreferencesKey("selected_sound_id")
        val WATERMARK_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("watermark_enabled")
        val STREAK_COUNT = intPreferencesKey("streak_count")
        val LAST_ACTIVE_DATE = stringPreferencesKey("last_active_date")
        val TOTAL_FAHH_COUNT = intPreferencesKey("total_fahh_count")
        val HIGHEST_COMBO_TIER = intPreferencesKey("highest_combo_tier")
        val MY_SOUNDS_UNLOCKED = androidx.datastore.preferences.core.booleanPreferencesKey("my_sounds_unlocked")
        val CUSTOM_SOUND_SLOTS = intPreferencesKey("custom_sound_slots")
        val UPDATE_ONBOARDING_VERSION = intPreferencesKey("update_onboarding_version")
    }

    val volumeFlow: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.VOLUME] ?: 1.0f
    }

    val isFirstRunFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FIRST_RUN] ?: true
    }

    suspend fun updateVolume(volume: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VOLUME] = volume
        }
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_RUN] = false
        }
    }

    val updateOnboardingVersionFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.UPDATE_ONBOARDING_VERSION] ?: 0
    }

    suspend fun setUpdateOnboardingVersion(version: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.UPDATE_ONBOARDING_VERSION] = version
        }
    }

    val recordingCountFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.RECORDING_COUNT] ?: 0
    }

    val walkthroughDoneFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WALKTHROUGH_DONE] ?: false
    }

    suspend fun setWalkthroughDone() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WALKTHROUGH_DONE] = true
        }
    }

    suspend fun incrementRecordingCount(): Int {
        var newCount = 0
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.RECORDING_COUNT] ?: 0
            newCount = current + 1
            preferences[PreferencesKeys.RECORDING_COUNT] = newCount
        }
        return newCount
    }

    suspend fun incrementShareCount(): Int {
        var newCount = 0
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.SHARE_COUNT] ?: 0
            newCount = current + 1
            preferences[PreferencesKeys.SHARE_COUNT] = newCount
        }
        return newCount
    }

    suspend fun incrementSoundPlayCount(): Int {
        var newCount = 0
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.SOUND_PLAY_COUNT] ?: 0
            newCount = current + 1
            preferences[PreferencesKeys.SOUND_PLAY_COUNT] = newCount
        }
        return newCount
    }

    suspend fun incrementUnlockCount(): Int {
        var newCount = 0
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.UNLOCK_COUNT] ?: 0
            newCount = current + 1
            preferences[PreferencesKeys.UNLOCK_COUNT] = newCount
        }
        return newCount
    }

    suspend fun hasRated(): Boolean {
        return context.dataStore.data.first()[PreferencesKeys.HAS_RATED] ?: false
    }

    suspend fun setHasRated() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_RATED] = true
        }
    }

    suspend fun incrementRatingDismissCount(): Int {
        var newCount = 0
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.RATING_DISMISS_COUNT] ?: 0
            newCount = current + 1
            preferences[PreferencesKeys.RATING_DISMISS_COUNT] = newCount
        }
        return newCount
    }

    suspend fun getRatingDismissCount(): Int {
        return context.dataStore.data.first()[PreferencesKeys.RATING_DISMISS_COUNT] ?: 0
    }

    val favoriteSoundFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FAVORITE_SOUND]
    }

    suspend fun setFavoriteSound(name: String?) {
        context.dataStore.edit { preferences ->
            if (name == null) {
                preferences.remove(PreferencesKeys.FAVORITE_SOUND)
            } else {
                preferences[PreferencesKeys.FAVORITE_SOUND] = name
            }
        }
    }

    val selectedSoundIdFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SELECTED_SOUND_ID]
    }

    suspend fun setSelectedSoundId(soundId: String?) {
        context.dataStore.edit { preferences ->
            if (soundId == null) {
                preferences.remove(PreferencesKeys.SELECTED_SOUND_ID)
            } else {
                preferences[PreferencesKeys.SELECTED_SOUND_ID] = soundId
            }
        }
    }

    val mySoundsUnlockedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.MY_SOUNDS_UNLOCKED] ?: false
    }

    suspend fun unlockMySounds() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MY_SOUNDS_UNLOCKED] = true
        }
    }

    /**
     * A rewarded custom-sound slot is local to this device. The old one-time unlock is
     * treated as five grandfathered slots so an early tester never loses access.
     */
    val customSoundSlotsFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CUSTOM_SOUND_SLOTS]
            ?: if (preferences[PreferencesKeys.MY_SOUNDS_UNLOCKED] == true) 5 else 0
    }

    suspend fun unlockNextCustomSoundSlot(): Int {
        var newSlotCount = 0
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.CUSTOM_SOUND_SLOTS]
                ?: if (preferences[PreferencesKeys.MY_SOUNDS_UNLOCKED] == true) 5 else 0
            newSlotCount = current + 1
            preferences[PreferencesKeys.CUSTOM_SOUND_SLOTS] = newSlotCount
        }
        return newSlotCount
    }

    val watermarkEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WATERMARK_ENABLED] ?: true
    }

    suspend fun setWatermarkEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WATERMARK_ENABLED] = enabled
        }
    }

    val streakFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.STREAK_COUNT] ?: 0
    }

    val totalFahhCountFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TOTAL_FAHH_COUNT] ?: 0
    }

    /** Local press totals, keyed by the stable sound ID. Never leave this device. */
    val soundPressCountsFlow: Flow<Map<String, Int>> = context.dataStore.data.map { preferences ->
        preferences.asMap().mapNotNull { (key, value) ->
            val id = key.name.removePrefix(SOUND_PRESS_PREFIX)
            if (key.name.startsWith(SOUND_PRESS_PREFIX) && value is Int) id to value else null
        }.toMap()
    }

    suspend fun recordDailyActivity() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        context.dataStore.edit { preferences ->
            val lastDate = preferences[PreferencesKeys.LAST_ACTIVE_DATE]
            when {
                lastDate == today -> { /* already recorded today */ }
                lastDate != null && LocalDate.parse(lastDate) == LocalDate.now().minusDays(1) -> {
                    preferences[PreferencesKeys.STREAK_COUNT] = (preferences[PreferencesKeys.STREAK_COUNT] ?: 0) + 1
                    preferences[PreferencesKeys.LAST_ACTIVE_DATE] = today
                }
                else -> {
                    preferences[PreferencesKeys.STREAK_COUNT] = 1
                    preferences[PreferencesKeys.LAST_ACTIVE_DATE] = today
                }
            }
        }
    }

    suspend fun incrementTotalFahhCount(): Int {
        var newCount = 0
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.TOTAL_FAHH_COUNT] ?: 0
            newCount = current + 1
            preferences[PreferencesKeys.TOTAL_FAHH_COUNT] = newCount
        }
        return newCount
    }

    suspend fun incrementSoundPress(soundId: String): Int {
        val key = intPreferencesKey("$SOUND_PRESS_PREFIX$soundId")
        var newCount = 0
        context.dataStore.edit { preferences ->
            newCount = (preferences[key] ?: 0) + 1
            preferences[key] = newCount
        }
        return newCount
    }

    val highestComboTierFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HIGHEST_COMBO_TIER] ?: 0
    }

    suspend fun setHighestComboTier(tier: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.HIGHEST_COMBO_TIER] ?: 0
            if (tier > current) {
                preferences[PreferencesKeys.HIGHEST_COMBO_TIER] = tier
            }
        }
    }
}
