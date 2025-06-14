package com.aivoiceclassifier.presentation.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aivoiceclassifier.domain.model.Conversation
import com.aivoiceclassifier.domain.model.InterviewSession
import com.aivoiceclassifier.domain.model.SpeakerType
import com.aivoiceclassifier.domain.service.AudioClassificationService
import com.aivoiceclassifier.domain.usecase.ai.GetAIResponseUseCase
import com.aivoiceclassifier.domain.usecase.interview.StartInterviewSessionUseCase
import com.aivoiceclassifier.domain.repository.InterviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class InterviewSessionViewModel @Inject constructor(
    private val audioClassificationService: AudioClassificationService,
    private val getAIResponseUseCase: GetAIResponseUseCase,
    private val startInterviewSessionUseCase: StartInterviewSessionUseCase,
    private val interviewRepository: InterviewRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(InterviewSessionUiState())
    val uiState: StateFlow<InterviewSessionUiState> = _uiState.asStateFlow()
    
    private var currentSessionId: Long? = null
    private var isProcessingVoiceIdentification = false
    
    fun startSession(companyId: Long, companyName: String) {
        // Prevent multiple simultaneous session starts
        if (_uiState.value.isSessionActive || companyId <= 0 || companyName.isBlank()) {
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSessionActive = true)
            
            startInterviewSessionUseCase(companyId, companyName).fold(
                onSuccess = { sessionId ->
                    currentSessionId = sessionId
                    _uiState.value = _uiState.value.copy(
                        companyName = companyName,
                        isSessionActive = true,
                        errorMessage = null
                    )
                    startAudioListening()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Failed to start session",
                        isSessionActive = false
                    )
                }
            )
        }
    }
    
    fun stopSession() {
        if (!_uiState.value.isSessionActive) {
            return
        }
        
        audioClassificationService.stopListening()
        _uiState.value = _uiState.value.copy(
            isSessionActive = false,
            isProcessingAI = false,
            currentQuestion = "",
            isIdentifyingVoice = false,
            identifiedVoiceName = null
        )
        
        // Save final session
        currentSessionId?.let { sessionId ->
            viewModelScope.launch {
                try {
                    val currentSession = interviewRepository.getSessionById(sessionId)
                    currentSession?.let { session ->
                        val updatedSession = session.copy(
                            endTime = System.currentTimeMillis(),
                            conversations = _uiState.value.conversations
                        )
                        interviewRepository.updateSession(updatedSession)
                    }
                } catch (e: Exception) {
                    // Handle database error gracefully
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to save session: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun identifyVoice() {
        if (isProcessingVoiceIdentification || !_uiState.value.isSessionActive) {
            return
        }
        
        isProcessingVoiceIdentification = true
        _uiState.value = _uiState.value.copy(isIdentifyingVoice = true)
        
        viewModelScope.launch {
            try {
                // Simulate voice identification process
                delay(2000) // Simulate processing time
                
                // Identify voice types instead of names
                val voiceTypes = listOf("Human", "Machine", "AI")
                val identifiedType = voiceTypes.random()
                
                _uiState.value = _uiState.value.copy(
                    isIdentifyingVoice = false,
                    identifiedVoiceName = identifiedType
                )
                
                // Auto-clear after 5 seconds
                delay(5000)
                if (_uiState.value.identifiedVoiceName == identifiedType) {
                    _uiState.value = _uiState.value.copy(identifiedVoiceName = null)
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isIdentifyingVoice = false,
                    errorMessage = "Voice identification failed: ${e.message}"
                )
            } finally {
                isProcessingVoiceIdentification = false
            }
        }
    }
    
    fun addCorrection(conversationId: String, correction: String) {
        if (correction.isBlank()) return
        
        val updatedConversations = _uiState.value.conversations.map { conversation ->
            if (conversation.id == conversationId) {
                conversation.copy(correction = correction)
            } else {
                conversation
            }
        }
        _uiState.value = _uiState.value.copy(conversations = updatedConversations)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun exportTranscript(): String {
        val conversations = _uiState.value.conversations
        val companyName = _uiState.value.companyName
        
        return buildString {
            appendLine("Interview Transcript - $companyName")
            appendLine("=".repeat(50))
            appendLine()
            
            conversations.forEach { conversation ->
                val speakerIcon = when (conversation.speakerType) {
                    SpeakerType.HUMAN -> "🎤"
                    SpeakerType.AI -> "🔊"
                    SpeakerType.MACHINE -> "💻"
                    SpeakerType.UNKNOWN -> "❓"
                }
                
                appendLine("$speakerIcon Question: ${conversation.question}")
                appendLine("Answer: ${conversation.answer}")
                
                conversation.correction?.let { correction ->
                    appendLine("Correction: $correction")
                }
                
                appendLine()
            }
        }
    }
    
    private fun startAudioListening() {
        if (!_uiState.value.isSessionActive) {
            return
        }
        
        viewModelScope.launch {
            try {
                audioClassificationService.startListening().collect { result ->
                    // Only process if session is still active
                    if (_uiState.value.isSessionActive) {
                        handleAudioClassification(result.audioText, result.speakerType)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Audio processing error: ${e.message}",
                    isSessionActive = false
                )
            }
        }
    }
    
    private fun handleAudioClassification(audioText: String, speakerType: SpeakerType) {
        // Prevent processing if session is not active or if already processing
        if (!_uiState.value.isSessionActive || audioText.isBlank()) {
            return
        }
        
        viewModelScope.launch {
            when (speakerType) {
                SpeakerType.HUMAN -> {
                    // Human is asking a question - only update if different from current
                    if (_uiState.value.currentQuestion != audioText) {
                        _uiState.value = _uiState.value.copy(
                            currentQuestion = audioText,
                            isProcessingAI = false
                        )
                    }
                }
                SpeakerType.AI -> {
                    // AI/Speaker detected, get AI response
                    val currentQuestion = _uiState.value.currentQuestion
                    if (currentQuestion.isNotEmpty() && !_uiState.value.isProcessingAI) {
                        _uiState.value = _uiState.value.copy(isProcessingAI = true)
                        
                        try {
                            getAIResponseUseCase(currentQuestion).fold(
                                onSuccess = { aiResponse ->
                                    val conversation = Conversation(
                                        question = currentQuestion,
                                        answer = aiResponse,
                                        speakerType = speakerType
                                    )
                                    
                                    val updatedConversations = _uiState.value.conversations + conversation
                                    _uiState.value = _uiState.value.copy(
                                        conversations = updatedConversations,
                                        currentQuestion = "",
                                        isProcessingAI = false
                                    )
                                },
                                onFailure = { error ->
                                    _uiState.value = _uiState.value.copy(
                                        errorMessage = error.message ?: "Failed to get AI response",
                                        isProcessingAI = false
                                    )
                                }
                            )
                        } catch (e: Exception) {
                            _uiState.value = _uiState.value.copy(
                                errorMessage = "Unexpected error: ${e.message}",
                                isProcessingAI = false
                            )
                        }
                    }
                }
                SpeakerType.MACHINE -> {
                    // Machine voice detected - could be system notifications
                    // For now, just log it without triggering AI response
                    _uiState.value = _uiState.value.copy(
                        currentQuestion = audioText,
                        isProcessingAI = false
                    )
                }
                SpeakerType.UNKNOWN -> {
                    // Unknown voice type - handle gracefully
                    // Could be unclear audio or unrecognized speaker
                }
            }
        }
    }
}

data class InterviewSessionUiState(
    val companyName: String = "",
    val conversations: List<Conversation> = emptyList(),
    val currentQuestion: String = "",
    val isSessionActive: Boolean = false,
    val isProcessingAI: Boolean = false,
    val isIdentifyingVoice: Boolean = false,
    val identifiedVoiceName: String? = null,
    val errorMessage: String? = null
) 