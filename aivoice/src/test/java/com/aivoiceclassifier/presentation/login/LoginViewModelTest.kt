package com.aivoiceclassifier.presentation.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
class LoginViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be correct`() {
        val initialState = viewModel.uiState.value
        
        assertEquals("", initialState.username)
        assertEquals("", initialState.password)
        assertFalse(initialState.isAdminMode)
        assertFalse(initialState.isLoading)
        assertFalse(initialState.isLoginSuccessful)
        assertNull(initialState.errorMessage)
    }

    @Test
    fun `updateUsername should update username and clear error`() {
        // When
        viewModel.updateUsername("testuser")
        
        // Then
        val state = viewModel.uiState.value
        assertEquals("testuser", state.username)
        assertNull(state.errorMessage)
    }

    @Test
    fun `updatePassword should update password and clear error`() {
        // When
        viewModel.updatePassword("testpass")
        
        // Then
        val state = viewModel.uiState.value
        assertEquals("testpass", state.password)
        assertNull(state.errorMessage)
    }

    @Test
    fun `login with empty username should show error`() {
        // Given
        viewModel.updateUsername("")
        viewModel.updatePassword("password")
        
        // When
        viewModel.login()
        
        // Then
        val state = viewModel.uiState.value
        assertEquals("Username is required", state.errorMessage)
        assertFalse(state.isLoading)
        assertFalse(state.isLoginSuccessful)
    }

    @Test
    fun `login with empty password should show error`() {
        // Given
        viewModel.updateUsername("user")
        viewModel.updatePassword("")
        
        // When
        viewModel.login()
        
        // Then
        val state = viewModel.uiState.value
        assertEquals("Password is required", state.errorMessage)
        assertFalse(state.isLoading)
        assertFalse(state.isLoginSuccessful)
    }

    @Test
    fun `login with blank username should show error`() {
        // Given
        viewModel.updateUsername("   ")
        viewModel.updatePassword("password")
        
        // When
        viewModel.login()
        
        // Then
        val state = viewModel.uiState.value
        assertEquals("Username is required", state.errorMessage)
        assertFalse(state.isLoading)
        assertFalse(state.isLoginSuccessful)
    }

    @Test
    fun `login with blank password should show error`() {
        // Given
        viewModel.updateUsername("user")
        viewModel.updatePassword("   ")
        
        // When
        viewModel.login()
        
        // Then
        val state = viewModel.uiState.value
        assertEquals("Password is required", state.errorMessage)
        assertFalse(state.isLoading)
        assertFalse(state.isLoginSuccessful)
    }

    @Test
    fun `clearError should clear error message`() {
        // Given - set an error
        viewModel.updateUsername("")
        viewModel.login()
        assertEquals("Username is required", viewModel.uiState.value.errorMessage)
        
        // When
        viewModel.clearError()
        
        // Then
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `resetLoginState should reset login success and error`() {
        // Given - set some state
        viewModel.updateUsername("admin")
        viewModel.updatePassword("admin")
        
        // When
        viewModel.resetLoginState()
        
        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoginSuccessful)
        assertNull(state.errorMessage)
        // Other fields should remain unchanged
        assertEquals("admin", state.username)
        assertEquals("admin", state.password)
    }

    @Test
    fun `updateUsername should clear existing error`() {
        // Given - set an error first
        viewModel.updateUsername("")
        viewModel.login()
        assertEquals("Username is required", viewModel.uiState.value.errorMessage)
        
        // When - update username
        viewModel.updateUsername("newuser")
        
        // Then - error should be cleared
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals("newuser", viewModel.uiState.value.username)
    }

    @Test
    fun `updatePassword should clear existing error`() {
        // Given - set an error first
        viewModel.updateUsername("user")
        viewModel.updatePassword("")
        viewModel.login()
        assertEquals("Password is required", viewModel.uiState.value.errorMessage)
        
        // When - update password
        viewModel.updatePassword("newpass")
        
        // Then - error should be cleared
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals("newpass", viewModel.uiState.value.password)
    }

    @Test
    fun `multiple login calls with empty username should show same error`() {
        // Given
        viewModel.updateUsername("")
        viewModel.updatePassword("password")
        
        // When - call login multiple times
        viewModel.login()
        viewModel.login()
        viewModel.login()
        
        // Then - should show same error
        val state = viewModel.uiState.value
        assertEquals("Username is required", state.errorMessage)
        assertFalse(state.isLoading)
        assertFalse(state.isLoginSuccessful)
    }

    @Test
    fun `multiple login calls with empty password should show same error`() {
        // Given
        viewModel.updateUsername("user")
        viewModel.updatePassword("")
        
        // When - call login multiple times
        viewModel.login()
        viewModel.login()
        viewModel.login()
        
        // Then - should show same error
        val state = viewModel.uiState.value
        assertEquals("Password is required", state.errorMessage)
        assertFalse(state.isLoading)
        assertFalse(state.isLoginSuccessful)
    }

    @Test
    fun `login validation should handle whitespace correctly`() {
        // Test various whitespace scenarios
        
        // Only spaces in username
        viewModel.updateUsername("   ")
        viewModel.updatePassword("password")
        viewModel.login()
        assertEquals("Username is required", viewModel.uiState.value.errorMessage)
        
        // Only spaces in password
        viewModel.updateUsername("user")
        viewModel.updatePassword("   ")
        viewModel.login()
        assertEquals("Password is required", viewModel.uiState.value.errorMessage)
        
        // Both empty
        viewModel.updateUsername("")
        viewModel.updatePassword("")
        viewModel.login()
        assertEquals("Username is required", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `state should maintain consistency after multiple operations`() {
        // Test state consistency through multiple operations
        
        // Initial state
        assertEquals("", viewModel.uiState.value.username)
        assertEquals("", viewModel.uiState.value.password)
        assertFalse(viewModel.uiState.value.isAdminMode)
        
        // Update username
        viewModel.updateUsername("testuser")
        assertEquals("testuser", viewModel.uiState.value.username)
        assertEquals("", viewModel.uiState.value.password)
        
        // Update password
        viewModel.updatePassword("testpass")
        assertEquals("testuser", viewModel.uiState.value.username)
        assertEquals("testpass", viewModel.uiState.value.password)
        
        // Clear error (should not affect other fields)
        viewModel.clearError()
        assertEquals("testuser", viewModel.uiState.value.username)
        assertEquals("testpass", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.errorMessage)
        
        // Reset login state (should not affect username/password)
        viewModel.resetLoginState()
        assertEquals("testuser", viewModel.uiState.value.username)
        assertEquals("testpass", viewModel.uiState.value.password)
        assertFalse(viewModel.uiState.value.isLoginSuccessful)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `error messages should be specific and helpful`() {
        // Test specific error messages
        
        // Empty username
        viewModel.updateUsername("")
        viewModel.updatePassword("password")
        viewModel.login()
        assertEquals("Username is required", viewModel.uiState.value.errorMessage)
        
        // Empty password
        viewModel.updateUsername("user")
        viewModel.updatePassword("")
        viewModel.login()
        assertEquals("Password is required", viewModel.uiState.value.errorMessage)
        
        // Whitespace username
        viewModel.updateUsername("   ")
        viewModel.updatePassword("password")
        viewModel.login()
        assertEquals("Username is required", viewModel.uiState.value.errorMessage)
        
        // Whitespace password
        viewModel.updateUsername("user")
        viewModel.updatePassword("   ")
        viewModel.login()
        assertEquals("Password is required", viewModel.uiState.value.errorMessage)
    }
} 