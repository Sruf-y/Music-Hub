package AppDatabase

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.db.SupportSQLiteDatabase

object MyDatabaseProvider {


    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Use separate ALTER TABLE statements
            database.execSQL("ALTER TABLE songs ADD COLUMN album_artist TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE songs ADD COLUMN size INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE songs ADD COLUMN mime_type TEXT NOT NULL DEFAULT ''")
            database.execSQL("ALTER TABLE songs ADD COLUMN year INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_3_4 = object : Migration(3,4){
        override fun migrate(connection: SQLiteConnection) {

        }
    }
    val MIGRATION_4_5 = object : Migration(4,5){
        override fun migrate(connection: SQLiteConnection) {

        }
    }
    val MIGRATION_5_6 = object : Migration(5,6){
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE songs ADD COLUMN album_id INTEGER")
        }
    }


    @Volatile
    private var INSTANCE: MyAppsDatabase? = null

    fun getDatabase(context: Context): MyAppsDatabase{
        return INSTANCE?:synchronized(this){
            Room.databaseBuilder(
                context,
                MyAppsDatabase::class.java,
                "my-database"
            )
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                .fallbackToDestructiveMigration()
                .build()
                .also {
                    INSTANCE = it
                }
        }
    }
}