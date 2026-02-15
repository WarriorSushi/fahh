package com.fahh.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SoundDao {
    @Query("SELECT * FROM sounds")
    fun getAllSounds(): Flow<List<SoundEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sounds: List<SoundEntity>)

    @Update
    suspend fun updateSound(sound: SoundEntity)

    @Query("UPDATE sounds SET isLocked = 0 WHERE packName = :packName")
    suspend fun unlockPack(packName: String)

    @Query("UPDATE sounds SET isLocked = 0 WHERE name = :soundName")
    suspend fun unlockSound(soundName: String)
}
