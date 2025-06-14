package com.aivoiceclassifier.presentation.interview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aivoiceclassifier.domain.model.InterviewNote
import com.aivoiceclassifier.domain.model.SpeakerType
import com.aivoiceclassifier.domain.model.VoiceRecognitionResult
import com.aivoiceclassifier.domain.service.AudioClassificationService
import com.aivoiceclassifier.domain.usecase.interview.StartInterviewSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject
import java.util.UUID

@HiltViewModel
class CompanyInterviewViewModel @Inject constructor(
    private val audioClassificationService: AudioClassificationService,
    private val startInterviewSessionUseCase: StartInterviewSessionUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CompanyInterviewUiState())
    val uiState: StateFlow<CompanyInterviewUiState> = _uiState.asStateFlow()
    
    fun initializeInterview(companyId: String, companyName: String) {
        _uiState.value = _uiState.value.copy(
            companyId = companyId,
            companyName = companyName
        )
    }
    
    fun startSession() {
        _uiState.value = _uiState.value.copy(
            isSessionActive = true,
            sessionStartTime = System.currentTimeMillis()
        )
        
        viewModelScope.launch {
            try {
                // Initialize interview session using the use case
                // Convert companyId string to Long, use 1L as default for demo
                val companyIdLong = _uiState.value.companyId.toLongOrNull() ?: 1L
                startInterviewSessionUseCase(companyIdLong, _uiState.value.companyName)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to start session: ${e.message}"
                )
            }
        }
    }
    
    fun endSession() {
        _uiState.value = _uiState.value.copy(
            isSessionActive = false,
            isListening = false,
            isProcessing = false
        )
    }
    
    fun startListening() {
        _uiState.value = _uiState.value.copy(
            isListening = true,
            isProcessing = true,
            errorMessage = null
        )
        
        viewModelScope.launch {
            try {
                // Simulate voice recognition process
                delay(3000) // Simulate listening time
                
                // Simulate voice recognition result
                val recognitionResult = simulateVoiceRecognition()
                
                _uiState.value = _uiState.value.copy(
                    isListening = false,
                    isProcessing = false,
                    lastSpeakerType = recognitionResult.speakerType
                )
                
                // Add note to the notepad
                addNote(recognitionResult)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isListening = false,
                    isProcessing = false,
                    errorMessage = "Failed to process voice: ${e.message}"
                )
            }
        }
    }
    
    fun stopListening() {
        _uiState.value = _uiState.value.copy(
            isListening = false,
            isProcessing = false
        )
    }
    
    fun exportNotes() {
        viewModelScope.launch {
            try {
                // In a real app, this would export notes to a file or share them
                val notesText = generateNotesText()
                
                // For now, we'll just simulate the export process
                _uiState.value = _uiState.value.copy(
                    isProcessing = true
                )
                
                delay(1000) // Simulate export process
                
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    exportMessage = "Notes exported successfully!"
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    errorMessage = "Failed to export notes: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            exportMessage = null
        )
    }
    
    private fun addNote(recognitionResult: VoiceRecognitionResult) {
        val note = InterviewNote(
            companyId = _uiState.value.companyId,
            content = recognitionResult.transcribedText,
            speakerType = recognitionResult.speakerType,
            timestamp = recognitionResult.timestamp
        )
        
        val updatedNotes = _uiState.value.notes + note
        _uiState.value = _uiState.value.copy(notes = updatedNotes)
    }
    
    private fun simulateVoiceRecognition(): VoiceRecognitionResult {
        // Simulate different types of voice recognition results
        val sampleResults = listOf(
            VoiceRecognitionResult(
                transcribedText = "Tell me about your experience with Android development.",
                speakerType = SpeakerType.MACHINE,
                confidence = 0.95f
            ),
            VoiceRecognitionResult(
                transcribedText = "I have been working with Android for over 3 years, focusing on Kotlin and Jetpack Compose.",
                speakerType = SpeakerType.HUMAN,
                confidence = 0.88f
            ),
            VoiceRecognitionResult(
                transcribedText = "What are your strengths in mobile development?",
                speakerType = SpeakerType.MACHINE,
                confidence = 0.92f
            ),
            VoiceRecognitionResult(
                transcribedText = "My strengths include MVVM architecture, clean code practices, and UI/UX design.",
                speakerType = SpeakerType.HUMAN,
                confidence = 0.85f
            ),
            VoiceRecognitionResult(
                transcribedText = "Can you explain the difference between Activity and Fragment?",
                speakerType = SpeakerType.MACHINE,
                confidence = 0.90f
            ),
            VoiceRecognitionResult(
                transcribedText = "This is an AI-generated response about Android components.",
                speakerType = SpeakerType.AI,
                confidence = 0.75f
            ),
            VoiceRecognitionResult(
                transcribedText = "Unclear audio input detected.",
                speakerType = SpeakerType.UNKNOWN,
                confidence = 0.45f
            )
        )
        
        return sampleResults.random()
    }
    
    private fun generateNotesText(): String {
        val notes = _uiState.value.notes
        if (notes.isEmpty()) return "No notes available."
        
        val sb = StringBuilder()
        sb.appendLine("Interview Notes - ${_uiState.value.companyName}")
        sb.appendLine("Session Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(_uiState.value.sessionStartTime))}")
        sb.appendLine("Total Entries: ${notes.size}")
        sb.appendLine()
        sb.appendLine("=".repeat(50))
        sb.appendLine()
        
        notes.forEachIndexed { index, note ->
            val timeStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date(note.timestamp))
            
            sb.appendLine("${index + 1}. [$timeStr] ${getSpeakerTypeText(note.speakerType)}")
            sb.appendLine("   ${note.content}")
            
            if (note.speakerType == SpeakerType.HUMAN || note.speakerType == SpeakerType.AI) {
                sb.appendLine("   [IGNORED - Human/AI Voice]")
            }
            
            sb.appendLine()
        }
        
        return sb.toString()
    }
    
    private fun getSpeakerTypeText(speakerType: SpeakerType): String {
        return when (speakerType) {
            SpeakerType.MACHINE -> "Machine Voice"
            SpeakerType.HUMAN -> "Human Voice"
            SpeakerType.AI -> "AI Voice"
            SpeakerType.UNKNOWN -> "Unknown Voice"
        }
    }
}

data class CompanyInterviewUiState(
    val companyId: String = "",
    val companyName: String = "",
    val isSessionActive: Boolean = false,
    val isListening: Boolean = false,
    val isProcessing: Boolean = false,
    val notes: List<InterviewNote> = emptyList(),
    val lastSpeakerType: SpeakerType? = null,
    val sessionStartTime: Long = 0L,
    val errorMessage: String? = null,
    val exportMessage: String? = null
) 