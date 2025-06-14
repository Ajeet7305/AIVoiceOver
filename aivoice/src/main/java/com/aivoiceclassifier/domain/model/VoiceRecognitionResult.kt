package com.aivoiceclassifier.domain.model

data class VoiceRecognitionResult(
    val transcribedText: String,
    val speakerType: SpeakerType,
    val confidence: Float,
    val timestamp: Long = System.currentTimeMillis()
)

enum class SpeakerType {
    MACHINE,
    HUMAN,
    AI,
    UNKNOWN
}

data class InterviewNote(
    val id: String = java.util.UUID.randomUUID().toString(),
    val companyId: String,
    val content: String,
    val speakerType: SpeakerType,
    val timestamp: Long = System.currentTimeMillis()
) 