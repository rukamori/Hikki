package moe.rukamori.hikki.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import moe.rukamori.hikki.data.db.HikkiDatabase
import moe.rukamori.hikki.data.repository.DataStoreSettingsRepository
import moe.rukamori.hikki.data.repository.OfflineNotesRepository
import moe.rukamori.hikki.domain.repository.NotesRepository
import moe.rukamori.hikki.domain.repository.SettingsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): HikkiDatabase = HikkiDatabase.create(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindNotesRepository(repository: OfflineNotesRepository): NotesRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(repository: DataStoreSettingsRepository): SettingsRepository
}
