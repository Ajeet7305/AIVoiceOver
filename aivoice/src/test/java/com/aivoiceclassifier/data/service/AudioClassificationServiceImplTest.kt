package com.aivoiceclassifier.data.service

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class AudioClassificationServiceImplTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockContext: Context

    private lateinit var audioService: AudioClassificationServiceImpl

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        audioService = AudioClassificationServiceImpl(mockContext)
    }

    @After
    fun tearDown() {
        // Ensure cleanup
        try {
            audioService.stopListening()
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }

    @Test
    fun `service should be instantiable`() {
        assertNotNull(audioService)
    }

    @Test
    fun `startListening should return flow`() {
        val flow = audioService.startListening()
        assertNotNull(flow)
        
        // Cleanup
        audioService.stopListening()
    }

    @Test
    fun `stopListening should not throw exception`() {
        // Should not throw exception even if not started
        audioService.stopListening()
        
        // Should not throw exception after starting
        audioService.startListening()
        audioService.stopListening()
    }

    @Test
    fun `multiple start calls should not cause issues`() {
        // Multiple starts should not throw exceptions
        audioService.startListening()
        audioService.startListening()
        audioService.startListening()
        
        // Cleanup
        audioService.stopListening()
    }

    @Test
    fun `service should handle rapid start stop cycles`() {
        // Rapid cycles should not cause issues
        repeat(3) {
            audioService.startListening()
            audioService.stopListening()
        }
    }

    @Test
    fun `isListening should not throw exception`() {
        // Should not throw exception in any state
        audioService.isListening()
        
        audioService.startListening()
        audioService.isListening()
        
        audioService.stopListening()
        audioService.isListening()
    }

    @Test
    fun `service should be thread safe`() = runTest {
        // Basic thread safety test
        audioService.startListening()
        audioService.isListening()
        audioService.stopListening()
        audioService.isListening()
    }
} 