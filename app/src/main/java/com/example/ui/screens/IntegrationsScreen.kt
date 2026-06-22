package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.SmartSyncViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IntegrationsScreen(
    viewModel: SmartSyncViewModel,
    modifier: Modifier = Modifier
) {
    val connections by viewModel.allConnections.collectAsState()

    var showConnectionDialog by remember { mutableStateOf(false) }
    var selectedPlatformToConnect by remember { mutableStateOf("") }
    var inputEmailAddress by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("integrations_screen")
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Mode Header
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "External Sync Profiles",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Securely establish connections with external calendar providers, directories, and tasks channels.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f), thickness = 1.dp)

        // Switch List of platforms
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 280.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val list = connections.sortedBy { !it.isConnected }
            items(list) { conn ->
                IntegrationCardItem(
                    connection = conn,
                    onConnectRequest = {
                        selectedPlatformToConnect = conn.platform
                        inputEmailAddress = ""
                        emailError = false
                        showConnectionDialog = true
                    },
                    onDisconnect = {
                        viewModel.disconnectPlatform(conn.platform)
                    }
                )
            }
        }
    }

    // Modal sync profile dialog input
    if (showConnectionDialog) {
        AlertDialog(
            onDismissRequest = { showConnectionDialog = false },
            title = {
                Text(
                    text = "Sync $selectedPlatformToConnect",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Establish a synchronized pipeline with your $selectedPlatformToConnect workspace account.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    OutlinedTextField(
                        value = inputEmailAddress,
                        onValueChange = {
                            inputEmailAddress = it
                            emailError = !it.contains("@")
                        },
                        label = { Text("Profile email / login identifier") },
                        placeholder = { Text("username@domain.com") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("integration_email_input"),
                        isError = emailError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    if (emailError) {
                        Text(
                            text = "Please input a valid profile email address.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputEmailAddress.isNotEmpty() && inputEmailAddress.contains("@")) {
                            viewModel.connectPlatform(selectedPlatformToConnect, inputEmailAddress)
                            showConnectionDialog = false
                        } else {
                            emailError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Sync Credentials", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConnectionDialog = false }) {
                    Text("Dismiss")
                }
            }
        )
    }
}

@Composable
fun IntegrationCardItem(
    connection: ConnectionEntity,
    onConnectRequest: () -> Unit,
    onDisconnect: () -> Unit
) {
    val syncedTimeText = if (connection.lastSynced > 0L) {
        val df = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        "Synced: " + df.format(Date(connection.lastSynced))
    } else {
        "Disabled"
    }

    val iconPair = when (connection.platform.lowercase()) {
        "google calendar" -> Pair(Icons.Default.Today, CategoryWork)
        "notion" -> Pair(Icons.Default.Bookmark, Color.Black)
        "slack" -> Pair(Icons.Default.ChatBubble, CategoryHealth)
        "outlook" -> Pair(Icons.Default.Email, CategoryWork)
        "zoom" -> Pair(Icons.Default.VideoCall, CategoryWork)
        "todoist" -> Pair(Icons.Default.CheckBox, TertiaryDark)
        else -> Pair(Icons.Default.CloudQueue, MaterialTheme.colorScheme.primary)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("integration_card_${connection.platform.replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = if (connection.isConnected) CardDefaults.outlinedCardBorder() else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(40.dp)
                            .background(iconPair.second.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconPair.first,
                            contentDescription = connection.platform,
                            tint = iconPair.second,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = connection.platform,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (connection.isConnected) connection.email else "Not Synced",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Check indicator
                if (connection.isConnected) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(24.dp)
                            .background(Color(0xFF10B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Connected",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = syncedTimeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Medium
                )

                if (connection.isConnected) {
                    TextButton(
                        onClick = onDisconnect,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Disconnect", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onConnectRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), contentColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Establish Sync", fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
