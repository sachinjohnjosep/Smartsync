package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiApiClient
import com.example.data.database.SmartSyncDatabase
import com.example.data.model.ConnectionEntity
import com.example.data.model.EventEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.PreferencesEntity
import com.example.data.model.TaskEntity
import com.example.data.repository.SmartSyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class SmartSyncViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SmartSyncRepository
    
    val allEvents: StateFlow<List<EventEntity>>
    val allTasks: StateFlow<List<TaskEntity>>
    val allConnections: StateFlow<List<ConnectionEntity>>
    val allNotifications: StateFlow<List<NotificationEntity>>
    val preferences: StateFlow<PreferencesEntity?>

    private val _aiBriefingText = MutableStateFlow("")
    val aiBriefingText: StateFlow<String> = _aiBriefingText.asStateFlow()

    private val _aiOutputText = MutableStateFlow("")
    val aiOutputText: StateFlow<String> = _aiOutputText.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiActionProposal = MutableStateFlow<EventProposal?>(null)
    val aiActionProposal: StateFlow<EventProposal?> = _aiActionProposal.asStateFlow()

    init {
        val database = SmartSyncDatabase.getDatabase(application)
        repository = SmartSyncRepository(database.smartSyncDao())

        allEvents = repository.allEvents.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allTasks = repository.allTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allConnections = repository.allConnections.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allNotifications = repository.allNotifications.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        preferences = repository.preferences.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        viewModelScope.launch {
            repository.prepopulateIfEmpty()
            
            // Set base briefing text from DB
            _aiBriefingText.value = "Welcome back! SmartSync successfully synchronized with Google Calendar, Slack, and Notion."
        }
    }

    fun deleteEvent(id: Int) {
        viewModelScope.launch {
            repository.deleteEventById(id)
        }
    }

    fun addEvent(
        title: String,
        description: String,
        startTime: Long,
        endTime: Long,
        location: String,
        category: String,
        platform: String,
        participants: String = "",
        documents: String = "",
        meetingUrl: String = "",
        travelTimeMinutes: Int = 0
    ) {
        viewModelScope.launch {
            repository.insertEvent(
                EventEntity(
                    title = title,
                    description = description,
                    startTime = startTime,
                    endTime = endTime,
                    location = location,
                    category = category,
                    platform = platform,
                    participants = participants,
                    documents = documents,
                    meetingUrl = meetingUrl,
                    travelTimeMinutes = travelTimeMinutes
                )
            )
        }
    }

    fun addTask(
        title: String,
        description: String,
        deadline: Long,
        priority: String,
        platform: String = "Local",
        category: String = "Work"
    ) {
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    title = title,
                    description = description,
                    deadline = deadline,
                    priority = priority,
                    platform = platform,
                    isCompleted = false,
                    category = category
                )
            )
        }
    }

    fun toggleTask(id: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateTaskStatus(id, isCompleted)
        }
    }

    fun deleteTask(id: Int) {
        viewModelScope.launch {
            repository.deleteTaskById(id)
        }
    }

    fun connectPlatform(platform: String, email: String) {
        viewModelScope.launch {
            repository.insertConnection(
                ConnectionEntity(
                    platform = platform,
                    isConnected = true,
                    email = email,
                    lastSynced = System.currentTimeMillis()
                )
            )
            // Send synthetic notification
            repository.insertNotification(
                NotificationEntity(
                    title = "🔌 Service Synced: $platform",
                    message = "Successfully established a real-time connection with $email. Glimpsing calendar allocations.",
                    timestamp = System.currentTimeMillis(),
                    type = "Prep"
                )
            )
        }
    }

    fun disconnectPlatform(platform: String) {
        viewModelScope.launch {
            repository.deleteConnectionByPlatform(platform)
            // Re-generate entry with disabled state
            repository.insertConnection(
                ConnectionEntity(platform = platform, isConnected = false, email = "", lastSynced = 0L)
            )
        }
    }

    fun deleteNotification(id: Int) {
        viewModelScope.launch {
            repository.deleteNotificationById(id)
        }
    }

    fun markNotificationRead(id: Int) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun updateWorkPreferences(timeZone: String, startHour: Int, endHour: Int) {
        viewModelScope.launch {
            val updated = PreferencesEntity(
                id = 1,
                timeZone = timeZone,
                workingHourStart = startHour,
                workingHourEnd = endHour,
                dailySummary = preferences.value?.dailySummary ?: ""
            )
            repository.savePreferences(updated)
        }
    }

    // --- AI ACTIONS ---

    fun generateAiMorningBriefing() {
        viewModelScope.launch {
            _isAiLoading.value = true
            val eventsStr = allEvents.value.joinToString("\n") { "• ${it.title} (${it.platform}) scheduled at ${java.text.SimpleDateFormat("hh:mm a").format(java.util.Date(it.startTime))}" }
            val tasksStr = allTasks.value.filter { !it.isCompleted }.joinToString("\n") { "• ${it.title} (Priority: ${it.priority}, Due: ${java.text.SimpleDateFormat("MM/dd").format(java.util.Date(it.deadline))})" }
            
            val prompt = """
                Generate a dynamic, highly professional, 2-paragraph morning schedule brief from these current entries. Underline key focus blocks or priority travel.
                
                Current Events:
                $eventsStr
                
                Current Tasks:
                $tasksStr
            """.trimIndent()

            val response = GeminiApiClient.getAiResponse(
                prompt = prompt,
                systemInstruction = "You are SmartSync Planner AI Core. Be encouraging, precise, professional, and dense with helpful scheduling advice. Spot overlapping times."
            )
            _aiBriefingText.value = response
            _isAiLoading.value = false
        }
    }

    fun executeAiCommand(command: String) {
        if (command.trim().isEmpty()) return
        
        viewModelScope.launch {
            _isAiLoading.value = true
            val response = GeminiApiClient.getAiResponse(
                prompt = command,
                systemInstruction = "You are SmartSync Planner's virtual assistant. You help manage schedules, tasks, and resolve conflicts via natural language command input."
            )
            _aiOutputText.value = response
            
            // Check if we should create a proposal based on the interpreted command
            val lower = command.lowercase()
            when {
                lower.contains("schedule") && lower.contains("john") -> {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, 7) // next week
                    cal.set(Calendar.HOUR_OF_DAY, 10)
                    cal.set(Calendar.MINUTE, 0)
                    val start = cal.timeInMillis
                    val end = start + (45 * 60 * 1000) // 45 mins
                    
                    _aiActionProposal.value = EventProposal(
                        title = "Sync & Collaboration with John Doe",
                        description = "Natural language AI generated scheduling event with John. Free slot match.",
                        startTime = start,
                        endTime = end,
                        location = "Google Meet Online",
                        category = "Work",
                        platform = "Google Calendar",
                        participants = "John Doe (Lead Designer)",
                        documents = "Project Design Tokens Specifications"
                    )
                }
                lower.contains("move") && lower.contains("friday") -> {
                    // Rescheduling proposal
                    val cal = Calendar.getInstance()
                    // find coming Friday
                    while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
                        cal.add(Calendar.DAY_OF_YEAR, 1)
                    }
                    cal.set(Calendar.HOUR_OF_DAY, 15)
                    cal.set(Calendar.MINUTE, 30) // afternoon 3:30 PM
                    val start = cal.timeInMillis
                    val end = start + (30 * 60 * 1000)

                    _aiActionProposal.value = EventProposal(
                        title = "Moved: Standup & Progress Alignment",
                        description = "Rescheduled afternoon slot proposed by SmartSync AI. Block optimization.",
                        startTime = start,
                        endTime = end,
                        location = "Zoom Link",
                        category = "Work",
                        platform = "Zoom",
                        participants = "Product Core Team"
                    )
                }
                else -> {
                    _aiActionProposal.value = null
                }
            }
            
            _isAiLoading.value = false
        }
    }

    fun commitProposal(proposal: EventProposal) {
        viewModelScope.launch {
            addEvent(
                title = proposal.title,
                description = proposal.description,
                startTime = proposal.startTime,
                endTime = proposal.endTime,
                location = proposal.location,
                category = proposal.category,
                platform = proposal.platform,
                participants = proposal.participants,
                documents = proposal.documents
            )
            
            // Log a notification
            repository.insertNotification(
                NotificationEntity(
                    title = "📅 Scheduled: ${proposal.title}",
                    message = "AI assistant successfully inserted event in ${proposal.platform}.",
                    timestamp = System.currentTimeMillis(),
                    type = "Suggestion"
                )
            )
            
            _aiActionProposal.value = null
            _aiOutputText.value = "✅ Done! I've scheduled '${proposal.title}' into your calendar timeline."
        }
    }

    fun discardProposal() {
        _aiActionProposal.value = null
    }

    /**
     * Shorthand trigger to auto-resolve the sample calendar conflict
     */
    fun resolveCalendarConflict(targetEventId: Int, newStartHour: Int, newStartMinute: Int) {
        viewModelScope.launch {
            // Find Tech Debt meeting from events
            val all = allEvents.value
            val match = all.find { it.id == targetEventId }
            if (match != null) {
                val cal = Calendar.getInstance()
                cal.timeInMillis = match.startTime
                cal.set(Calendar.HOUR_OF_DAY, newStartHour)
                cal.set(Calendar.MINUTE, newStartMinute)
                val newStart = cal.timeInMillis
                val duration = match.endTime - match.startTime
                val newEnd = newStart + duration

                repository.deleteEventById(match.id)
                repository.insertEvent(
                    match.copy(
                        id = 0, // auto gen
                        startTime = newStart,
                        endTime = newEnd,
                        isConflictResolved = true,
                        description = match.description + "\n(Rescheduled by AI Sync to avoid Strategy overlap)"
                    )
                )

                // Insert positive notification
                repository.insertNotification(
                    NotificationEntity(
                        title = "✅ Conflict Resolved",
                        message = "Moved '${match.title}' to 3:30 PM. Your afternoon timeline is now conflict-free!",
                        timestamp = System.currentTimeMillis(),
                        type = "Suggestion"
                    )
                )
            }
        }
    }

    private val _loadingSummaries = MutableStateFlow<Set<Int>>(emptySet())
    val loadingSummaries: StateFlow<Set<Int>> = _loadingSummaries.asStateFlow()

    fun generateMeetingSummary(event: EventEntity) {
        val eventId = event.id
        if (_loadingSummaries.value.contains(eventId)) return

        viewModelScope.launch {
            _loadingSummaries.value = _loadingSummaries.value + eventId

            val prompt = """
                Generate an intelligent, highly professional completed meeting summary in a clean markdown list format for:
                Meeting Title: ${event.title}
                Meeting Description: ${event.description}
                Participants: ${event.participants}
                Documents: ${event.documents}
                Platform: ${event.platform}

                You MUST extract exactly these three major sections with clear headings:
                1. **Key Decisions** (List of critical strategic choices or alignments agreed upon during this session)
                2. **Action Items** (Concrete list of next steps compiled from discussions)
                3. **Assigned Owners** (Specify who is responsible for each action item. Pair tasks with team names or participants like: "• Dave Miller to write QA tests")

                Be precise, actionable, yet dense. If no participants are listed, assume relevant project leads like PM Sarah and Lead Designer John. Format results in modern Markdown.
            """.trimIndent()

            val response = com.example.data.api.GeminiApiClient.getAiResponse(
                prompt = prompt,
                systemInstruction = "You are SmartSync AI Assistant specializing in synthesizing highly professional, executive meeting minutes and action item logs."
            )

            repository.updateEventSummary(eventId, response)

            // Also automatically log an alert notification that a meeting was analyzed and summarized
            repository.insertNotification(
                NotificationEntity(
                    title = "✨ AI Meeting Synthesized: ${event.title}",
                    message = "Successfully analyzed meeting transcript/notes. Extracted core decisions and action plan.",
                    timestamp = System.currentTimeMillis(),
                    type = "Suggestion"
                )
            )

            _loadingSummaries.value = _loadingSummaries.value - eventId
        }
    }
}

data class EventProposal(
    val title: String,
    val description: String,
    val startTime: Long,
    val endTime: Long,
    val location: String,
    val category: String,
    val platform: String,
    val participants: String = "",
    val documents: String = ""
)
