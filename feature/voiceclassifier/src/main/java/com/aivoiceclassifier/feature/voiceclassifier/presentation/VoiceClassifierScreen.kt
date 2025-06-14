package com.aivoiceclassifier.feature.voiceclassifier.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.aivoiceclassifier.domain.audio.model.AudioClassification
import com.aivoiceclassifier.domain.audio.model.VoiceType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoiceClassifierScreen(
    viewModel: VoiceClassifierViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val classificationHistory by viewModel.classificationHistory.collectAsStateWithLifecycle()
    
    val microphonePermissionState = rememberPermissionState(
        android.Manifest.permission.RECORD_AUDIO
    )
    
    LaunchedEffect(Unit) {
        if (!microphonePermissionState.status.isGranted) {
            microphonePermissionState.launchPermissionRequest()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!microphonePermissionState.status.isGranted) {
            PermissionRequiredCard(
                onRequestPermission = { microphonePermissionState.launchPermissionRequest() }
            )
        } else {
            ClassificationSection(
                uiState = uiState,
                onStartListening = viewModel::startListening,
                onStopListening = viewModel::stopListening,
                onClearError = viewModel::clearError
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            StatisticsSection(statistics = uiState.statistics)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            HistorySection(
                history = classificationHistory,
                selectedFilter = uiState.selectedFilter,
                onFilterChange = viewModel::filterHistoryByType,
                onDeleteClassification = viewModel::deleteClassification,
                onClearOldClassifications = viewModel::clearOldClassifications
            )
        }
    }
}

@Composable
private fun PermissionRequiredCard(
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Microphone Permission Required",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "This app needs microphone access to classify voice audio in real-time.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onErrorContainer,
                    contentColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
private fun ClassificationSection(
    uiState: VoiceClassifierUiState,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onClearError: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.currentClassification != null) {
                ClassificationResult(uiState.currentClassification)
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            FloatingActionButton(
                onClick = if (uiState.isListening) onStopListening else onStartListening,
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                containerColor = if (uiState.isListening) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.primary
            ) {
                if (uiState.isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = if (uiState.isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (uiState.isListening) "Stop Listening" else "Start Listening",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = when {
                    uiState.isProcessing -> "Processing audio..."
                    uiState.isListening -> "Listening... Tap to stop"
                    else -> "Tap to start listening"
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            
            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onClearError) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear error",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassificationResult(classification: AudioClassification) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val (icon, color) = when (classification.classification) {
            VoiceType.HUMAN -> Icons.Default.Person to Color(0xFF4CAF50)
            VoiceType.AI_GENERATED -> Icons.Default.SmartToy to Color(0xFF2196F3)
            VoiceType.PHONE_CALL -> Icons.Default.Phone to Color(0xFFFF9800)
        }
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = classification.classification.displayName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "Confidence: ${(classification.confidence * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatisticsSection(statistics: Map<VoiceType, Int>) {
    if (statistics.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Classification Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    VoiceType.values().forEach { type ->
                        val count = statistics[type] ?: 0
                        StatisticItem(
                            type = type,
                            count = count
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticItem(type: VoiceType, count: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val (icon, color) = when (type) {
            VoiceType.HUMAN -> Icons.Default.Person to Color(0xFF4CAF50)
            VoiceType.AI_GENERATED -> Icons.Default.SmartToy to Color(0xFF2196F3)
            VoiceType.PHONE_CALL -> Icons.Default.Phone to Color(0xFFFF9800)
        }
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = type.name.lowercase().replace("_", " "),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HistorySection(
    history: List<AudioClassification>,
    selectedFilter: VoiceType?,
    onFilterChange: (VoiceType?) -> Unit,
    onDeleteClassification: (AudioClassification) -> Unit,
    onClearOldClassifications: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Classification History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onClearOldClassifications) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear old classifications"
                    )
                }
            }
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                item {
                    FilterChip(
                        onClick = { onFilterChange(null) },
                        label = { Text("All") },
                        selected = selectedFilter == null
                    )
                }
                
                items(VoiceType.values()) { type ->
                    FilterChip(
                        onClick = { onFilterChange(type) },
                        label = { Text(type.displayName) },
                        selected = selectedFilter == type
                    )
                }
            }
            
            if (history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No classifications yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(history) { classification ->
                        HistoryItem(
                            classification = classification,
                            onDelete = { onDeleteClassification(classification) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(
    classification: AudioClassification,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, color) = when (classification.classification) {
                VoiceType.HUMAN -> Icons.Default.Person to Color(0xFF4CAF50)
                VoiceType.AI_GENERATED -> Icons.Default.SmartToy to Color(0xFF2196F3)
                VoiceType.PHONE_CALL -> Icons.Default.Phone to Color(0xFFFF9800)
            }
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = classification.classification.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "${(classification.confidence * 100).toInt()}% • ${formatTimestamp(classification.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete classification",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
} 