package com.aivoiceclassifier.domain.repository

import com.aivoiceclassifier.domain.model.InterviewSession
import kotlinx.coroutines.flow.Flow

interface InterviewRepository {
    fun getAllSessions(): Flow<List<InterviewSession>>
    fun getSessionsByCompany(companyId: Long): Flow<List<InterviewSession>>
    suspend fun getSessionById(id: Long): InterviewSession?
    suspend fun insertSession(session: InterviewSession): Long
    suspend fun updateSession(session: InterviewSession)
    suspend fun deleteSession(session: InterviewSession)
} 