package com.aivoiceclassifier.domain.repository

interface AIAssistantRepository {
    suspend fun getAIResponse(question: String, context: String = ""): Result<String>
} 