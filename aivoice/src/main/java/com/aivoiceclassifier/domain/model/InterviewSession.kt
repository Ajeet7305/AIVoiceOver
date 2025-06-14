package com.aivoiceclassifier.domain.model

data class InterviewSession(
    val id: Long = 0,
    val companyId: Long,
    val companyName: String,
    val conversations: List<Conversation> = emptyList(),
    val startTime: Long,
    val endTime: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class Conversation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val question: String,
    val answer: String,
    val correction: String? = null,
    val speakerType: SpeakerType,
    val timestamp: Long = System.currentTimeMillis()
) 