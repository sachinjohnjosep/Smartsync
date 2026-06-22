package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PreferencesEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.SmartSyncViewModel

@Composable
fun SettingsScreen(
    viewModel: SmartSyncViewModel,
    modifier: Modifier = Modifier
) {
    val preferences by viewModel.preferences.collectAsState()

    // Temp variables for UI fields
    var selectedTimeZone by remember { mutableStateOf("EDT (UTC-4)") }
    var startHour by remember { mutableStateOf(9) }
    var endHour by remember { mutableStateOf(17) }
    var notifyEnabled by remember { mutableStateOf(true) }
    var lockCalendarSync by remember { mutableStateOf(false) }

    // Synchronize field states when DB loads
    LaunchedEffect(preferences) {
        preferences?.let {
            selectedTimeZone = it.timeZone
            startHour = it.workingHourStart
            endHour = it.workingHourEnd
        }
    }

    // Timezones dropdown selection dialog
    var showTimeZoneDropdown by remember { mutableStateOf(false) }
    val timeZonesList = listOf(
        "UTC (Greenwich)",
        "EDT (UTC-4)",
        "PDT (UTC-7)",
        "BST (UTC+1)",
        "IST (UTC+5.30)",
        "AEST (UTC+10)"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen")
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Title
        item {
            Column {
                Text(
                    text = "Preferences & Configurations",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Configure time zone alignments, daily notifications, and priority working periods.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        item { Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f), thickness = 1.dp) }

        // TimeZone Settings section
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("timezone_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = "TZ", tint = MaterialTheme.colorScheme.primary)
                        Text("Workspace Time Zone", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "SmartSync aligns all synced service links based on this central timeline setting.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            .clickable { showTimeZoneDropdown = true }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedTimeZone,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                        }
                    }
                }
            }
        }

        // Working Hours configuration section
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("working_hours_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.CalendarViewDay, contentDescription = "Hours", tint = MaterialTheme.colorScheme.primary)
                        Text("Standard Synchronized Hours", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "Preferred hours for meetings. AI scheduling will automatically target suggestions inside this bracket.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    // Hours adjustable fields
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Preferred Start Hour", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Slider(
                                value = startHour.toFloat(),
                                onValueChange = { startHour = it.toInt() },
                                valueRange = 6f..12f,
                                steps = 6
                            )
                            Text("$startHour:00 AM", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Preferred End Hour", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Slider(
                                value = endHour.toFloat(),
                                onValueChange = { endHour = it.toInt() },
                                valueRange = 13f..21f,
                                steps = 8
                            )
                            Text("${if (endHour > 12) endHour - 12 else endHour}:00 PM", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Button(
                        onClick = { viewModel.updateWorkPreferences(selectedTimeZone, startHour, endHour) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save preferences", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Sync Toggles (Notification slide preferences etc.)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("sync_toggles_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Switch 1: Notification push
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = "Push settings", tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("Smart push alerts", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Notify 15 mins prior of meetings", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                        Switch(checked = notifyEnabled, onCheckedChange = { notifyEnabled = it })
                    }

                    // Switch 2: Secure Lock metadata offline
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = "Lock configs", tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("Offline sandbox isolation", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Limits analytics sharing with services", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                        Switch(checked = lockCalendarSync, onCheckedChange = { lockCalendarSync = it })
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SmartSync Planner v1.5 • Locally Sandbox Secured",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // TimeZone select modal options
    if (showTimeZoneDropdown) {
        AlertDialog(
            onDismissRequest = { showTimeZoneDropdown = false },
            title = { Text("Select timezone", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(timeZonesList) { tz ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedTimeZone = tz
                                    showTimeZoneDropdown = false
                                }
                                .padding(14.dp)
                        ) {
                            Text(text = tz, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}
