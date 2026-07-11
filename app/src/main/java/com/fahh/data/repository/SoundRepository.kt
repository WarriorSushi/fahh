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

    /** Adds newly bundled sounds and refreshes metadata without relocking earned sounds. */
    suspend fun syncCatalog(sounds: List<Sound>) {
        val entries = sounds.map { it.toEntity() }
        soundDao.insertMissing(entries)
        entries.forEach { sound ->
            soundDao.updateCatalogMetadata(
                soundId = sound.soundId,
                name = sound.name,
                resId = sound.resId,
                icon = sound.icon,
                packName = sound.packName
            )
        }
        // A catalog-level free sound must be free for existing installs too, not only on
        // first insertion. This never relocks sounds the user has already earned.
        soundDao.unlockSounds(sounds.filterNot { it.isLocked }.map { it.id }.toSet())
    }

    suspend fun unlockPack(packName: String) {
        soundDao.unlockPack(packName)
    }

    suspend fun unlockSound(soundId: String) {
        soundDao.unlockSound(soundId)
    }

    private fun SoundEntity.toModel() = Sound(
        name = name,
        resId = resId,
        icon = icon,
        isLocked = isLocked,
        packName = packName,
        id = soundId
    )

    private fun Sound.toEntity() = SoundEntity(
        soundId = id,
        name = name,
        resId = resId,
        icon = icon,
        isLocked = isLocked,
        packName = packName
    )
}
