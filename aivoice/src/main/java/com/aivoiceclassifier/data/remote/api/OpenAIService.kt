package com.aivoiceclassifier.data.remote.api

import com.aivoiceclassifier.data.remote.dto.OpenAIRequest
import com.aivoiceclassifier.data.remote.dto.OpenAIResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAIService {
    
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: OpenAIRequest
    ): Response<OpenAIResponse>
} 