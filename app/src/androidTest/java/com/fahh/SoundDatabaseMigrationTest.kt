package com.fahh

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fahh.data.database.SoundDatabase
import com.fahh.data.model.Sound
import com.fahh.data.repository.SoundRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SoundDatabaseMigrationTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val databaseName = "sound-migration-test.db"

    @After
    fun tearDown() {
        context.deleteDatabase(databaseName)
    }

    @Test
    fun migratesLegacyUnlocksAndSyncsNewCatalogEntries() = runBlocking {
        createVersionOneDatabase()

        val database = Room.databaseBuilder(context, SoundDatabase::class.java, databaseName)
            .addMigrations(SoundDatabase.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()

        try {
            val repository = SoundRepository(database.soundDao())
            val migratedSounds = repository.allSounds.first()
            val airHorn = migratedSounds.first { it.id == "air_horn" }
            val romantic = migratedSounds.first { it.id == "romantic" }

            assertFalse("A rewarded unlock must survive migration", airHorn.isLocked)
            assertFalse("The old Romance Sax row maps to Romantic", romantic.isLocked)

            repository.syncCatalog(
                listOf(
                    Sound(
                        name = "Air Horn Updated",
                        resId = 101,
                        icon = "A",
                        isLocked = true,
                        packName = "Chaos",
                        id = "air_horn"
                    ),
                    Sound(
                        name = "New Catalog Sound",
                        resId = 102,
                        icon = "N",
                        isLocked = true,
                        packName = "New",
                        id = "new_catalog_sound"
                    )
                )
            )

            val syncedSounds = repository.allSounds.first()
            val syncedAirHorn = syncedSounds.first { it.id == "air_horn" }
            val newSound = syncedSounds.first { it.id == "new_catalog_sound" }

            assertEquals("Air Horn Updated", syncedAirHorn.name)
            assertFalse("Catalog metadata must not relock an earned sound", syncedAirHorn.isLocked)
            assertTrue("New catalog entries use their configured default lock state", newSound.isLocked)
        } finally {
            database.close()
        }
    }

    private fun createVersionOneDatabase() {
        context.deleteDatabase(databaseName)
        val database = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null)
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `sounds` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `resId` INTEGER NOT NULL,
                `icon` TEXT NOT NULL,
                `isLocked` INTEGER NOT NULL,
                `packName` TEXT NOT NULL
            )
            """.trimIndent()
        )
        database.execSQL(
            "INSERT INTO `sounds` (`name`, `resId`, `icon`, `isLocked`, `packName`) VALUES (?, ?, ?, ?, ?)",
            arrayOf("Air Horn", 1, "A", 0, "Chaos")
        )
        database.execSQL(
            "INSERT INTO `sounds` (`name`, `resId`, `icon`, `isLocked`, `packName`) VALUES (?, ?, ?, ?, ?)",
            arrayOf("Romance Sax", 2, "X", 0, "Classic")
        )
        database.version = 1
        database.close()
    }
}
