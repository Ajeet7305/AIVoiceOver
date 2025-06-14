package com.aivoiceclassifier

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.aivoiceclassifier.presentation.navigation.AppNavigation
import com.aivoiceclassifier.ui.theme.AiVoiceClassifierTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainContent()
        }
    }
}

@Composable
private fun MainContent() {
    val systemInDarkTheme = isSystemInDarkTheme()
    var isDarkTheme by rememberSaveable { mutableStateOf(systemInDarkTheme) }
    
    AiVoiceClassifierTheme(darkTheme = isDarkTheme) {
        AppNavigation(
            onToggleTheme = { isDarkTheme = !isDarkTheme }
        )
    }
} 