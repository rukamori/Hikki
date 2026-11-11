/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * GPL-3.0 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.hikki.domain.repository

import android.net.Uri
import moe.rukamori.hikki.domain.model.BackupSummary
import moe.rukamori.hikki.domain.model.RestoreMode

interface BackupRepository {
    suspend fun exportBackup(destination: Uri): BackupSummary

    suspend fun inspectBackup(source: Uri): BackupSummary

    suspend fun restoreBackup(
        source: Uri,
        mode: RestoreMode,
    ): BackupSummary

    suspend fun exportMarkdown(destination: Uri): Int
}
