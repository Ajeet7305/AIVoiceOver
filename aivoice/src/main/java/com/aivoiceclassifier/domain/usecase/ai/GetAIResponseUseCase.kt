package com.aivoiceclassifier.domain.usecase.ai

import com.aivoiceclassifier.domain.repository.AIAssistantRepository
import javax.inject.Inject

class GetAIResponseUseCase @Inject constructor(
    private val aiAssistantRepository: AIAssistantRepository
) {
    suspend operator fun invoke(question: String, context: String = ""): Result<String> {
        return aiAssistantRepository.getAIResponse(question, context)
    }
} 