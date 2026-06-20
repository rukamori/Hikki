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
