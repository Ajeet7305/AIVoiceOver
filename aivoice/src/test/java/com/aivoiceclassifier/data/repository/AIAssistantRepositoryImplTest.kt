package com.aivoiceclassifier.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aivoiceclassifier.data.remote.api.OpenAIService
import com.aivoiceclassifier.data.remote.dto.Message
import com.aivoiceclassifier.data.remote.dto.OpenAIRequest
import com.aivoiceclassifier.data.remote.dto.OpenAIResponse
import com.aivoiceclassifier.data.remote.dto.Choice
import com.aivoiceclassifier.data.remote.dto.Usage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AIAssistantRepositoryImplTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockOpenAIService: OpenAIService

    private lateinit var repository: AIAssistantRepositoryImpl

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = AIAssistantRepositoryImpl(mockOpenAIService)
    }

    @Test
    fun `getAIResponse should return success when API call succeeds`() = runTest {
        val question = "What are your strengths?"
        val context = "Interview context"
        val expectedResponse = "My key strengths include problem-solving and teamwork."
        
        val mockResponse = OpenAIResponse(
            id = "chatcmpl-123",
            objectType = "chat.completion",
            created = System.currentTimeMillis(),
            model = "gpt-3.5-turbo",
            choices = listOf(
                Choice(
                    index = 0,
                    message = Message(
                        role = "assistant",
                        content = expectedResponse
                    ),
                    finishReason = "stop"
                )
            ),
            usage = Usage(
                promptTokens = 10,
                completionTokens = 20,
                totalTokens = 30
            )
        )
        
        whenever(mockOpenAIService.getChatCompletion(any(), any(), any())).thenReturn(Response.success(mockResponse))

        val result = repository.getAIResponse(question, context)

        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
    }

    @Test
    fun `getAIResponse should return fallback when API call fails`() = runTest {
        val question = "Tell me about yourself"
        val context = ""
        
        whenever(mockOpenAIService.getChatCompletion(any(), any(), any())).thenReturn(
            Response.error(500, okhttp3.ResponseBody.create(null, "Server Error"))
        )

        val result = repository.getAIResponse(question, context)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.contains("dedicated professional") == true)
    }

    @Test
    fun `getAIResponse should return fallback when API throws exception`() = runTest {
        val question = "What are your strengths?"
        val context = ""
        val exception = RuntimeException("Network error")
        
        whenever(mockOpenAIService.getChatCompletion(any(), any(), any())).thenThrow(exception)

        val result = repository.getAIResponse(question, context)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.contains("problem-solving") == true)
    }

    @Test
    fun `getAIResponse should handle empty response choices`() = runTest {
        val question = "What are your strengths?"
        val context = ""
        
        val mockResponse = OpenAIResponse(
            id = "chatcmpl-123",
            objectType = "chat.completion",
            created = System.currentTimeMillis(),
            model = "gpt-3.5-turbo",
            choices = emptyList(),
            usage = Usage(
                promptTokens = 10,
                completionTokens = 0,
                totalTokens = 10
            )
        )
        
        whenever(mockOpenAIService.getChatCompletion(any(), any(), any())).thenReturn(Response.success(mockResponse))

        val result = repository.getAIResponse(question, context)

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertEquals("I'm sorry, I couldn't generate a response.", response)
    }

    @Test
    fun `getAIResponse should handle null response body`() = runTest {
        val question = "What are your strengths?"
        val context = ""
        
        whenever(mockOpenAIService.getChatCompletion(any(), any(), any())).thenReturn(Response.success(null))

        val result = repository.getAIResponse(question, context)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.contains("problem-solving") == true)
    }

    @Test
    fun `getAIResponse should generate appropriate fallback for different question types`() = runTest {
        val testCases = mapOf(
            "Tell me about yourself" to "dedicated professional",
            "What are your strengths?" to "problem-solving abilities",
            "What are your weaknesses?" to "overly thorough",
            "Why do you want to work at this company?" to "excellent reputation",
            "What is your experience?" to "valuable experience",
            "What are your goals?" to "career goals",
            "Describe a challenge" to "approach challenges",
            "How do you work in a team?" to "effective teamwork"
        )
        
        testCases.forEach { (question, expectedKeyword) ->
            whenever(mockOpenAIService.getChatCompletion(any(), any(), any())).thenThrow(RuntimeException("API Error"))
            
            val result = repository.getAIResponse(question, "")
            
            assertTrue(result.isSuccess)
            val response = result.getOrNull()
            assertTrue(
                response?.contains(expectedKeyword, ignoreCase = true) == true,
                "Question '$question' should contain '$expectedKeyword' but got: $response"
            )
        }
    }

    @Test
    fun `getAIResponse should handle context parameter correctly`() = runTest {
        val question = "What are your strengths?"
        val context = "This is for a software engineering position"
        val expectedResponse = "I have strong technical skills."
        
        val mockResponse = OpenAIResponse(
            id = "chatcmpl-123",
            objectType = "chat.completion",
            created = System.currentTimeMillis(),
            model = "gpt-3.5-turbo",
            choices = listOf(
                Choice(
                    index = 0,
                    message = Message(
                        role = "assistant",
                        content = expectedResponse
                    ),
                    finishReason = "stop"
                )
            ),
            usage = Usage(
                promptTokens = 15,
                completionTokens = 10,
                totalTokens = 25
            )
        )
        
        whenever(mockOpenAIService.getChatCompletion(any(), any(), any())).thenReturn(Response.success(mockResponse))

        val result = repository.getAIResponse(question, context)

        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
    }

    @Test
    fun `getAIResponse should handle empty question gracefully`() = runTest {
        val emptyQuestion = ""
        val context = ""
        
        whenever(mockOpenAIService.getChatCompletion(any(), any(), any())).thenThrow(RuntimeException("API Error"))

        val result = repository.getAIResponse(emptyQuestion, context)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isNotEmpty() == true)
    }
} 