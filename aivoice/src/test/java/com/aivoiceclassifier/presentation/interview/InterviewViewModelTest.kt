package com.aivoiceclassifier.presentation.interview

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aivoiceclassifier.domain.model.Company
import com.aivoiceclassifier.domain.usecase.company.AddCompanyUseCase
import com.aivoiceclassifier.domain.usecase.company.GetAllCompaniesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InterviewViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: InterviewViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Create simple mock repository that returns empty flow
        val mockCompanyRepository = object : com.aivoiceclassifier.domain.repository.CompanyRepository {
            var companies: List<Company> = emptyList()
            var shouldSucceed = true
            var errorMessage = "Error"
            
            override fun getAllCompanies() = flowOf(companies)
            override suspend fun insertCompany(company: Company): Long = if (shouldSucceed) 1L else throw Exception(errorMessage)
            override suspend fun deleteCompany(company: Company) {}
            override suspend fun deleteCompanyById(id: Long) {}
            override suspend fun getCompanyById(id: Long): Company? = null
        }
        
        val getAllCompaniesUseCase = GetAllCompaniesUseCase(mockCompanyRepository)
        val addCompanyUseCase = AddCompanyUseCase(mockCompanyRepository)
        
        viewModel = InterviewViewModel(
            getAllCompaniesUseCase = getAllCompaniesUseCase,
            addCompanyUseCase = addCompanyUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be correct`() {
        val initialState = viewModel.uiState.value
        
        assertEquals(emptyList(), initialState.companies)
        assertNull(initialState.selectedCompany)
        assertFalse(initialState.showAddCompanyDialog)
        assertFalse(initialState.isLoading)
        assertNull(initialState.errorMessage)
        assertFalse(initialState.navigateToSession)
    }

    @Test
    fun `showAddCompanyDialog should update state correctly`() {
        // When
        viewModel.showAddCompanyDialog()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.showAddCompanyDialog)
    }

    @Test
    fun `dismissAddCompanyDialog should update state correctly`() {
        // Given
        viewModel.showAddCompanyDialog()
        assertTrue(viewModel.uiState.value.showAddCompanyDialog)
        
        // When
        viewModel.dismissAddCompanyDialog()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.showAddCompanyDialog)
    }

    @Test
    fun `addCompany with valid name should update state correctly`() = runTest {
        // When
        viewModel.addCompany("Google")
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.showAddCompanyDialog)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun `addCompany with blank name should show error`() = runTest {
        // When
        viewModel.addCompany("   ")
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Company name cannot be empty", state.errorMessage)
    }

    @Test
    fun `selectCompany should update state correctly`() {
        // Given
        val company = Company(id = 1L, name = "Google")
        
        // When
        viewModel.selectCompany(company)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(company, state.selectedCompany)
        assertTrue(state.navigateToSession)
    }

    @Test
    fun `onNavigationHandled should reset navigation state`() {
        // Given
        val company = Company(id = 1L, name = "Google")
        viewModel.selectCompany(company)
        assertTrue(viewModel.uiState.value.navigateToSession)
        
        // When
        viewModel.onNavigationHandled()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.navigateToSession)
    }

    @Test
    fun `clearError should clear error message`() = runTest {
        // Given - trigger an error
        viewModel.addCompany("   ") // This should cause an error
        assertEquals("Company name cannot be empty", viewModel.uiState.value.errorMessage)
        
        // When
        viewModel.clearError()
        
        // Then
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `should handle empty company name correctly`() = runTest {
        // When
        viewModel.addCompany("")
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Company name cannot be empty", state.errorMessage)
    }
} 