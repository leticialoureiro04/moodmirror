package com.leticia.moodmirror.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MoodRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MoodDatabase : RoomDatabase() {
    abstract fun moodRecordDao(): MoodRecordDao

    companion object {
        @Volatile
        private var instance: MoodDatabase? = null

        fun getInstance(context: Context): MoodDatabase {
            // Singleton para evitar multiplas instancias da BD em runtime.
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MoodDatabase::class.java,
                    "moodmirror_db"
                ).build().also { instance = it }
            }
        }
    }
}
