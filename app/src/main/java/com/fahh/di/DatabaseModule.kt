package com.fahh.di

import android.content.Context
import com.fahh.data.database.SoundDao
import com.fahh.data.database.SoundDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SoundDatabase {
        return SoundDatabase.getDatabase(context)
    }

    @Provides
    fun provideSoundDao(database: SoundDatabase): SoundDao {
        return database.soundDao()
    }
}
