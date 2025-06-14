package com.aivoiceclassifier.feature.voiceclassifier.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.aivoiceclassifier.core.common.model.Result
import com.aivoiceclassifier.domain.audio.model.AudioClassification
import com.aivoiceclassifier.domain.audio.model.VoiceType
import com.aivoiceclassifier.domain.audio.repository.AudioRepository
import com.aivoiceclassifier.domain.audio.usecase.GetClassificationHistoryUseCase
import com.aivoiceclassifier.domain.audio.usecase.StartAudioClassificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VoiceClassifierViewModel @Inject constructor(
    private val audioRepository: AudioRepository,
    private val startAudioClassificationUseCase: StartAudioClassificationUseCase,
    private val getClassificationHistoryUseCase: GetClassificationHistoryUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(VoiceClassifierUiState())
    val uiState: StateFlow<VoiceClassifierUiState> = _uiState.asStateFlow()
    
    private val _classificationHistory = MutableStateFlow<List<AudioClassification>>(emptyList())
    val classificationHistory: StateFlow<List<AudioClassification>> = _classificationHistory.asStateFlow()
    
    init {
        observeClassificationHistory()
        loadStatistics()
    }
    
    fun startListening() {
        if (_uiState.value.isListening) return
        
        _uiState.value = _uiState.value.copy(
            isListening = true,
            error = null
        )
        
        viewModelScope.launch {
            startAudioClassificationUseCase()
                .collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            _uiState.value = _uiState.value.copy(isProcessing = true)
                        }
                        is Result.Success -> {
                            val classification = result.data
                            _uiState.value = _uiState.value.copy(
                                currentClassification = classification,
                                isProcessing = false
                            )
                            
                            // Save classification to database
                            saveClassification(classification)
                        }
                        is Result.Error -> {
                            _uiState.value = _uiState.value.copy(
                                error = result.exception.message ?: "Unknown error occurred",
                                isProcessing = false,
                                isListening = false
                            )
                        }
                    }
                }
        }
    }
    
    fun stopListening() {
        viewModelScope.launch {
            audioRepository.stopAudioRecording()
            _uiState.value = _uiState.value.copy(
                isListening = false,
                isProcessing = false
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun filterHistoryByType(type: VoiceType?) {
        _uiState.value = _uiState.value.copy(selectedFilter = type)
        observeClassificationHistory()
    }
    
    private fun observeClassificationHistory() {
        viewModelScope.launch {
            val flow = when (val filter = _uiState.value.selectedFilter) {
                null -> getClassificationHistoryUseCase.getAllClassifications()
                else -> getClassificationHistoryUseCase.getClassificationsByType(filter)
            }
            
            flow.collect { history ->
                _classificationHistory.value = history
            }
        }
    }
    
    private fun saveClassification(classification: AudioClassification) {
        viewModelScope.launch {
            audioRepository.saveClassification(classification)
            loadStatistics()
        }
    }
    
    private fun loadStatistics() {
        viewModelScope.launch {
            when (val result = audioRepository.getStatistics()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(statistics = result.data)
                }
                is Result.Error -> {
                    // Handle error silently for statistics
                }
                is Result.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }
    
    fun deleteClassification(classification: AudioClassification) {
        viewModelScope.launch {
            audioRepository.deleteClassification(classification)
            loadStatistics()
        }
    }
    
    fun clearOldClassifications() {
        viewModelScope.launch {
            val cutoffTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L) // 7 days ago
            audioRepository.deleteOldClassifications(cutoffTime)
            loadStatistics()
        }
    }
}

data class VoiceClassifierUiState(
    val isListening: Boolean = false,
    val isProcessing: Boolean = false,
    val currentClassification: AudioClassification? = null,
    val error: String? = null,
    val selectedFilter: VoiceType? = null,
    val statistics: Map<VoiceType, Int> = emptyMap()
) 