package com.aivoiceclassifier.presentation.translator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aivoiceclassifier.domain.model.TranslatorMessage
import com.aivoiceclassifier.domain.model.LanguageDetectionResult
import com.aivoiceclassifier.domain.model.TranslationResult
import com.aivoiceclassifier.domain.service.AudioClassificationService
import com.aivoiceclassifier.domain.usecase.ai.GetAIResponseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject
import java.util.UUID

@HiltViewModel
class TranslatorViewModel @Inject constructor(
    private val audioClassificationService: AudioClassificationService,
    private val getAIResponseUseCase: GetAIResponseUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TranslatorUiState())
    val uiState: StateFlow<TranslatorUiState> = _uiState.asStateFlow()
    
    fun startListening() {
        _uiState.value = _uiState.value.copy(
            isListening = true,
            isProcessing = true,
            errorMessage = null
        )
        
        viewModelScope.launch {
            try {
                // Simulate voice recognition
                delay(2000) // Simulate listening time
                
                // Simulate detected speech (in a real app, this would come from speech recognition)
                val simulatedSpeech = getSimulatedSpeech()
                
                _uiState.value = _uiState.value.copy(
                    isListening = false,
                    isTranslating = true
                )
                
                // Simulate language detection and translation
                val detectionResult = detectLanguage(simulatedSpeech)
                val translationResult = translateText(simulatedSpeech, detectionResult.languageCode)
                
                // Add user message
                val userMessage = TranslatorMessage(
                    content = translationResult.translatedText,
                    isUser = true,
                    detectedLanguage = detectionResult.language,
                    originalText = if (detectionResult.languageCode != "en") simulatedSpeech else null
                )
                
                val updatedMessages = _uiState.value.messages + userMessage
                
                _uiState.value = _uiState.value.copy(
                    messages = updatedMessages,
                    lastDetectedLanguage = detectionResult.language,
                    isTranslating = false,
                    isProcessing = false
                )
                
                // Generate AI response
                generateAIResponse(translationResult.translatedText)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isListening = false,
                    isTranslating = false,
                    isProcessing = false,
                    errorMessage = "Failed to process speech: ${e.message}"
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
    
    fun toggleSpeakReply() {
        _uiState.value = _uiState.value.copy(
            isSpeakReplyEnabled = !_uiState.value.isSpeakReplyEnabled
        )
    }
    
    fun clearChat() {
        _uiState.value = _uiState.value.copy(
            messages = emptyList(),
            lastDetectedLanguage = null
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    private suspend fun generateAIResponse(userInput: String) {
        try {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            
            // Generate AI response using the existing use case
            val context = "You are a helpful translator assistant. Respond to the user's translated message in a helpful and conversational way."
            val aiResponseResult = getAIResponseUseCase(userInput, context)
            
            val aiResponse = aiResponseResult.getOrElse { 
                "I'm sorry, I couldn't process your request at the moment. Please try again."
            }
            
            val aiMessage = TranslatorMessage(
                content = aiResponse,
                isUser = false
            )
            
            val updatedMessages = _uiState.value.messages + aiMessage
            
            _uiState.value = _uiState.value.copy(
                messages = updatedMessages,
                isProcessing = false
            )
            
            // Simulate speech synthesis if enabled
            if (_uiState.value.isSpeakReplyEnabled) {
                simulateSpeechSynthesis(aiResponse)
            }
            
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                errorMessage = "Failed to generate AI response: ${e.message}"
            )
        }
    }
    
    private suspend fun simulateSpeechSynthesis(text: String) {
        // In a real app, this would use Android's TextToSpeech API
        // For now, we'll just simulate the process
        delay(1000)
    }
    
    private fun detectLanguage(text: String): LanguageDetectionResult {
        // Simulate language detection based on common phrases
        return when {
            text.contains("hola") || text.contains("gracias") || text.contains("buenos") -> 
                LanguageDetectionResult("Spanish", 0.95f, "es")
            text.contains("bonjour") || text.contains("merci") || text.contains("comment") -> 
                LanguageDetectionResult("French", 0.92f, "fr")
            text.contains("guten") || text.contains("danke") || text.contains("wie") -> 
                LanguageDetectionResult("German", 0.90f, "de")
            text.contains("namaste") || text.contains("dhanyawad") || text.contains("kaise") -> 
                LanguageDetectionResult("Hindi", 0.88f, "hi")
            text.contains("vanakkam") || text.contains("nandri") || text.contains("eppadi") -> 
                LanguageDetectionResult("Tamil", 0.87f, "ta")
            else -> LanguageDetectionResult("English", 0.85f, "en")
        }
    }
    
    private fun translateText(text: String, sourceLanguage: String): TranslationResult {
        // Simulate translation - in a real app, this would use a translation API
        val translatedText = when (sourceLanguage) {
            "es" -> when {
                text.contains("hola") -> "Hello"
                text.contains("gracias") -> "Thank you"
                text.contains("buenos días") -> "Good morning"
                else -> "Hello, how can I help you today?"
            }
            "fr" -> when {
                text.contains("bonjour") -> "Hello"
                text.contains("merci") -> "Thank you"
                text.contains("comment allez-vous") -> "How are you?"
                else -> "Hello, how can I help you today?"
            }
            "de" -> when {
                text.contains("guten tag") -> "Good day"
                text.contains("danke") -> "Thank you"
                text.contains("wie geht es") -> "How are you?"
                else -> "Hello, how can I help you today?"
            }
            "hi" -> when {
                text.contains("namaste") -> "Hello"
                text.contains("dhanyawad") -> "Thank you"
                text.contains("kaise hain") -> "How are you?"
                else -> "Hello, how can I help you today?"
            }
            "ta" -> when {
                text.contains("vanakkam") -> "Hello"
                text.contains("nandri") -> "Thank you"
                text.contains("eppadi irukkireenga") -> "How are you?"
                else -> "Hello, how can I help you today?"
            }
            else -> text // Already in English
        }
        
        return TranslationResult(
            originalText = text,
            translatedText = translatedText,
            sourceLanguage = sourceLanguage,
            targetLanguage = "en"
        )
    }
    
    private fun getSimulatedSpeech(): String {
        // Simulate different language inputs for demo purposes
        val simulatedInputs = listOf(
            "Hello, how are you today?",
            "Hola, ¿cómo estás hoy?",
            "Bonjour, comment allez-vous aujourd'hui?",
            "Guten Tag, wie geht es Ihnen heute?",
            "Namaste, aap kaise hain?",
            "Vanakkam, eppadi irukkireenga?",
            "Thank you for your help",
            "Gracias por tu ayuda",
            "Merci pour votre aide"
        )
        
        return simulatedInputs.random()
    }
}

data class TranslatorUiState(
    val messages: List<TranslatorMessage> = emptyList(),
    val isListening: Boolean = false,
    val isTranslating: Boolean = false,
    val isProcessing: Boolean = false,
    val isSpeakReplyEnabled: Boolean = true,
    val lastDetectedLanguage: String? = null,
    val errorMessage: String? = null
) 