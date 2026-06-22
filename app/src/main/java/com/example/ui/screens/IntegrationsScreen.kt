package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.text.style.TextAlign
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
    val syncedDevices by viewModel.syncedDevices.collectAsState()

    var showConnectionDialog by remember { mutableStateOf(false) }
    var selectedPlatformToConnect by remember { mutableStateOf("") }
    var inputEmailAddress by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }

    // Sub-tab switcher state (0: Cloud Services, 1: My Synced Devices)
    var selectedSubTab by remember { mutableStateOf(0) }

    // Pairing dialog state
    var showPairDialog by remember { mutableStateOf(false) }
    var inputPairPin by remember { mutableStateOf("") }
    var pairDeviceName by remember { mutableStateOf("") }
    var pairDeviceType by remember { mutableStateOf("Mobile Phone") }
    var pairErrorText by remember { mutableStateOf<String?>(null) }
    var pairSuccessMsg by remember { mutableStateOf<String?>(null) }

    // Generated pin reference
    var currentGeneratedPin by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("integrations_screen")
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Mode Header
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Connections & Sync",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Manage external cloud pipeline channels or link accounts across multiple client devices.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Sub-Tab Segment Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf("Cloud Services", "My Linked Devices")
            tabs.forEachIndexed { idx, title ->
                val active = selectedSubTab == idx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { selectedSubTab = idx }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Divider(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
            thickness = 1.dp,
            modifier = Modifier.padding(top = 12.dp)
        )

        if (selectedSubTab == 0) {
            // --- TAB 0: CLOUD PIPELINE CONNECTIONS (ORIGINAL GRID) ---
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
        } else {
            // --- TAB 1: SYNC ACCOUNT IN MY DEVICES (NEW HIGH-FIDELITY IMPLEMENTATION) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cloud Sync Status Hero Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = "Active Sync",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Real-Time Cloud Synchronization",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Calendar scheduling, AI plans, and completed details are encrypted end-to-end and synced instantly.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Synced Account details
                val preferencesState by viewModel.preferences.collectAsState()
                val activeEmail = preferencesState?.userEmail ?: "you@workspace.com"

                Text(
                    text = "SYNCED PROFILE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "User Account",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = preferencesState?.userName ?: "Workspace Account",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = activeEmail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // Header for Devices List
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SYNCED DEVICES AND CLIENTS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "${syncedDevices.size} Linked",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Devices Listing cards
                syncedDevices.forEach { dev ->
                    val devIcon = when (dev.deviceType.lowercase()) {
                        "laptop", "macbook", "desktop" -> Icons.Default.Laptop
                        "tablet", "ipad" -> Icons.Default.Tablet
                        else -> Icons.Default.PhoneAndroid
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("synced_device_card_${dev.deviceName.replace(" ", "_").lowercase()}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = if (dev.isCurrentDevice) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(40.dp)
                                    .background(
                                        if (dev.isCurrentDevice) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = devIcon,
                                    contentDescription = "Device Type",
                                    tint = if (dev.isCurrentDevice) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = dev.deviceName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (dev.isCurrentDevice) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                "THIS DEVICE",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${dev.deviceType} • ${dev.osVersion}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                    Text(
                                        text = "Online",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                                Text(
                                    text = dev.lastSyncTime,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }

                // Actions Card for generator & pairing
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Add Sync Device",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Link multiple tablets, phones, or desktops to direct plans simultaneously. Generate a temporary pair link or specify standard pair codes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Generating Codes Option
                            Button(
                                onClick = {
                                    currentGeneratedPin = viewModel.generateSyncCode()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("generate_pairing_pin_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.VpnKey, contentDescription = "Pin Icon", modifier = Modifier.size(16.dp))
                                    Text("Display Sync Pin", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Entering pairing code manual
                            Button(
                                onClick = {
                                    pairDeviceName = ""
                                    inputPairPin = ""
                                    pairErrorText = null
                                    pairSuccessMsg = null
                                    showPairDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("submit_pairing_pin_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Link Icon", modifier = Modifier.size(16.dp))
                                    Text("Enter Sync Pin", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Code display animate section
                        AnimatedVisibility(visible = currentGeneratedPin.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "TEMPORARY SYNC PAIRING PIN",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentGeneratedPin,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 2.sp,
                                        modifier = Modifier.testTag("generated_pairing_pin")
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Expires in 5 minutes. Enter this code on your secondary device to synchronize accounts.",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal dialog for entering the partner device sync pin
    if (showPairDialog) {
        AlertDialog(
            onDismissRequest = { showPairDialog = false },
            title = { Text("Link Secondary Device", fontWeight = FontWeight.Black) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Input the sync pin displayed on your secondary app client to connect profiles.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    OutlinedTextField(
                        value = pairDeviceName,
                        onValueChange = {
                            pairDeviceName = it
                            pairErrorText = null
                        },
                        label = { Text("Client Device Name") },
                        placeholder = { Text("e.g. My Pixel Tablet") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pair_device_name_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = inputPairPin,
                        onValueChange = {
                            inputPairPin = it.uppercase()
                            pairErrorText = null
                        },
                        label = { Text("6-Digit Sync Pin") },
                        placeholder = { Text("SS-XXX-XXX") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pair_device_pin_input"),
                        singleLine = true
                    )

                    // Error presentation
                    pairErrorText?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Success presentation
                    pairSuccessMsg?.let { msg ->
                        Text(
                            text = msg,
                            color = Color(0xFF10B981),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedPin = inputPairPin.replace("-", "").trim()
                        if (pairDeviceName.isEmpty() || trimmedPin.isEmpty()) {
                            pairErrorText = "All fields are required."
                            return@Button
                        }
                        if (trimmedPin.length != 6) {
                            pairErrorText = "Pairing pin must be exactly 6 alphanumeric characters."
                            return@Button
                        }

                        val success = viewModel.registerNewDevice(
                            name = pairDeviceName,
                            brand = pairDeviceType,
                            pin = trimmedPin
                        )
                        if (success) {
                            pairSuccessMsg = "Device successfully paired and synchronized! Updating registry..."
                            pairErrorText = null
                            inputPairPin = ""
                            pairDeviceName = ""
                            // Dismiss automatically in 1.5 seconds
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                showPairDialog = false
                            }, 1500)
                        } else {
                            pairErrorText = "An error occurred with coupling code verification."
                        }
                    }
                ) {
                    Text("Apply Sync Link", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPairDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal sync profile dialog input (original slot)
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
