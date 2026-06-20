package moe.rukamori.hikki.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        NoteEntity::class,
        FolderEntity::class,
        TagEntity::class,
        CategoryEntity::class,
        NoteTagMap::class,
        NoteSearchEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class HikkiDatabase : RoomDatabase() {
    abstract val dao: HikkiDao

    companion object {
        private const val DatabaseName = "hikki.db"

        fun create(context: Context): HikkiDatabase =
            Room.databaseBuilder(context, HikkiDatabase::class.java, DatabaseName)
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .setQueryExecutor(java.util.concurrent.Executors.newFixedThreadPool(4))
                .setTransactionExecutor(java.util.concurrent.Executors.newFixedThreadPool(4))
                .build()
    }
}
