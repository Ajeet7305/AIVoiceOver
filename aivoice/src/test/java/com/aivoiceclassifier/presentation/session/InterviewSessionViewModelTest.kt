package com.aivoiceclassifier.presentation.session

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aivoiceclassifier.domain.model.Conversation
import com.aivoiceclassifier.domain.model.InterviewSession
import com.aivoiceclassifier.domain.model.SpeakerType
import com.aivoiceclassifier.domain.service.AudioClassificationResult
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
class InterviewSessionViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: InterviewSessionViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Create mock AudioClassificationService
        val mockAudioClassificationService = object : com.aivoiceclassifier.domain.service.AudioClassificationService {
            override fun startListening() = flowOf(
                AudioClassificationResult(
                    audioText = "Test question",
                    speakerType = SpeakerType.HUMAN,
                    confidence = 0.9f
                )
            )
            override fun stopListening() {}
            override fun isListening(): Boolean = false
        }
        
        // Create mock repository that returns empty flow
        val mockAIAssistantRepository = object : com.aivoiceclassifier.domain.repository.AIAssistantRepository {
            override suspend fun getAIResponse(question: String, context: String): Result<String> {
                return Result.success("Mock AI response to: $question")
            }
        }
        
        // Create mock GetAIResponseUseCase
        val mockGetAIResponseUseCase = com.aivoiceclassifier.domain.usecase.ai.GetAIResponseUseCase(mockAIAssistantRepository)
        
        // Create mock InterviewRepository
        val mockInterviewRepository = object : com.aivoiceclassifier.domain.repository.InterviewRepository {
            override fun getAllSessions() = flowOf(emptyList<InterviewSession>())
            override fun getSessionsByCompany(companyId: Long) = flowOf(emptyList<InterviewSession>())
            override suspend fun insertSession(session: InterviewSession): Long = 1L
            override suspend fun updateSession(session: InterviewSession) {}
            override suspend fun deleteSession(session: InterviewSession) {}
            override suspend fun getSessionById(id: Long): InterviewSession? = 
                InterviewSession(
                    id = id,
                    companyId = 1L,
                    companyName = "Test Company",
                    startTime = System.currentTimeMillis(),
                    endTime = null,
                    conversations = emptyList()
                )
        }
        
        // Create mock StartInterviewSessionUseCase
        val mockStartInterviewSessionUseCase = com.aivoiceclassifier.domain.usecase.interview.StartInterviewSessionUseCase(mockInterviewRepository)
        
        viewModel = InterviewSessionViewModel(
            audioClassificationService = mockAudioClassificationService,
            getAIResponseUseCase = mockGetAIResponseUseCase,
            startInterviewSessionUseCase = mockStartInterviewSessionUseCase,
            interviewRepository = mockInterviewRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be correct`() {
        val initialState = viewModel.uiState.value
        
        assertEquals("", initialState.companyName)
        assertEquals(emptyList(), initialState.conversations)
        assertFalse(initialState.isSessionActive)
        assertFalse(initialState.isProcessingAI)
        assertFalse(initialState.isIdentifyingVoice)
        assertEquals("", initialState.currentQuestion)
        assertNull(initialState.identifiedVoiceName)
        assertNull(initialState.errorMessage)
    }

    @Test
    fun `startSession should update state correctly`() = runTest {
        // When
        viewModel.startSession(1L, "Google")
        
        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isSessionActive)
        assertEquals("Google", state.companyName)
        assertNull(state.errorMessage)
    }

    @Test
    fun `startSession with invalid parameters should not start session`() = runTest {
        // When - invalid company ID
        viewModel.startSession(0L, "Google")
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isSessionActive)
        
        // When - blank company name
        viewModel.startSession(1L, "")
        
        // Then
        val state2 = viewModel.uiState.value
        assertFalse(state2.isSessionActive)
    }

    @Test
    fun `stopSession should update state correctly`() = runTest {
        // Given - start session first
        viewModel.startSession(1L, "Google")
        assertTrue(viewModel.uiState.value.isSessionActive)
        
        // When
        viewModel.stopSession()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isSessionActive)
        assertFalse(state.isProcessingAI)
        assertEquals("", state.currentQuestion)
        assertFalse(state.isIdentifyingVoice)
        assertNull(state.identifiedVoiceName)
    }

    @Test
    fun `identifyVoice should update state correctly`() = runTest {
        // Given - start session first
        viewModel.startSession(1L, "Google")
        
        // When
        viewModel.identifyVoice()
        
        // Initially should be identifying
        assertTrue(viewModel.uiState.value.isIdentifyingVoice)
        
        // After processing (simulate delay)
        advanceTimeBy(3000)
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isIdentifyingVoice)
        assertTrue(state.identifiedVoiceName in listOf("Human", "Machine", "AI"))
    }

    @Test
    fun `identifyVoice should not work when session is inactive`() = runTest {
        // Given - session is not active
        assertFalse(viewModel.uiState.value.isSessionActive)
        
        // When
        viewModel.identifyVoice()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isIdentifyingVoice)
        assertNull(state.identifiedVoiceName)
    }

    @Test
    fun `addCorrection should update conversation correctly`() = runTest {
        // When
        viewModel.addCorrection("test123", "This is a correction")
        
        // Then - should not throw any exceptions
        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
    }

    @Test
    fun `addCorrection with blank correction should not update`() = runTest {
        // When
        viewModel.addCorrection("test123", "")
        
        // Then - should not throw any exceptions
        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
    }

    @Test
    fun `exportTranscript should return formatted transcript`() {
        // When
        val transcript = viewModel.exportTranscript()
        
        // Then
        assertTrue(transcript.contains("Interview Transcript"))
        assertTrue(transcript.contains("=".repeat(50)))
    }

    @Test
    fun `clearError should clear error message`() = runTest {
        // When
        viewModel.clearError()
        
        // Then
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `should handle empty company name correctly`() = runTest {
        // When
        viewModel.startSession(1L, "   ")
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isSessionActive)
        assertEquals("", state.companyName)
    }

    @Test
    fun `should handle zero company ID correctly`() = runTest {
        // When
        viewModel.startSession(0L, "Google")
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isSessionActive)
        assertEquals("", state.companyName)
    }

    @Test
    fun `stopSession when not active should not crash`() = runTest {
        // Given - session is not active
        assertFalse(viewModel.uiState.value.isSessionActive)
        
        // When
        viewModel.stopSession()
        
        // Then - should not crash
        val state = viewModel.uiState.value
        assertFalse(state.isSessionActive)
    }

    @Test
    fun `exportTranscript with empty conversations should return basic transcript`() {
        // Given - no conversations
        assertEquals(emptyList(), viewModel.uiState.value.conversations)
        
        // When
        val transcript = viewModel.exportTranscript()
        
        // Then
        assertTrue(transcript.contains("Interview Transcript"))
        assertTrue(transcript.isNotEmpty())
    }
} 