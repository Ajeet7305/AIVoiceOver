package com.aivoiceclassifier.domain.usecase.interview

import com.aivoiceclassifier.domain.model.InterviewSession
import com.aivoiceclassifier.domain.repository.InterviewRepository
import javax.inject.Inject

class StartInterviewSessionUseCase @Inject constructor(
    private val interviewRepository: InterviewRepository
) {
    suspend operator fun invoke(companyId: Long, companyName: String): Result<Long> {
        return try {
            val session = InterviewSession(
                companyId = companyId,
                companyName = companyName,
                startTime = System.currentTimeMillis()
            )
            val sessionId = interviewRepository.insertSession(session)
            Result.success(sessionId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 