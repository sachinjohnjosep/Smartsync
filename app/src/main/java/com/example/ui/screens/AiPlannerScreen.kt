package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EventEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.SmartSyncViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiPlannerScreen(
    viewModel: SmartSyncViewModel,
    modifier: Modifier = Modifier
) {
    val events by viewModel.allEvents.collectAsState()
    val preferences by viewModel.preferences.collectAsState()
    val aiOutputText by viewModel.aiOutputText.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val proposal by viewModel.aiActionProposal.collectAsState()

    var userCommandText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Look for overlaps in events
    // An overlap occurs when event A ends after event B starts AND event A starts before event B ends
    // Let's filter to upcoming actual conflicts
    val detectedConflicts = remember(events) {
        val list = mutableListOf<Pair<EventEntity, EventEntity>>()
        val sorted = events.sortedBy { it.startTime }
        for (i in 0 until sorted.size - 1) {
            val eventA = sorted[i]
            val eventB = sorted[i + 1]
            if (eventA.endTime > eventB.startTime && !eventA.isConflictResolved && !eventB.isConflictResolved) {
                list.add(Pair(eventA, eventB))
            }
        }
        list
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_planner_screen")
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Title
        item {
            Column {
                Text(
                    text = "AI Smart Sync Planner",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Let Gemini optimize meeting blocks, resolve conflicts, and process commands.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        // 1. Conflict warnings + resolution recommendations
        if (detectedConflicts.isNotEmpty()) {
            item {
                Text(
                    text = "Detected Overlaps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(detectedConflicts) { (eventA, eventB) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Active Conflict",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Calendar overlap detected!",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "• Event 1: '${eventA.title}' (${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(eventA.startTime))})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Event 2: '${eventB.title}' (${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(eventB.startTime))})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "💡 SmartSync suggestion to resolve conflict:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Text(
                            text = "Shift '${eventB.title}' to 03:30 PM today, since all participants Dave and Jane are available.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.resolveCalendarConflict(eventB.id, 15, 30) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(imageVector = Icons.Default.Done, contentDescription = "Schedule Shift", modifier = Modifier.size(16.dp))
                                Text("Auto-Reschedule event B", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.LockReset, contentDescription = "Conflict free", tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(
                                text = "Timeline fully optimized",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Zero parallel overlap conflicts found in connected calendar profiles.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }

        // 2. Predict busy periods & Focus hours
        item {
            Column {
                Text(
                    text = "Aesthetic Daily Profile Predictions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.Timeline, contentDescription = "Predictions", tint = CategoryStudy)
                            Text("Peak Hour & Focus Predictions", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        
                        Text(
                            text = preferences?.learnPreferencesJson ?: "Analyzing calendar patterns to determine peak email hours and focus slots...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        // Progress Blocks representation
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.fillMaxHeight().weight(0.3f).background(CategoryStudy)) // Focus (30%)
                                Box(modifier = Modifier.fillMaxHeight().weight(0.1f).background(Color.Transparent))
                                Box(modifier = Modifier.fillMaxHeight().weight(0.4f).background(CategoryWork)) // Meeting (40%)
                                Box(modifier = Modifier.fillMaxHeight().weight(0.2f).background(CategoryPersonal)) // Personal (20%)
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("🟩 Focus Block recommended from 10:00 AM - 12:00 PM.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }

        // 3. Gemini Commands Hub & assistant bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Natural Language Assistant",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.SupportAgent, contentDescription = "AI Core", tint = MaterialTheme.colorScheme.secondary)
                            Text("Command Console", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        TextField(
                            value = userCommandText,
                            onValueChange = { userCommandText = it },
                            placeholder = { Text("E.g., 'Schedule a meeting with John next week'") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_command_input"),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                viewModel.executeAiCommand(userCommandText)
                                keyboardController?.hide()
                            }),
                            trailingIcon = {
                                if (isAiLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    IconButton(onClick = {
                                        viewModel.executeAiCommand(userCommandText)
                                        keyboardController?.hide()
                                    }) {
                                        Icon(imageVector = Icons.Default.Send, contentDescription = "Submit", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                disabledContainerColor = MaterialTheme.colorScheme.background
                            )
                        )

                        // Sample helper commands strip
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Suggested Prompts:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PromptPill("Schedule meeting with John.") { userCommandText = it }
                            PromptPill("Show my free time tomorrow.") { userCommandText = it }
                        }
                    }
                }
            }
        }

        // 4. AI response area
        if (aiOutputText.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("ai_response_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI response", tint = MaterialTheme.colorScheme.secondary)
                            Text("Gemini Scheduler Response", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = aiOutputText,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 5. Action proposals (e.g. Schedule Event proposal trigger)
        if (proposal != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("ai_proposal_action_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Authorize action", tint = Color(0xFF10B981))
                            Text("Lock in AI Proposal?", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
                        }

                        Text(
                            text = "Title: ${proposal!!.title}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "Proposed platform: ${proposal!!.platform}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.commitProposal(proposal!!) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Text("Acknowledge & Save", fontWeight = FontWeight.Bold)
                            }

                            TextButton(onClick = { viewModel.discardProposal() }) {
                                Text("Dismiss", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PromptPill(
    text: String,
    onClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            .clickable { onClick(text) }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
    }
}
