package com.example.audiogid.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Файл лучше переименовать в AppDatabase.kt, чтобы имя совпадало с классом
@Database(
    entities = [Exhibit::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exhibitDao(): ExhibitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "audio_guide_database"
                )
                    // При повышении version без миграции приложение упадёт.
                    // На этапе разработки раскомментируйте следующую строку,
                    // в релизе — пишите полноценные Migration.
                    // .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
