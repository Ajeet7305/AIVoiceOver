package com.aivoiceclassifier.data.local.dao

import androidx.room.*
import com.aivoiceclassifier.data.local.entity.InterviewSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InterviewSessionDao {
    
    @Query("SELECT * FROM interview_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<InterviewSessionEntity>>
    
    @Query("SELECT * FROM interview_sessions WHERE companyId = :companyId ORDER BY startTime DESC")
    fun getSessionsByCompany(companyId: Long): Flow<List<InterviewSessionEntity>>
    
    @Query("SELECT * FROM interview_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): InterviewSessionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: InterviewSessionEntity): Long
    
    @Update
    suspend fun updateSession(session: InterviewSessionEntity)
    
    @Delete
    suspend fun deleteSession(session: InterviewSessionEntity)
} 