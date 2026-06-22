package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.SmartSyncViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: SmartSyncViewModel,
    modifier: Modifier = Modifier
) {
    // Current Active Navigation Index
    var activeTabIndex by remember { mutableStateOf(0) } // 0: Dashboard, 1: Calendar, 2: Planner, 3: Tasks, 4: Notifications, 5: Integrations, 6: Settings
    
    // Onboarding status flow state
    var showOnboarding by remember { mutableStateOf(true) }
    var currentOnboardingPage by remember { mutableStateOf(0) }

    // Dialog Toggle flags
    var showAddEventDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    // Responsive setup check
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val navigationItems = listOf(
        NavigationItem("Dashboard", Icons.Default.Dashboard, Icons.Outlined.Dashboard),
        NavigationItem("Calendar", Icons.Default.CalendarMonth, Icons.Outlined.CalendarMonth),
        NavigationItem("AI Planner", Icons.Default.AutoAwesome, Icons.Outlined.AutoAwesome),
        NavigationItem("Tasks", Icons.Default.Checklist, Icons.Outlined.Checklist),
        NavigationItem("Alerts", Icons.Default.Notifications, Icons.Outlined.Notifications),
        NavigationItem("Sync", Icons.Default.Sync, Icons.Outlined.Sync),
        NavigationItem("Settings", Icons.Default.Settings, Icons.Outlined.Settings)
    )

    if (showOnboarding) {
        // --- ONBOARDING FLOW PANEL ---
        OnboardingFlow(
            currentPage = currentOnboardingPage,
            onNext = {
                if (currentOnboardingPage == 1) {
                    showOnboarding = false
                } else {
                    currentOnboardingPage++
                }
            },
            onSkip = {
                showOnboarding = false
            }
        )
    } else {
        // --- WORKSPACE CORE SCREEN ---
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Adaptive Navigation Side-Rail for large wide profiles (tablets)
            if (isTablet) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxHeight()
                        .testTag("tablet_nav_rail"),
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(44.dp)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "App Emblem",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "SmartSync",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    content = {
                        navigationItems.forEachIndexed { index, nav ->
                            val selected = activeTabIndex == index
                            NavigationRailItem(
                                selected = selected,
                                onClick = { activeTabIndex = index },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) nav.activeIcon else nav.inactiveIcon,
                                        contentDescription = nav.label
                                    )
                                },
                                label = { Text(nav.label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("tablet_nav_item_${nav.label.lowercase()}")
                            )
                        }
                    }
                )
            }

            // Main Scaffold area (AppBar, page load area, bottom standard bar)
            Scaffold(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (activeTabIndex == 0) { // Dashboard screen
                                    Column {
                                        Text(
                                            text = "SmartSync",
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = (-0.5).sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF10B981)) // bg-emerald-500
                                            )
                                            Text(
                                                text = "AI SYNC ACTIVE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F9F72), // text-emerald-600
                                                letterSpacing = 0.8.sp
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = navigationItems[activeTabIndex].label,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        },
                        actions = {
                            // User Quick Profile Mock Avatar matching design HTML (AD, bg #D1E4FF, border white, shadow-sm, text #001D36)
                            Box(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "AD",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                },
                floatingActionButton = {
                    // Floating button that dynamically shifts to context trigger add task or add event
                    FloatingActionButton(
                        onClick = {
                            if (activeTabIndex == 3) {
                                showAddTaskDialog = true
                            } else {
                                showAddEventDialog = true
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("core_floating_action_button")
                    ) {
                        Icon(
                            imageVector = if (activeTabIndex == 3) Icons.Default.PlaylistAdd else Icons.Default.Add,
                            contentDescription = "Create Item"
                        )
                    }
                },
                bottomBar = {
                    // bottom standard navigation on Compact Phone profiles
                    if (!isTablet) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("mobile_bottom_nav_bar")
                        ) {
                            navigationItems.forEachIndexed { index, nav ->
                                val selected = activeTabIndex == index
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { activeTabIndex = index },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) nav.activeIcon else nav.inactiveIcon,
                                            contentDescription = nav.label
                                        )
                                    },
                                    label = { Text(nav.label, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.testTag("mobile_nav_item_${nav.label.lowercase()}")
                                )
                            }
                        }
                    }
                }
            ) { paddingValues ->
                // Pages dispatcher load
                val pgModifier = Modifier.padding(paddingValues)
                _activeScreen(
                    index = activeTabIndex,
                    viewModel = viewModel,
                    onNavigateToTab = { activeTabIndex = it },
                    onAddButtonClicked = { showAddEventDialog = true },
                    onAddTaskClicked = { showAddTaskDialog = true },
                    modifier = pgModifier
                )
            }
        }
    }

    // --- ADD EVENT DIALOG MODAL ---
    if (showAddEventDialog) {
        AddEventDialog(
            onDismiss = { showAddEventDialog = false },
            onConfirmAdd = { title, desc, start, end, loc, category, platform, parts, docs, meeting ->
                viewModel.addEvent(
                    title = title,
                    description = desc,
                    startTime = start,
                    endTime = end,
                    location = loc,
                    category = category,
                    platform = platform,
                    participants = parts,
                    documents = docs,
                    meetingUrl = meeting
                )
                showAddEventDialog = false
            }
        )
    }

    // --- ADD TASK DIALOG MODAL ---
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirmAdd = { title, desc, limit, priority, platform, category ->
                viewModel.addTask(
                    title = title,
                    description = desc,
                    deadline = limit,
                    priority = priority,
                    platform = platform,
                    category = category
                )
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
private fun _activeScreen(
    index: Int,
    viewModel: SmartSyncViewModel,
    onNavigateToTab: (Int) -> Unit,
    onAddButtonClicked: () -> Unit,
    onAddTaskClicked: () -> Unit,
    modifier: Modifier
) {
    when (index) {
        0 -> DashboardScreen(viewModel = viewModel, onNavigateToTab = onNavigateToTab, onAddClicked = onAddButtonClicked, modifier = modifier)
        1 -> CalendarScreen(viewModel = viewModel, onAddClicked = onAddButtonClicked, modifier = modifier)
        2 -> AiPlannerScreen(viewModel = viewModel, modifier = modifier)
        3 -> TasksScreen(viewModel = viewModel, onAddTaskClicked = onAddTaskClicked, modifier = modifier)
        4 -> NotificationsScreen(viewModel = viewModel, modifier = modifier)
        5 -> IntegrationsScreen(viewModel = viewModel, modifier = modifier)
        6 -> SettingsScreen(viewModel = viewModel, modifier = modifier)
    }
}

// Helper class representing Navigation tags
data class NavigationItem(
    val label: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
)

// --- ONBOARDING SCREEN DESIGN PANEL ---
@Composable
fun OnboardingFlow(
    currentPage: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("onboarding_pane")
            .background(BackgroundDark)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .maxHeightWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Illustrated visual image asset compiled elegantly
            Image(
                painter = painterResource(id = R.drawable.planner_onboarding_hero),
                contentDescription = "Onboarding Welcome illustration",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (currentPage == 0) {
                Text(
                    text = "Welcome to SmartSync",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "An intelligent scheduling companion that automatically connects directories, calendars, and emails into one seamless, unified daily timeline.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            } else {
                Text(
                    text = "AI-Driven Optimization",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Unlock natural language commands, daily morning Summaries generated by Gemini, overlapping conflict detection, and meeting prep cards.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action triggers row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onSkip) {
                    Text("Skip", color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (currentPage == 0) "Synchronize Profiles" else "Let's Begin!",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// Custom responsive constrain limiters
fun Modifier.maxHeightWidth(): Modifier = this
    .widthIn(max = 500.dp)
    .fillMaxWidth()

// --- DETAIL MODALS CREATIONS SLOTS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onConfirmAdd: (String, String, Long, Long, String, String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var startHourVal by remember { mutableStateOf(10) } // Default 10 AM
    var endHourVal by remember { mutableStateOf(11) } // Default 11 AM
    var loc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Work") }
    var platform by remember { mutableStateOf("Local") }
    var participants by remember { mutableStateOf("") }
    var docs by remember { mutableStateOf("") }
    var meetingUrl by remember { mutableStateOf("") }

    val platformList = listOf("Local", "Google Calendar", "Outlook", "Zoom", "Notion")
    val categoryList = listOf("Work", "Personal", "Study", "Health", "Travel")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Sync Event", fontWeight = FontWeight.Black) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title") },
                    modifier = Modifier.fillMaxWidth().testTag("add_event_title")
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startHourVal.toString(),
                        onValueChange = { startHourVal = it.toIntOrNull() ?: 9 },
                        label = { Text("Start (AM/PM)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endHourVal.toString(),
                        onValueChange = { endHourVal = it.toIntOrNull() ?: 10 },
                        label = { Text("End (AM/PM)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = loc,
                    onValueChange = { loc = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Simulators parameters prep card
                OutlinedTextField(
                    value = participants,
                    onValueChange = { participants = it },
                    label = { Text("Participants (emails)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = docs,
                    onValueChange = { docs = it },
                    label = { Text("Attach docs link") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = meetingUrl,
                    onValueChange = { meetingUrl = it },
                    label = { Text("Zoom / Meet URL") },
                    placeholder = { Text("https://zoom.us/...") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty()) {
                        // Create modern standard timestamps relative to today
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.HOUR_OF_DAY, startHourVal)
                        cal.set(Calendar.MINUTE, 0)
                        val startL = cal.timeInMillis

                        cal.set(Calendar.HOUR_OF_DAY, endHourVal)
                        val endL = cal.timeInMillis

                        onConfirmAdd(title, desc, startL, endL, loc, category, platform, participants, docs, meetingUrl)
                    }
                }
            ) {
                Text("Confirm Add", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirmAdd: (String, String, Long, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("High") }
    var platform by remember { mutableStateOf("Local") }
    var category by remember { mutableStateOf("Work") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Sync Task", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth().testTag("add_task_title")
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Details") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Simplified selectors
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("High", "Medium", "Low").forEach { pri ->
                        val selected = priority == pri
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                                .clickable { priority = pri }
                                .padding(8.dp)
                        ) {
                            Text(pri, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty()) {
                        val deadline = System.currentTimeMillis() + (24 * 3600 * 1000) // Default明天 deadline
                        onConfirmAdd(title, desc, deadline, priority, platform, category)
                    }
                }
            ) {
                Text("Lock Task", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
