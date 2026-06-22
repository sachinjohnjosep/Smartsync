package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EventEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.SmartSyncViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: SmartSyncViewModel,
    onAddClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val events by viewModel.allEvents.collectAsState()
    
    // Day, Week, Month selector
    var currentViewMode by remember { mutableStateOf("Week") } // Day, Week, Month
    
    // Currently active selected day
    val selectedDayCalendar = remember { mutableStateOf(Calendar.getInstance()) }
    val selectedTimeInMillis = selectedDayCalendar.value.timeInMillis
    
    // Re-check start of selected day
    val activeSelectedDayStart = remember(selectedTimeInMillis) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = selectedTimeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }
    
    val activeSelectedDayEnd = activeSelectedDayStart + (24 * 3600 * 1000)

    val currentDayEvents = remember(events, activeSelectedDayStart) {
        events.filter { it.startTime in activeSelectedDayStart until activeSelectedDayEnd }
    }

    var selectedEventDetails by remember { mutableStateOf<EventEntity?>(null) }

    selectedEventDetails?.let { event ->
        MeetingDetailsDialog(
            event = event,
            onDismiss = { selectedEventDetails = null },
            viewModel = viewModel
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen")
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Selector Tab Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedDayCalendar.value.time),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Switch buttons
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface),
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                listOf("Day", "Week", "Month").forEach { mode ->
                    val selected = currentViewMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { currentViewMode = mode }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = mode,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // 2. Week Strip visual (Scrollable horizontal calendar line)
        WeekCalendarStrip(
            selectedCalendar = selectedDayCalendar.value,
            events = events,
            onDaySelected = { 
                selectedDayCalendar.value = it
            }
        )

        Divider(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        // 3. Show list of events or grid timeline for the clicked day
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Schedule agenda for " + SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(selectedDayCalendar.value.time),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    IconButton(onClick = onAddClicked) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "New Meeting",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (currentDayEvents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Free agenda day",
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "Fully Free! No Synced Bookings.",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Use the float '+' button to schedule local work sessions or sync Google Calendar.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                modifier = Modifier.width(260.dp)
                            )
                        }
                    }
                }
            } else {
                items(currentDayEvents) { event ->
                    EventCalendarCard(
                        event = event,
                        onDeleted = { viewModel.deleteEvent(it) },
                        onEventClick = { selectedEventDetails = it }
                    )
                }
            }
        }
    }
}

@Composable
fun WeekCalendarStrip(
    selectedCalendar: Calendar,
    events: List<EventEntity>,
    onDaySelected: (Calendar) -> Unit
) {
    val daysList = remember(selectedCalendar) {
        val list = mutableListOf<Calendar>()
        val startCal = selectedCalendar.clone() as Calendar
        startCal.add(Calendar.DAY_OF_YEAR, -3) // Show preceding 3 days
        for (i in 0..6) {
            list.add(startCal.clone() as Calendar)
            startCal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        items(daysList) { dayCal ->
            val isSelected = isSameDay(dayCal, selectedCalendar)
            val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(dayCal.time)
            val dayNum = SimpleDateFormat("dd", Locale.getDefault()).format(dayCal.time)
            
            // Check if this day has any synced items
            val hasEvents = remember(events) {
                events.any { event ->
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = event.startTime
                    isSameDay(cal, dayCal)
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .clickable { onDaySelected(dayCal) }
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = dayName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Text(
                        text = dayNum,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                    
                    // Simple dot marker representing scheduled meetings
                    if (hasEvents) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(5.dp)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                        )
                    } else {
                        Box(modifier = Modifier.size(5.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EventCalendarCard(
    event: EventEntity,
    onDeleted: (Int) -> Unit,
    onEventClick: (EventEntity) -> Unit
) {
    val categoryColor = when (event.category.lowercase()) {
        "work" -> CategoryWork
        "personal" -> CategoryPersonal
        "study" -> CategoryStudy
        "health" -> CategoryHealth
        "travel" -> CategoryTravel
        else -> MaterialTheme.colorScheme.primary
    }

    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val formattedStart = timeFormat.format(Date(event.startTime))
    val formattedEnd = timeFormat.format(Date(event.endTime))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("event_calendar_card")
            .clickable { onEventClick(event) },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(12.dp)
                            .background(categoryColor)
                    )
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = { onDeleted(event.id) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time & Platform details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Time slot",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "$formattedStart - $formattedEnd",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Linked Platform",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = event.platform,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (event.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
            }

            // Expanded detail: Meeting preparation card parameters (participants, links)
            if (event.participants.isNotEmpty() || event.documents.isNotEmpty() || event.meetingUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = "💼 MEETING PREPARATION CARD",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (event.participants.isNotEmpty()) {
                    Text(
                        text = "Participants: ${event.participants}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
                
                if (event.documents.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Attached Documents: ${event.documents}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textDecoration = TextDecoration.Underline
                    )
                }

                if (event.meetingUrl.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { /* Simulated browser trigger */ },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.Launch, contentDescription = "Launch", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("Launch meeting URL", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Commute / Travel estimation
            if (event.travelTimeMinutes > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CategoryTravel.copy(alpha = 0.15f))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Commute Time",
                            tint = CategoryTravel
                        )
                        Text(
                            text = "AI Commute Estimate: ${event.travelTimeMinutes} mins travel time required before startup.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CategoryTravel,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
