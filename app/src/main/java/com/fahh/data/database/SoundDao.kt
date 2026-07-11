package com.fahh.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SoundDao {
    @Query("SELECT * FROM sounds")
    fun getAllSounds(): Flow<List<SoundEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMissing(sounds: List<SoundEntity>)

    @Query("DELETE FROM sounds WHERE soundId NOT IN (:activeSoundIds)")
    suspend fun removeCatalogSoundsNoLongerShipped(activeSoundIds: Set<String>)

    @Query(
        """
        UPDATE sounds
        SET name = :name, resId = :resId, icon = :icon, packName = :packName
        WHERE soundId = :soundId
        """
    )
    suspend fun updateCatalogMetadata(
        soundId: String,
        name: String,
        resId: Int,
        icon: String,
        packName: String
    )

    @Query("UPDATE sounds SET isLocked = 0 WHERE packName = :packName")
    suspend fun unlockPack(packName: String)

    @Query("UPDATE sounds SET isLocked = 0 WHERE soundId = :soundId")
    suspend fun unlockSound(soundId: String)

    @Query("UPDATE sounds SET isLocked = 0 WHERE soundId IN (:soundIds)")
    suspend fun unlockSounds(soundIds: Set<String>)
}
