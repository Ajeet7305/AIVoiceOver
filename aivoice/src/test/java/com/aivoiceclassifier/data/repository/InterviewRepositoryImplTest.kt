package com.aivoiceclassifier.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aivoiceclassifier.data.local.dao.InterviewSessionDao
import com.aivoiceclassifier.data.local.entity.InterviewSessionEntity
import com.aivoiceclassifier.domain.model.Conversation
import com.aivoiceclassifier.domain.model.InterviewSession
import com.aivoiceclassifier.domain.model.SpeakerType
import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class InterviewRepositoryImplTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockDao: InterviewSessionDao

    private lateinit var repository: InterviewRepositoryImpl
    private lateinit var gson: Gson

    private val testSession = InterviewSession(
        id = 1L,
        companyId = 100L,
        companyName = "Test Company",
        startTime = System.currentTimeMillis(),
        endTime = null,
        conversations = listOf(
            Conversation(
                id = "conv1",
                question = "Tell me about yourself",
                answer = "I am a software developer",
                speakerType = SpeakerType.HUMAN,
                correction = null
            )
        ),
        createdAt = System.currentTimeMillis()
    )

    private val testEntity = InterviewSessionEntity(
        id = 1L,
        companyId = 100L,
        companyName = "Test Company",
        startTime = testSession.startTime,
        endTime = null,
        transcript = """[{"id":"conv1","question":"Tell me about yourself","answer":"I am a software developer","speakerType":"HUMAN","correction":null,"timestamp":${System.currentTimeMillis()}}]""",
        createdAt = testSession.createdAt
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        gson = Gson()
        repository = InterviewRepositoryImpl(mockDao, gson)
    }

    @Test
    fun `insertSession should insert session and return id`() = runTest {
        whenever(mockDao.insertSession(org.mockito.kotlin.any())).thenReturn(1L)

        val result = repository.insertSession(testSession)

        assertEquals(1L, result)
        verify(mockDao).insertSession(org.mockito.kotlin.any())
    }

    @Test
    fun `updateSession should update session in dao`() = runTest {
        repository.updateSession(testSession)

        verify(mockDao).updateSession(org.mockito.kotlin.any())
    }

    @Test
    fun `deleteSession should delete session from dao`() = runTest {
        repository.deleteSession(testSession)

        verify(mockDao).deleteSession(org.mockito.kotlin.any())
    }

    @Test
    fun `getSessionById should return session when found`() = runTest {
        whenever(mockDao.getSessionById(1L)).thenReturn(testEntity)

        val result = repository.getSessionById(1L)

        assertNotNull(result)
        assertEquals(testSession.id, result.id)
        assertEquals(testSession.companyName, result.companyName)
    }

    @Test
    fun `getSessionById should return null when not found`() = runTest {
        whenever(mockDao.getSessionById(1L)).thenReturn(null)

        val result = repository.getSessionById(1L)

        assertNull(result)
    }

    @Test
    fun `getAllSessions should return flow of sessions`() = runTest {
        val entities = listOf(testEntity)
        whenever(mockDao.getAllSessions()).thenReturn(flowOf(entities))

        val flow = repository.getAllSessions()
        val result = mutableListOf<List<InterviewSession>>()
        flow.collect { result.add(it) }

        assertEquals(1, result[0].size)
        assertEquals(testSession.companyName, result[0][0].companyName)
    }

    @Test
    fun `getSessionsByCompany should return flow of sessions for company`() = runTest {
        val entities = listOf(testEntity)
        whenever(mockDao.getSessionsByCompany(100L)).thenReturn(flowOf(entities))

        val flow = repository.getSessionsByCompany(100L)
        val result = mutableListOf<List<InterviewSession>>()
        flow.collect { result.add(it) }

        assertEquals(1, result[0].size)
        assertEquals(testSession.companyId, result[0][0].companyId)
    }

    @Test
    fun `should handle empty conversations list`() = runTest {
        val entityWithoutConversations = testEntity.copy(transcript = "[]")
        
        whenever(mockDao.getSessionById(1L)).thenReturn(entityWithoutConversations)

        val result = repository.getSessionById(1L)

        assertNotNull(result)
        assertEquals(0, result.conversations.size)
    }

    @Test
    fun `should handle session with multiple conversations`() = runTest {
        val multipleConversations = listOf(
            Conversation(
                id = "conv1",
                question = "Tell me about yourself",
                answer = "I am a software developer",
                speakerType = SpeakerType.HUMAN
            ),
            Conversation(
                id = "conv2",
                question = "What are your strengths?",
                answer = "Problem solving and teamwork",
                speakerType = SpeakerType.AI,
                correction = "Could be more specific"
            )
        )
        
        val entityJson = gson.toJson(multipleConversations)
        val entityWithMultiple = testEntity.copy(transcript = entityJson)
        
        whenever(mockDao.getSessionById(1L)).thenReturn(entityWithMultiple)

        val result = repository.getSessionById(1L)

        assertNotNull(result)
        assertEquals(2, result.conversations.size)
        assertEquals("conv1", result.conversations[0].id)
        assertEquals("conv2", result.conversations[1].id)
        assertEquals("Could be more specific", result.conversations[1].correction)
    }

    @Test
    fun `should handle different speaker types correctly`() = runTest {
        val conversationWithMachine = Conversation(
            id = "conv1",
            question = "System notification",
            answer = "Recording started",
            speakerType = SpeakerType.MACHINE
        )
        
        val entityJson = gson.toJson(listOf(conversationWithMachine))
        val entityWithMachine = testEntity.copy(transcript = entityJson)
        
        whenever(mockDao.getSessionById(1L)).thenReturn(entityWithMachine)

        val result = repository.getSessionById(1L)

        assertNotNull(result)
        assertEquals(SpeakerType.MACHINE, result.conversations[0].speakerType)
    }

    @Test
    fun `should handle session with end time`() = runTest {
        val endTime = System.currentTimeMillis()
        val completedEntity = testEntity.copy(endTime = endTime)
        
        whenever(mockDao.getSessionById(1L)).thenReturn(completedEntity)

        val result = repository.getSessionById(1L)

        assertNotNull(result)
        assertEquals(endTime, result.endTime)
    }

    @Test
    fun `getAllSessions should handle empty list`() = runTest {
        whenever(mockDao.getAllSessions()).thenReturn(flowOf(emptyList()))

        val flow = repository.getAllSessions()
        val result = mutableListOf<List<InterviewSession>>()
        flow.collect { result.add(it) }

        assertEquals(0, result[0].size)
    }

    @Test
    fun `getSessionsByCompany should handle empty list`() = runTest {
        whenever(mockDao.getSessionsByCompany(999L)).thenReturn(flowOf(emptyList()))

        val flow = repository.getSessionsByCompany(999L)
        val result = mutableListOf<List<InterviewSession>>()
        flow.collect { result.add(it) }

        assertEquals(0, result[0].size)
    }

    @Test
    fun `should handle malformed JSON gracefully`() = runTest {
        val entityWithBadJson = testEntity.copy(transcript = "invalid json")
        
        whenever(mockDao.getSessionById(1L)).thenReturn(entityWithBadJson)

        val result = repository.getSessionById(1L)

        assertNotNull(result)
        assertEquals(0, result.conversations.size) // Should default to empty list
    }
} 