/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * GPL-3.0 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.hikki.domain.usecase

import android.net.Uri
import moe.rukamori.hikki.domain.model.BackupSummary
import moe.rukamori.hikki.domain.model.RestoreMode
import moe.rukamori.hikki.domain.repository.BackupRepository
import javax.inject.Inject

class BackupUseCases
    @Inject
    constructor(
        private val repository: BackupRepository,
    ) {
        suspend fun exportBackup(destination: Uri): BackupSummary = repository.exportBackup(destination)

        suspend fun inspectBackup(source: Uri): BackupSummary = repository.inspectBackup(source)

        suspend fun restoreBackup(
            source: Uri,
            mode: RestoreMode,
        ): BackupSummary = repository.restoreBackup(source, mode)

        suspend fun exportMarkdown(destination: Uri): Int = repository.exportMarkdown(destination)
    }
