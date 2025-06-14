package com.aivoiceclassifier.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OpenAIRequest(
    @SerializedName("model")
    val model: String = "gpt-3.5-turbo",
    @SerializedName("messages")
    val messages: List<Message>,
    @SerializedName("max_tokens")
    val maxTokens: Int = 150,
    @SerializedName("temperature")
    val temperature: Double = 0.7
)

data class Message(
    @SerializedName("role")
    val role: String, // "system", "user", "assistant"
    @SerializedName("content")
    val content: String
)

data class OpenAIResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("object")
    val objectType: String,
    @SerializedName("created")
    val created: Long,
    @SerializedName("model")
    val model: String,
    @SerializedName("choices")
    val choices: List<Choice>,
    @SerializedName("usage")
    val usage: Usage
)

data class Choice(
    @SerializedName("index")
    val index: Int,
    @SerializedName("message")
    val message: Message,
    @SerializedName("finish_reason")
    val finishReason: String
)

data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int
) 