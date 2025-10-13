package AppDatabase

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MyDatabaseProvider {


    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Example: add a new column to the Song table
            database.execSQL("ALTER TABLE songs ADD COLUMN timesPlayed INTEGER DEFAULT 0 NOT NULL")
        }
    }


    @Volatile
    private var INSTANCE: MyAppsDatabase? = null

    fun getDatabase(context: Context): MyAppsDatabase{

        return INSTANCE?:synchronized(this){
            Room.databaseBuilder(context,
                MyAppsDatabase::class.java,
                "my-database")
                .build()
                .also {
                    INSTANCE = it
                }
        }
    }
}