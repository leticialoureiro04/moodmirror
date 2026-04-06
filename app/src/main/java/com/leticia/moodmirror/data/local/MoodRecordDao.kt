package com.leticia.moodmirror.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MoodRecordEntity)

    @Query("SELECT * FROM mood_records ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<MoodRecordEntity>>
}
