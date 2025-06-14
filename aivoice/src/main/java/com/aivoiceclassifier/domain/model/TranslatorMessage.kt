package com.aivoiceclassifier.domain.model

data class TranslatorMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val content: String,
    val isUser: Boolean,
    val detectedLanguage: String? = null,
    val originalText: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class LanguageDetectionResult(
    val language: String,
    val confidence: Float,
    val languageCode: String
)

data class TranslationResult(
    val originalText: String,
    val translatedText: String,
    val sourceLanguage: String,
    val targetLanguage: String = "en"
) 