package com.aivoiceclassifier.presentation.session

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aivoiceclassifier.domain.model.Conversation
import com.aivoiceclassifier.domain.model.SpeakerType
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun InterviewSessionScreen(
    companyId: Long,
    companyName: String,
    onNavigateBack: () -> Unit,
    viewModel: InterviewSessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val clipboardManager = LocalClipboardManager.current
    var correctionText by remember { mutableStateOf("") }
    var showCorrectionDialog by remember { mutableStateOf(false) }
    var selectedConversationId by remember { mutableStateOf("") }
    
    // Stable initialization state - use derivedStateOf to prevent unnecessary recompositions
    val isInitialized = remember { mutableStateOf(false) }
    val shouldInitialize by remember {
        derivedStateOf {
            !isInitialized.value && companyId > 0 && companyName.isNotBlank()
        }
    }
    
    // Audio permission
    val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    
    // Initialize session only once with stable key and prevent multiple triggers
    LaunchedEffect(key1 = "session_init") {
        if (audioPermissionState.status.isGranted && shouldInitialize) {
            viewModel.startSession(companyId, companyName)
            isInitialized.value = true
        }
    }
    
    // Auto-scroll with stable conversation count key and debouncing
    val conversationCount = uiState.conversations.size
    LaunchedEffect(key1 = "auto_scroll") {
        if (conversationCount > 0) {
            // Add small delay to prevent rapid scrolling
            delay(100)
            try {
                listState.animateScrollToItem(maxOf(0, conversationCount - 1))
            } catch (e: Exception) {
                // Handle edge case gracefully
            }
        }
    }
    
    // Handle back navigation with proper cleanup
    DisposableEffect(Unit) {
        onDispose {
            if (uiState.isSessionActive) {
                viewModel.stopSession()
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Modern Top App Bar
        TopAppBar(
            title = { 
                Column {
                    Text(
                        "Interview Session",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = companyName,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        viewModel.stopSession()
                        onNavigateBack()
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack, 
                        contentDescription = "Back",
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            actions = {
                // Voice Identification Button
                IconButton(
                    onClick = { viewModel.identifyVoice() },
                    enabled = uiState.isSessionActive && !uiState.isIdentifyingVoice,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Person, 
                        contentDescription = "Identify Voice",
                        tint = if (uiState.isIdentifyingVoice) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Export Button
                Button(
                    onClick = {
                        val transcript = viewModel.exportTranscript()
                        clipboardManager.setText(AnnotatedString(transcript))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Share, 
                        contentDescription = "Export Transcript",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export", fontWeight = FontWeight.Medium)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
        
        if (!audioPermissionState.status.isGranted) {
            // Enhanced Permission Request UI
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Microphone Permission Required",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "This app needs microphone access to classify audio and provide interview assistance.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { audioPermissionState.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Grant Permission", fontWeight = FontWeight.Medium)
                    }
                }
            }
        } else {
            // Main Content
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Enhanced Voice Identification Card
                AnimatedVisibility(
                    visible = uiState.isIdentifyingVoice || uiState.identifiedVoiceName != null,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (uiState.isIdentifyingVoice) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Identifying Voice...",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Please speak clearly for voice recognition",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                            } else if (uiState.identifiedVoiceName != null) {
                                val voiceIcon = when (uiState.identifiedVoiceName) {
                                    "Human" -> "🎤"
                                    "Machine" -> "🔊"
                                    "AI" -> "🤖"
                                    else -> "🎵"
                                }
                                Text(
                                    text = "$voiceIcon Voice Identified",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${uiState.identifiedVoiceName!!} Voice",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
                
                // Enhanced Status Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.isSessionActive) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // Status Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Animated Status Icon
                                Card(
                                    modifier = Modifier.size(48.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (uiState.isSessionActive) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.outline
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (uiState.isSessionActive) Icons.Default.Mic else Icons.Default.MicOff,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column {
                                    Text(
                                        text = if (uiState.isSessionActive) "Session Active" else "Session Stopped",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = if (uiState.isSessionActive) 
                                            MaterialTheme.colorScheme.onPrimaryContainer 
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    when {
                                        uiState.isProcessingAI -> {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(12.dp),
                                                    strokeWidth = 1.5.dp,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "AI is processing...",
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                        uiState.currentQuestion.isNotEmpty() -> {
                                            Text(
                                                text = "Listening: ${uiState.currentQuestion}",
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                        else -> {
                                            Text(
                                                text = if (uiState.isSessionActive) "Ready to listen" else "Tap Start to begin",
                                                fontSize = 14.sp,
                                                color = if (uiState.isSessionActive) 
                                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                                else 
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Control Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Voice Identify Button
                            if (uiState.isSessionActive) {
                                Button(
                                    onClick = { viewModel.identifyVoice() },
                                    enabled = !uiState.isIdentifyingVoice,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (uiState.isIdentifyingVoice) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ID Voice", fontWeight = FontWeight.Medium)
                                }
                            }
                            
                            // Start/Stop Button
                            Button(
                                onClick = {
                                    if (uiState.isSessionActive) {
                                        viewModel.stopSession()
                                    } else {
                                        viewModel.startSession(companyId, companyName)
                                    }
                                },
                                modifier = if (uiState.isSessionActive) Modifier.weight(1f) else Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (uiState.isSessionActive) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.primary,
                                    contentColor = if (uiState.isSessionActive) 
                                        MaterialTheme.colorScheme.onError 
                                    else 
                                        MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (uiState.isSessionActive) Icons.Default.Stop else Icons.Default.Mic,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (uiState.isSessionActive) "Stop Session" else "Start Session",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                // Enhanced Conversations List
                if (uiState.conversations.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Text(
                                text = "No Conversations Yet",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "Start the session to begin recording conversations",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "💡 How it works:",
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "🎤 Human voice → Detected as questions\n🔊 AI/Speaker voice → Triggers responses\n👤 Use 'ID Voice' to identify speakers",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.conversations,
                            key = { conversation -> conversation.id }
                        ) { conversation ->
                            ModernConversationCard(
                                conversation = conversation,
                                onAddCorrection = { conversationId ->
                                    selectedConversationId = conversationId
                                    showCorrectionDialog = true
                                }
                            )
                        }
                        
                        // Add some bottom padding
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
    
    // Enhanced Correction Dialog
    if (showCorrectionDialog) {
        AlertDialog(
            onDismissRequest = { showCorrectionDialog = false },
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Add Correction",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Add a correction or suggestion for this conversation:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = correctionText,
                        onValueChange = { correctionText = it },
                        label = { Text("Correction or suggestion") },
                        placeholder = { Text("Enter your feedback here...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addCorrection(selectedConversationId, correctionText)
                        correctionText = ""
                        showCorrectionDialog = false
                    },
                    enabled = correctionText.isNotBlank(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Add Correction")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showCorrectionDialog = false
                    correctionText = ""
                }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    // Error handling
    uiState.errorMessage?.let { error ->
        LaunchedEffect(key1 = "error_handling", key2 = error) {
            viewModel.clearError()
        }
    }
}

@Composable
private fun ModernConversationCard(
    conversation: Conversation,
    onAddCorrection: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Enhanced Header with speaker type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Speaker type badge
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when (conversation.speakerType) {
                                SpeakerType.HUMAN -> MaterialTheme.colorScheme.primaryContainer
                                SpeakerType.AI -> MaterialTheme.colorScheme.secondaryContainer
                                SpeakerType.MACHINE -> MaterialTheme.colorScheme.tertiaryContainer
                                SpeakerType.UNKNOWN -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = when (conversation.speakerType) {
                                SpeakerType.HUMAN -> "🎤 Human"
                                SpeakerType.AI -> "🔊 AI Speaker"
                                SpeakerType.MACHINE -> "💻 Machine"
                                SpeakerType.UNKNOWN -> "❓ Unknown"
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = when (conversation.speakerType) {
                                SpeakerType.HUMAN -> MaterialTheme.colorScheme.onPrimaryContainer
                                SpeakerType.AI -> MaterialTheme.colorScheme.onSecondaryContainer
                                SpeakerType.MACHINE -> MaterialTheme.colorScheme.onTertiaryContainer
                                SpeakerType.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
                
                IconButton(
                    onClick = { onAddCorrection(conversation.id) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Add correction",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Question Section
            Column {
                Text(
                    text = "Question",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = conversation.question,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Answer Section
            Column {
                Text(
                    text = "Answer",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = conversation.answer,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 21.sp
                    )
                }
            }
            
            // Correction Section (if exists)
            conversation.correction?.let { correction ->
                Spacer(modifier = Modifier.height(16.dp))
                Column {
                    Text(
                        text = "Correction",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.tertiary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "💡",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = correction,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                lineHeight = 20.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
} 