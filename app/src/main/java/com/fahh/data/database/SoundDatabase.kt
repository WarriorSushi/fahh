package com.fahh.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [SoundEntity::class], version = 2, exportSchema = true)
abstract class SoundDatabase : RoomDatabase() {
    abstract fun soundDao(): SoundDao

    companion object {
        /**
         * Replaces the generated integer key with a stable catalog key while preserving the
         * current lock state. The old display-name based rows are mapped explicitly so future
         * copy changes do not affect entitlements.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `sounds_new` (
                        `soundId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `resId` INTEGER NOT NULL,
                        `icon` TEXT NOT NULL,
                        `isLocked` INTEGER NOT NULL,
                        `packName` TEXT NOT NULL,
                        PRIMARY KEY(`soundId`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT OR REPLACE INTO `sounds_new` (`soundId`, `name`, `resId`, `icon`, `isLocked`, `packName`)
                    SELECT
                        CASE `name`
                            WHEN 'Fahh' THEN 'fahh'
                            WHEN 'Bruh' THEN 'bruh'
                            WHEN 'Vine Boom' THEN 'vine_boom'
                            WHEN 'Wow' THEN 'wow'
                            WHEN 'Air Horn' THEN 'air_horn'
                            WHEN 'Dun Dunnn' THEN 'dun_dunnn'
                            WHEN 'Oh My God' THEN 'oh_my_god'
                            WHEN 'Directed By' THEN 'directed_by'
                            WHEN 'Sudden Suspense' THEN 'sudden_suspense'
                            WHEN 'Yoooo Japan' THEN 'yoooo_japan'
                            WHEN 'Gop Gop Gop' THEN 'gop_gop_gop'
                            WHEN 'Romance Sax' THEN 'romantic'
                            WHEN 'Romantic' THEN 'romantic'
                            ELSE 'legacy_' || `id`
                        END,
                        `name`, `resId`, `icon`, `isLocked`, `packName`
                    FROM `sounds`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `sounds`")
                db.execSQL("ALTER TABLE `sounds_new` RENAME TO `sounds`")
            }
        }

        @Volatile
        private var INSTANCE: SoundDatabase? = null

        fun getDatabase(context: Context): SoundDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SoundDatabase::class.java,
                    "fahh_database"
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
