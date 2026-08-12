package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        WatchlistEntity::class,
        NotificationEntity::class,
        UserSessionEntity::class,
        NewsArticleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PulseXDatabase : RoomDatabase() {
    abstract fun dao(): PulseXDao

    companion object {
        @Volatile
        private var INSTANCE: PulseXDatabase? = null

        fun getDatabase(context: Context): PulseXDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PulseXDatabase::class.java,
                    "pulsex_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
