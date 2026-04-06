package com.leticia.moodmirror.data

import com.leticia.moodmirror.data.local.MoodRecordDao
import com.leticia.moodmirror.data.local.MoodRecordEntity
import kotlinx.coroutines.flow.Flow

class MoodRepository(
    private val dao: MoodRecordDao
) {
    fun observeRecords(): Flow<List<MoodRecordEntity>> = dao.observeAll()

    suspend fun insert(record: MoodRecordEntity) = dao.insert(record)
}
