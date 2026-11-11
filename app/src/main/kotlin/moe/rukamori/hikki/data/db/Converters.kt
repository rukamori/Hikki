/*
 * Hikki (2026)
 * Copyright (C) Rukamori - github.com/rukamori
 * GPL-3.0 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.hikki.data.db

import androidx.room.TypeConverter
import moe.rukamori.hikki.domain.model.NoteLifecycle

class Converters {
    @TypeConverter
    fun noteLifecycleToString(value: NoteLifecycle): String = value.name

    @TypeConverter
    fun stringToNoteLifecycle(value: String): NoteLifecycle =
        runCatching { NoteLifecycle.valueOf(value) }.getOrDefault(NoteLifecycle.Active)
}
