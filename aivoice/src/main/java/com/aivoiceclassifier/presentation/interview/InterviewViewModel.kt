package com.aivoiceclassifier.presentation.interview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aivoiceclassifier.domain.model.Company
import com.aivoiceclassifier.domain.usecase.company.AddCompanyUseCase
import com.aivoiceclassifier.domain.usecase.company.GetAllCompaniesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InterviewViewModel @Inject constructor(
    private val getAllCompaniesUseCase: GetAllCompaniesUseCase,
    private val addCompanyUseCase: AddCompanyUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(InterviewUiState())
    val uiState: StateFlow<InterviewUiState> = _uiState.asStateFlow()
    
    init {
        loadCompanies()
    }
    
    fun showAddCompanyDialog() {
        _uiState.value = _uiState.value.copy(showAddCompanyDialog = true)
    }
    
    fun dismissAddCompanyDialog() {
        _uiState.value = _uiState.value.copy(showAddCompanyDialog = false)
    }
    
    fun addCompany(companyName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            addCompanyUseCase(companyName).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        showAddCompanyDialog = false,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Failed to add company",
                        isLoading = false
                    )
                }
            )
        }
    }
    
    fun selectCompany(company: Company) {
        _uiState.value = _uiState.value.copy(
            selectedCompany = company,
            navigateToSession = true
        )
    }
    
    fun onNavigationHandled() {
        _uiState.value = _uiState.value.copy(navigateToSession = false)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    private fun loadCompanies() {
        viewModelScope.launch {
            getAllCompaniesUseCase().collect { companies ->
                _uiState.value = _uiState.value.copy(companies = companies)
            }
        }
    }
}

data class InterviewUiState(
    val companies: List<Company> = emptyList(),
    val selectedCompany: Company? = null,
    val showAddCompanyDialog: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val navigateToSession: Boolean = false
) 