package com.aivoiceclassifier.data.repository

import com.aivoiceclassifier.data.local.dao.InterviewSessionDao
import com.aivoiceclassifier.data.local.entity.InterviewSessionEntity
import com.aivoiceclassifier.domain.model.Conversation
import com.aivoiceclassifier.domain.model.InterviewSession
import com.aivoiceclassifier.domain.repository.InterviewRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterviewRepositoryImpl @Inject constructor(
    private val interviewSessionDao: InterviewSessionDao,
    private val gson: Gson
) : InterviewRepository {
    
    override fun getAllSessions(): Flow<List<InterviewSession>> {
        return interviewSessionDao.getAllSessions().map { entities ->
            entities.map { it.toDomain(gson) }
        }
    }
    
    override fun getSessionsByCompany(companyId: Long): Flow<List<InterviewSession>> {
        return interviewSessionDao.getSessionsByCompany(companyId).map { entities ->
            entities.map { it.toDomain(gson) }
        }
    }
    
    override suspend fun getSessionById(id: Long): InterviewSession? {
        return interviewSessionDao.getSessionById(id)?.toDomain(gson)
    }
    
    override suspend fun insertSession(session: InterviewSession): Long {
        return interviewSessionDao.insertSession(session.toEntity(gson))
    }
    
    override suspend fun updateSession(session: InterviewSession) {
        interviewSessionDao.updateSession(session.toEntity(gson))
    }
    
    override suspend fun deleteSession(session: InterviewSession) {
        interviewSessionDao.deleteSession(session.toEntity(gson))
    }
}

// Extension functions for mapping
private fun InterviewSessionEntity.toDomain(gson: Gson): InterviewSession {
    val conversationType = object : TypeToken<List<Conversation>>() {}.type
    val conversations = try {
        gson.fromJson<List<Conversation>>(transcript, conversationType) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    
    return InterviewSession(
        id = id,
        companyId = companyId,
        companyName = companyName,
        conversations = conversations,
        startTime = startTime,
        endTime = endTime,
        createdAt = createdAt
    )
}

private fun InterviewSession.toEntity(gson: Gson): InterviewSessionEntity {
    return InterviewSessionEntity(
        id = id,
        companyId = companyId,
        companyName = companyName,
        transcript = gson.toJson(conversations),
        startTime = startTime,
        endTime = endTime,
        createdAt = createdAt
    )
} 