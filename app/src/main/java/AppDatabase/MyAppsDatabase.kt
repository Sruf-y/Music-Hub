package AppDatabase

import AppDatabase.SongRelated.Song
import AppDatabase.SongRelated.SongDao
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Song::class], version = 2)
abstract class MyAppsDatabase: RoomDatabase() {
    abstract fun songDao(): SongDao
}