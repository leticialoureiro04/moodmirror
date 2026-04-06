package com.leticia.moodmirror.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_records")
data class MoodRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val emotion: String,
    val faceDetected: Boolean,
    val isMoving: Boolean,
    val lightLux: Float,
    val overallMessage: String
)
