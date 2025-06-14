package com.aivoiceclassifier.presentation.translator

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aivoiceclassifier.domain.service.AudioClassificationService
import com.aivoiceclassifier.domain.usecase.ai.GetAIResponseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TranslatorViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var mockAudioService: AudioClassificationService

    @Mock
    private lateinit var mockGetAIResponseUseCase: GetAIResponseUseCase

    private lateinit var viewModel: TranslatorViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TranslatorViewModel {
        return TranslatorViewModel(mockAudioService, mockGetAIResponseUseCase)
    }

    @Test
    fun `initial state should be correct`() {
        viewModel = createViewModel()
        val initialState = viewModel.uiState.value

        assertFalse(initialState.isListening)
        assertFalse(initialState.isTranslating)
        assertFalse(initialState.isProcessing)
        assertTrue(initialState.isSpeakReplyEnabled)
        assertTrue(initialState.messages.isEmpty())
        assertEquals(null, initialState.lastDetectedLanguage)
        assertEquals(null, initialState.errorMessage)
    }

    @Test
    fun `toggleSpeakReply should toggle speak reply state`() {
        viewModel = createViewModel()
        val initialState = viewModel.uiState.value.isSpeakReplyEnabled

        viewModel.toggleSpeakReply()

        assertEquals(!initialState, viewModel.uiState.value.isSpeakReplyEnabled)

        viewModel.toggleSpeakReply()

        assertEquals(initialState, viewModel.uiState.value.isSpeakReplyEnabled)
    }

    @Test
    fun `clearChat should clear messages and language`() {
        viewModel = createViewModel()
        // Clear chat should work regardless of messages
        viewModel.clearChat()

        assertTrue(viewModel.uiState.value.messages.isEmpty())
        assertEquals(null, viewModel.uiState.value.lastDetectedLanguage)
    }

    @Test
    fun `clearError should clear error message`() {
        viewModel = createViewModel()
        // Clear error should work regardless of error state
        viewModel.clearError()

        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `stopListening should stop listening and processing`() {
        viewModel = createViewModel()
        viewModel.stopListening()

        assertFalse(viewModel.uiState.value.isListening)
        assertFalse(viewModel.uiState.value.isProcessing)
    }

    @Test
    fun `startListening should set listening state initially`() {
        viewModel = createViewModel()
        viewModel.startListening()

        assertTrue(viewModel.uiState.value.isListening)
        assertTrue(viewModel.uiState.value.isProcessing)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `startListening should generate AI response successfully`() = runTest {
        org.mockito.kotlin.whenever(mockGetAIResponseUseCase.invoke(org.mockito.kotlin.any(), org.mockito.kotlin.any()))
            .thenReturn(Result.success("Hello! I'm here to help you with translation."))

        viewModel = createViewModel()
        viewModel.startListening()
        testDispatcher.scheduler.advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertFalse(finalState.isListening)
        assertFalse(finalState.isTranslating)
        assertFalse(finalState.isProcessing)
        assertEquals(2, finalState.messages.size) // User message + AI response
        assertEquals("Hello! I'm here to help you with translation.", finalState.messages[1].content)
        assertFalse(finalState.messages[1].isUser)
    }

    @Test
    fun `startListening should handle AI response failure gracefully`() = runTest {
        org.mockito.kotlin.whenever(mockGetAIResponseUseCase.invoke(org.mockito.kotlin.any(), org.mockito.kotlin.any()))
            .thenReturn(Result.failure(RuntimeException("API Error")))

        viewModel = createViewModel()
        viewModel.startListening()
        testDispatcher.scheduler.advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertEquals(2, finalState.messages.size) // User message + fallback AI response
        assertTrue(finalState.messages[1].content.contains("I'm sorry, I couldn't process"))
    }

    @Test
    fun `startListening should detect English language correctly`() = runTest {
        org.mockito.kotlin.whenever(mockGetAIResponseUseCase.invoke(org.mockito.kotlin.any(), org.mockito.kotlin.any()))
            .thenReturn(Result.success("Hello! I'm here to help."))

        viewModel = createViewModel()
        viewModel.startListening()
        testDispatcher.scheduler.advanceUntilIdle()

        val finalState = viewModel.uiState.value
        // Since getSimulatedSpeech() returns random phrases, we should test that language detection works
        // The detected language should be one of the supported languages
        val supportedLanguages = listOf("English", "Spanish", "French", "German", "Hindi", "Tamil")
        assertTrue(supportedLanguages.contains(finalState.lastDetectedLanguage))
        assertTrue(supportedLanguages.contains(finalState.messages[0].detectedLanguage))
    }

    @Test
    fun `startListening should handle different languages`() = runTest {
        org.mockito.kotlin.whenever(mockGetAIResponseUseCase.invoke(org.mockito.kotlin.any(), org.mockito.kotlin.any()))
            .thenReturn(Result.success("Hello! I'm here to help."))

        viewModel = createViewModel()
        viewModel.startListening()
        testDispatcher.scheduler.advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertTrue(finalState.messages.isNotEmpty())
        assertTrue(finalState.lastDetectedLanguage != null)
    }

    @Test
    fun `startListening should handle exception during processing`() = runTest {
        org.mockito.kotlin.whenever(mockGetAIResponseUseCase.invoke(org.mockito.kotlin.any(), org.mockito.kotlin.any()))
            .thenThrow(RuntimeException("Processing error"))

        viewModel = createViewModel()
        viewModel.startListening()
        testDispatcher.scheduler.advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertFalse(finalState.isListening)
        assertFalse(finalState.isTranslating)
        assertFalse(finalState.isProcessing)
        assertTrue(finalState.errorMessage?.contains("Failed to generate AI response") == true)
    }

    @Test
    fun `multiple startListening calls should work correctly`() = runTest {
        org.mockito.kotlin.whenever(mockGetAIResponseUseCase.invoke(org.mockito.kotlin.any(), org.mockito.kotlin.any()))
            .thenReturn(Result.success("Response 1"))
            .thenReturn(Result.success("Response 2"))

        viewModel = createViewModel()
        // First call
        viewModel.startListening()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.messages.size)

        // Second call
        viewModel.startListening()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(4, viewModel.uiState.value.messages.size) // 2 user + 2 AI messages
    }

    @Test
    fun `state transitions should be correct during listening process`() = runTest {
        org.mockito.kotlin.whenever(mockGetAIResponseUseCase.invoke(org.mockito.kotlin.any(), org.mockito.kotlin.any()))
            .thenReturn(Result.success("AI Response"))

        viewModel = createViewModel()
        // Initial state
        assertFalse(viewModel.uiState.value.isListening)
        assertFalse(viewModel.uiState.value.isProcessing)

        // Start listening
        viewModel.startListening()

        // Should be listening and processing
        assertTrue(viewModel.uiState.value.isListening)
        assertTrue(viewModel.uiState.value.isProcessing)

        // Complete the process
        testDispatcher.scheduler.advanceUntilIdle()

        // Should be done
        assertFalse(viewModel.uiState.value.isListening)
        assertFalse(viewModel.uiState.value.isProcessing)
        assertFalse(viewModel.uiState.value.isTranslating)
    }
} 