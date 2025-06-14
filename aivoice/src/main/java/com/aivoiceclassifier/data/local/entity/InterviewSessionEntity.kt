package com.aivoiceclassifier.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interview_sessions")
data class InterviewSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyId: Long,
    val companyName: String,
    val transcript: String, // JSON string of conversation
    val startTime: Long,
    val endTime: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) 