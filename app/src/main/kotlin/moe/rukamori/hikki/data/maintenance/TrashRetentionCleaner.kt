/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * GPL-3.0 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.hikki.data.maintenance

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import moe.rukamori.hikki.di.ApplicationScope
import moe.rukamori.hikki.domain.repository.NotesRepository
import moe.rukamori.hikki.domain.repository.SettingsRepository
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrashRetentionCleaner
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
        private val notesRepository: NotesRepository,
        @ApplicationScope private val scope: CoroutineScope,
    ) {
        private val started = AtomicBoolean(false)

        fun start() {
            if (!started.compareAndSet(false, true)) return
            scope.launch {
                settingsRepository.settings
                    .map { it.trashRetention.days }
                    .distinctUntilChanged()
                    .collect { days ->
                        if (days != null) purge(days)
                    }
            }
        }

        private suspend fun purge(days: Int) {
            val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())
            try {
                notesRepository.purgeTrashedBefore(cutoff)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.w(LogTag, "Trash retention cleanup failed", error)
            }
        }

        private companion object {
            const val LogTag = "TrashRetention"
        }
    }
