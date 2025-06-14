package com.aivoiceclassifier.presentation.dashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aivoiceclassifier.domain.model.DashboardItemType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class DashboardViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Create a simple mock repository that returns empty flow
        val mockRepository = object : com.aivoiceclassifier.domain.repository.InterviewRepository {
            override suspend fun insertSession(session: com.aivoiceclassifier.domain.model.InterviewSession): Long = 1L
            override suspend fun updateSession(session: com.aivoiceclassifier.domain.model.InterviewSession) {}
            override suspend fun deleteSession(session: com.aivoiceclassifier.domain.model.InterviewSession) {}
            override suspend fun getSessionById(id: Long): com.aivoiceclassifier.domain.model.InterviewSession? = null
            override fun getAllSessions() = kotlinx.coroutines.flow.flowOf(emptyList<com.aivoiceclassifier.domain.model.InterviewSession>())
            override fun getSessionsByCompany(companyId: Long) = kotlinx.coroutines.flow.flowOf(emptyList<com.aivoiceclassifier.domain.model.InterviewSession>())
        }
        
        viewModel = DashboardViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have default items`() {
        val initialState = viewModel.uiState.value
        
        assertEquals(3, initialState.dashboardItems.size)
        assertEquals("Interview", initialState.selectedItem?.name)
        assertEquals(DashboardItemType.INTERVIEW, initialState.selectedItem?.type)
        
        // Check all default items are present
        val itemNames = initialState.dashboardItems.map { it.name }
        assertTrue(itemNames.contains("Interview"))
        assertTrue(itemNames.contains("Native Language Speaker"))
        assertTrue(itemNames.contains("Real Language Translator"))
        
        assertFalse(initialState.navigateToTranslator)
        assertNull(initialState.errorMessage)
    }

    @Test
    fun `onItemSelected should update selected item`() {
        // Given
        val translatorItem = viewModel.uiState.value.dashboardItems.find { 
            it.type == DashboardItemType.REAL_LANGUAGE_TRANSLATOR 
        }!!
        
        // When
        viewModel.onItemSelected(translatorItem)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals("Real Language Translator", state.selectedItem?.name)
        assertEquals(DashboardItemType.REAL_LANGUAGE_TRANSLATOR, state.selectedItem?.type)
    }

    @Test
    fun `addCustomItem should add new custom item`() {
        // When
        viewModel.addCustomItem("Custom Interview Type")
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(4, state.dashboardItems.size)
        
        val customItem = state.dashboardItems.find { it.name == "Custom Interview Type" }
        assertEquals("Custom Interview Type", customItem?.name)
        assertEquals(DashboardItemType.CUSTOM, customItem?.type)
        assertFalse(customItem?.isDefault ?: true)
        
        // Should be selected as current item
        assertEquals("Custom Interview Type", state.selectedItem?.name)
    }

    @Test
    fun `addCustomItem with blank name should not add item`() {
        // Given
        val initialCount = viewModel.uiState.value.dashboardItems.size
        
        // When
        viewModel.addCustomItem("   ")
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(initialCount, state.dashboardItems.size)
    }

    @Test
    fun `addCompany should add new company`() {
        // When
        viewModel.addCompany("Apple")
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.companies.size >= 3) // 2 default + 1 new
        
        val appleCompany = state.companies.find { it.name == "Apple" }
        assertEquals("Apple", appleCompany?.name)
    }

    @Test
    fun `addCompany with blank name should not add company`() {
        // Given
        val initialCount = viewModel.uiState.value.companies.size
        
        // When
        viewModel.addCompany("   ")
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(initialCount, state.companies.size)
    }

    @Test
    fun `navigateToTranslator should set navigation flag`() {
        // When
        viewModel.navigateToTranslator()
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.navigateToTranslator)
    }

    @Test
    fun `onNavigationHandled should clear navigation flag`() {
        // Given
        viewModel.navigateToTranslator()
        assertTrue(viewModel.uiState.value.navigateToTranslator)
        
        // When
        viewModel.onNavigationHandled()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.navigateToTranslator)
    }
} 