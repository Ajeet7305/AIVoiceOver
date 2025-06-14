package com.aivoiceclassifier.data.service

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.speech.SpeechRecognizer
import com.aivoiceclassifier.domain.model.SpeakerType
import com.aivoiceclassifier.domain.service.AudioClassificationResult
import com.aivoiceclassifier.domain.service.AudioClassificationService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

@Singleton
class AudioClassificationServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AudioClassificationService {
    
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    
    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE_FACTOR = 2
        
        // Thresholds for classification (these would be trained values in a real implementation)
        private const val HUMAN_VOICE_THRESHOLD = 0.3f
        private const val AI_SPEAKER_THRESHOLD = 0.7f
    }
    
    override fun startListening(): Flow<AudioClassificationResult> = flow {
        isRecording = true
        
        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) * BUFFER_SIZE_FACTOR
        
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )
            
            audioRecord?.startRecording()
            val buffer = ShortArray(bufferSize)
            
            while (isRecording) {
                val readResult = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                
                if (readResult > 0) {
                    val audioFeatures = extractAudioFeatures(buffer, readResult)
                    val classificationResult = classifyAudio(audioFeatures)
                    emit(classificationResult)
                }
                
                delay(100) // Process every 100ms
            }
        } catch (e: Exception) {
            // Emit mock data for demo purposes when audio recording fails
            while (isRecording) {
                emit(generateMockClassificationResult())
                delay(1000)
            }
        } finally {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        }
    }
    
    override fun stopListening() {
        isRecording = false
    }
    
    override fun isListening(): Boolean = isRecording
    
    private fun extractAudioFeatures(buffer: ShortArray, length: Int): AudioFeatures {
        // Calculate RMS (Root Mean Square) for volume
        var sum = 0.0
        for (i in 0 until length) {
            sum += buffer[i] * buffer[i]
        }
        val rms = sqrt(sum / length)
        
        // Calculate zero crossing rate (simplified)
        var zeroCrossings = 0
        for (i in 1 until length) {
            if ((buffer[i] >= 0) != (buffer[i - 1] >= 0)) {
                zeroCrossings++
            }
        }
        val zeroCrossingRate = zeroCrossings.toFloat() / length
        
        // Calculate spectral centroid (simplified)
        var weightedSum = 0.0
        var magnitudeSum = 0.0
        for (i in 0 until length) {
            val magnitude = abs(buffer[i].toDouble())
            weightedSum += i * magnitude
            magnitudeSum += magnitude
        }
        val spectralCentroid = if (magnitudeSum > 0) weightedSum / magnitudeSum else 0.0
        
        return AudioFeatures(
            rms = rms.toFloat(),
            zeroCrossingRate = zeroCrossingRate,
            spectralCentroid = spectralCentroid.toFloat()
        )
    }
    
    private fun classifyAudio(features: AudioFeatures): AudioClassificationResult {
        // Simple classification logic (in a real app, this would use ML models)
        val confidence = when {
            features.rms > 1000 && features.zeroCrossingRate > 0.1 -> {
                // High volume and high zero crossing rate suggests human speech
                Random.nextFloat() * 0.3f + 0.7f // 0.7-1.0 confidence for human
            }
            features.rms > 500 && features.spectralCentroid > 1000 -> {
                // Medium volume with high spectral centroid suggests AI/speaker
                Random.nextFloat() * 0.3f + 0.7f // 0.7-1.0 confidence for AI
            }
            else -> {
                Random.nextFloat() * 0.5f + 0.3f // 0.3-0.8 confidence
            }
        }
        
        val speakerType = when {
            confidence > AI_SPEAKER_THRESHOLD && features.spectralCentroid > 1000 -> SpeakerType.AI
            features.rms > 800 -> SpeakerType.MACHINE
            confidence > HUMAN_VOICE_THRESHOLD -> SpeakerType.HUMAN
            else -> SpeakerType.UNKNOWN
        }
        
        return AudioClassificationResult(
            speakerType = speakerType,
            confidence = confidence,
            audioText = generateMockTranscription(speakerType)
        )
    }
    
    private fun generateMockClassificationResult(): AudioClassificationResult {
        val speakerTypes = listOf(SpeakerType.HUMAN, SpeakerType.AI, SpeakerType.MACHINE, SpeakerType.UNKNOWN)
        val speakerType = speakerTypes.random()
        return AudioClassificationResult(
            speakerType = speakerType,
            confidence = Random.nextFloat() * 0.4f + 0.6f, // 0.6-1.0
            audioText = generateMockTranscription(speakerType)
        )
    }
    
    private fun generateMockTranscription(speakerType: SpeakerType): String {
        return when (speakerType) {
            SpeakerType.HUMAN -> {
                val humanPhrases = listOf(
                    "Tell me about yourself",
                    "What are your strengths?",
                    "Why do you want to work here?",
                    "Describe a challenging project",
                    "Where do you see yourself in 5 years?"
                )
                humanPhrases.random()
            }
            SpeakerType.AI -> {
                val aiPhrases = listOf(
                    "I am a dedicated professional with strong technical skills",
                    "My key strengths include problem-solving and teamwork",
                    "I'm excited about this opportunity because...",
                    "In my previous role, I successfully delivered...",
                    "I see myself growing within this organization"
                )
                aiPhrases.random()
            }
            SpeakerType.MACHINE -> {
                val machinePhrases = listOf(
                    "System notification: Recording started",
                    "Audio input detected from machine source",
                    "Automated response generated"
                )
                machinePhrases.random()
            }
            SpeakerType.UNKNOWN -> {
                "Unclear audio input detected"
            }
        }
    }
}

private data class AudioFeatures(
    val rms: Float,
    val zeroCrossingRate: Float,
    val spectralCentroid: Float
) 