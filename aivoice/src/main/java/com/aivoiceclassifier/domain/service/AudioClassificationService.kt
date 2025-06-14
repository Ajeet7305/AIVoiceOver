package com.aivoiceclassifier.domain.service

import com.aivoiceclassifier.domain.model.SpeakerType
import kotlinx.coroutines.flow.Flow

interface AudioClassificationService {
    fun startListening(): Flow<AudioClassificationResult>
    fun stopListening()
    fun isListening(): Boolean
}

data class AudioClassificationResult(
    val speakerType: SpeakerType,
    val confidence: Float,
    val audioText: String = "",
    val timestamp: Long = System.currentTimeMillis()
) 