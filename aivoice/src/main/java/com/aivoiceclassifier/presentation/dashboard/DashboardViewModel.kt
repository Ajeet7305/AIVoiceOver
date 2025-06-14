package com.aivoiceclassifier.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aivoiceclassifier.domain.model.DashboardItem
import com.aivoiceclassifier.domain.model.DashboardItemType
import com.aivoiceclassifier.domain.model.InterviewCompany
import com.aivoiceclassifier.domain.repository.InterviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val interviewRepository: InterviewRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        initializeDefaultItems()
        loadCompanies()
    }
    
    private fun initializeDefaultItems() {
        val defaultItems = listOf(
            DashboardItem(
                id = "interview",
                name = "Interview",
                type = DashboardItemType.INTERVIEW,
                isDefault = true
            ),
            DashboardItem(
                id = "native_speaker",
                name = "Native Language Speaker",
                type = DashboardItemType.NATIVE_LANGUAGE_SPEAKER,
                isDefault = true
            ),
            DashboardItem(
                id = "translator",
                name = "Real Language Translator",
                type = DashboardItemType.REAL_LANGUAGE_TRANSLATOR,
                isDefault = true
            )
        )
        
        _uiState.value = _uiState.value.copy(
            dashboardItems = defaultItems,
            selectedItem = defaultItems.first() // Default to Interview
        )
    }
    
    private fun loadCompanies() {
        // For now, we'll use a simple in-memory list
        // In a real app, this would load from the repository
        val sampleCompanies = listOf(
            InterviewCompany(
                id = UUID.randomUUID().toString(),
                name = "Google"
            ),
            InterviewCompany(
                id = UUID.randomUUID().toString(),
                name = "Microsoft"
            )
        )
        
        _uiState.value = _uiState.value.copy(companies = sampleCompanies)
    }
    
    fun onItemSelected(item: DashboardItem) {
        _uiState.value = _uiState.value.copy(selectedItem = item)
    }
    
    fun addCustomItem(itemName: String) {
        if (itemName.isNotBlank()) {
            val newItem = DashboardItem(
                id = UUID.randomUUID().toString(),
                name = itemName.trim(),
                type = DashboardItemType.CUSTOM,
                isDefault = false
            )
            
            val updatedItems = _uiState.value.dashboardItems + newItem
            _uiState.value = _uiState.value.copy(
                dashboardItems = updatedItems,
                selectedItem = newItem
            )
        }
    }
    
    fun addCompany(companyName: String) {
        if (companyName.isNotBlank()) {
            val newCompany = InterviewCompany(
                id = UUID.randomUUID().toString(),
                name = companyName.trim()
            )
            
            val updatedCompanies = _uiState.value.companies + newCompany
            _uiState.value = _uiState.value.copy(companies = updatedCompanies)
        }
    }
    
    fun navigateToTranslator() {
        _uiState.value = _uiState.value.copy(navigateToTranslator = true)
    }
    
    fun onNavigationHandled() {
        _uiState.value = _uiState.value.copy(
            navigateToTranslator = false
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class DashboardUiState(
    val dashboardItems: List<DashboardItem> = emptyList(),
    val selectedItem: DashboardItem? = null,
    val companies: List<InterviewCompany> = emptyList(),
    val navigateToTranslator: Boolean = false,
    val errorMessage: String? = null
) 