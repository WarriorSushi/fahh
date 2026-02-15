package com.fahh.data.repository

import com.fahh.data.database.SoundDao
import com.fahh.data.database.SoundEntity
import com.fahh.data.model.Sound
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundRepository @Inject constructor(private val soundDao: SoundDao) {

    val allSounds: Flow<List<Sound>> = soundDao.getAllSounds().map { entities ->
        entities.map { it.toModel() }
    }

    suspend fun insertAll(sounds: List<Sound>) {
        soundDao.insertAll(sounds.map { it.toEntity() })
    }

    suspend fun unlockPack(packName: String) {
        soundDao.unlockPack(packName)
    }

    private fun SoundEntity.toModel() = Sound(
        name = name,
        resId = resId,
        icon = icon,
        isLocked = isLocked,
        packName = packName
    )

    private fun Sound.toEntity() = SoundEntity(
        name = name,
        resId = resId,
        icon = icon,
        isLocked = isLocked,
        packName = packName
    )
}
