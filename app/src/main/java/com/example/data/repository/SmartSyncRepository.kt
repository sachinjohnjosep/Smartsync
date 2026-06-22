package com.example.data.repository

import com.example.data.database.SmartSyncDao
import com.example.data.model.ConnectionEntity
import com.example.data.model.EventEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.PreferencesEntity
import com.example.data.model.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class SmartSyncRepository(private val dao: SmartSyncDao) {

    val allEvents: Flow<List<EventEntity>> = dao.getAllEvents()
    val allTasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    val allConnections: Flow<List<ConnectionEntity>> = dao.getAllConnections()
    val allNotifications: Flow<List<NotificationEntity>> = dao.getAllNotifications()
    val preferences: Flow<PreferencesEntity?> = dao.getPreferences()

    suspend fun insertEvent(event: EventEntity) = dao.insertEvent(event)
    suspend fun deleteEventById(id: Int) = dao.deleteEventById(id)
    suspend fun updateEventSummary(id: Int, summary: String?) = dao.updateEventSummary(id, summary)
    suspend fun clearAllEvents() = dao.clearAllEvents()

    suspend fun insertTask(task: TaskEntity) = dao.insertTask(task)
    suspend fun updateTaskStatus(id: Int, isCompleted: Boolean) = dao.updateTaskStatus(id, isCompleted)
    suspend fun deleteTaskById(id: Int) = dao.deleteTaskById(id)

    suspend fun insertConnection(connection: ConnectionEntity) = dao.insertConnection(connection)
    suspend fun deleteConnectionByPlatform(platform: String) = dao.deleteConnectionByPlatform(platform)

    suspend fun insertNotification(notification: NotificationEntity) = dao.insertNotification(notification)
    suspend fun markNotificationAsRead(id: Int) = dao.markNotificationAsRead(id)
    suspend fun deleteNotificationById(id: Int) = dao.deleteNotificationById(id)

    suspend fun savePreferences(preferences: PreferencesEntity) = dao.insertPreferences(preferences)

    // Prepopulate some realistic data if DB is empty
    suspend fun prepopulateIfEmpty() {
        // Prepare calendar timestamps relative to current time
        val calendar = Calendar.getInstance()
        val today = calendar.clone() as Calendar
        val tomorrow = calendar.clone() as Calendar
        tomorrow.add(Calendar.DAY_OF_YEAR, 1)

        // Connections prepopulation
        val existingConnections = allConnections.firstOrNull() ?: emptyList()
        if (existingConnections.isEmpty()) {
            val sampleConnections = listOf(
                ConnectionEntity("Google Calendar", true, "sachinjohn649@gmail.com", System.currentTimeMillis()),
                ConnectionEntity("Slack", true, "workspace-sync@slack.com", System.currentTimeMillis()),
                ConnectionEntity("Notion", true, "sachin_notion@notion.so", System.currentTimeMillis()),
                ConnectionEntity("Todoist", true, "sachin_planner@todoist.com", System.currentTimeMillis()),
                ConnectionEntity("Outlook", false, "", 0L),
                ConnectionEntity("Zoom", true, "sachinjohn649@gmail.com", System.currentTimeMillis()),
                ConnectionEntity("Microsoft Teams", false, "", 0L),
                ConnectionEntity("Apple Calendar", false, "", 0L),
                ConnectionEntity("Trello", false, "", 0L)
            )
            for (conn in sampleConnections) {
                dao.insertConnection(conn)
            }
        }

        // Events prepopulation
        val existingEvents = allEvents.firstOrNull() ?: emptyList()
        if (existingEvents.isEmpty()) {
            val sampleEvents = mutableListOf<EventEntity>()

            // 1. Morning Kickoff Meeting (Zoom) - Today 9:30 AM to 10:00 AM
            today.set(Calendar.HOUR_OF_DAY, 9)
            today.set(Calendar.MINUTE, 30)
            val startTime1 = today.timeInMillis
            today.set(Calendar.HOUR_OF_DAY, 10)
            today.set(Calendar.MINUTE, 0)
            val endTime1 = today.timeInMillis

            sampleEvents.add(
                EventEntity(
                    title = "Daily Team Standup & Alignment",
                    description = "Sync with the core product team on blockers and sprint progress. Reviewing Figma prototypes.",
                    startTime = startTime1,
                    endTime = endTime1,
                    location = "Zoom Meeting",
                    category = "Work",
                    platform = "Zoom",
                    participants = "Sarah Jenkins (PM), Dave Miller (QA), John Doe (Designer)",
                    documents = "Figma Prototypes v1.2, Sprint backlog items",
                    meetingUrl = "https://zoom.us/j/9843924021",
                    isRecurring = true,
                    recurrenceRule = "Daily",
                    travelTimeMinutes = 0
                )
            )

            // 2. Focused Deep Work Block - Today 10:30 AM to 12:30 PM
            today.set(Calendar.HOUR_OF_DAY, 10)
            today.set(Calendar.MINUTE, 30)
            val startTime2 = today.timeInMillis
            today.set(Calendar.HOUR_OF_DAY, 12)
            today.set(Calendar.MINUTE, 30)
            val endTime2 = today.timeInMillis

            sampleEvents.add(
                EventEntity(
                    title = "🎨 Focus: Design Tokens Re-architecture",
                    description = "Uninterrupted focus time to clean up theme configurations and resolve compose padding issues.",
                    startTime = startTime2,
                    endTime = endTime2,
                    location = "Deep Work Mode",
                    category = "Personal",
                    platform = "Notion",
                    participants = "",
                    documents = "Design Tokens Specification v4",
                    meetingUrl = "",
                    isRecurring = false,
                    travelTimeMinutes = 0
                )
            )

            // 3. Lunch & Rest - Today 12:30 PM to 1:30 PM
            today.set(Calendar.HOUR_OF_DAY, 12)
            today.set(Calendar.MINUTE, 30)
            val startTimeLunch = today.timeInMillis
            today.set(Calendar.HOUR_OF_DAY, 13)
            today.set(Calendar.MINUTE, 30)
            val endTimeLunch = today.timeInMillis

            sampleEvents.add(
                EventEntity(
                    title = "🥗 Lunch & Power Nap",
                    description = "Take some downtime, stretch, grab a healthy bite to eat.",
                    startTime = startTimeLunch,
                    endTime = endTimeLunch,
                    location = "Kitchen / Lounge",
                    category = "Health",
                    platform = "Local",
                    participants = "",
                    documents = "",
                    meetingUrl = "",
                    isRecurring = true,
                    recurrenceRule = "Daily",
                    travelTimeMinutes = 0
                )
            )

            // 4. Overlapping Event Conflict A - Today 2:00 PM to 3:00 PM
            today.set(Calendar.HOUR_OF_DAY, 14)
            today.set(Calendar.MINUTE, 0)
            val startTimeConflictA = today.timeInMillis
            today.set(Calendar.HOUR_OF_DAY, 15)
            today.set(Calendar.MINUTE, 0)
            val endTimeConflictA = today.timeInMillis

            sampleEvents.add(
                EventEntity(
                    title = "Product Strategy Committee",
                    description = "Annual roadmap discussion and resource allocation layout.",
                    startTime = startTimeConflictA,
                    endTime = endTimeConflictA,
                    location = "Executive Boardroom",
                    category = "Work",
                    platform = "Google Calendar",
                    participants = "Marcus Aurelius (COO), David Goggins (VP)",
                    documents = "Roadmap Proposals Slide Deck.pdf",
                    meetingUrl = "",
                    isRecurring = false,
                    travelTimeMinutes = 15 // Needs 15 mins to get to Boardroom
                )
            )

            // 5. Overlapping Event Conflict B - Today 2:30 PM to 3:30 PM (CONFLICTING!)
            today.set(Calendar.HOUR_OF_DAY, 14)
            today.set(Calendar.MINUTE, 30)
            val startTimeConflictB = today.timeInMillis
            today.set(Calendar.HOUR_OF_DAY, 15)
            today.set(Calendar.MINUTE, 30)
            val endTimeConflictB = today.timeInMillis

            sampleEvents.add(
                EventEntity(
                    title = "Tech Debt Review & Refactor Planning",
                    description = "Addressing legacy database schemas. Urgent meeting requested by engineering lead.",
                    startTime = startTimeConflictB,
                    endTime = endTimeConflictB,
                    location = "Google Meet",
                    category = "Work",
                    platform = "Outlook",
                    participants = "Jane Eng Lead, Alex Dev",
                    documents = "Schema Migrations Draft.sql",
                    meetingUrl = "https://meet.google.com/abc-defg-hij",
                    isRecurring = false,
                    travelTimeMinutes = 0
                )
            )

            // 6. On-site Partner Presentation - Tomorrow 2:00 PM to 3:30 PM
            tomorrow.set(Calendar.HOUR_OF_DAY, 14)
            tomorrow.set(Calendar.MINUTE, 0)
            val startTimeTomorrow1 = tomorrow.timeInMillis
            tomorrow.set(Calendar.HOUR_OF_DAY, 15)
            tomorrow.set(Calendar.MINUTE, 30)
            val endTimeTomorrow1 = tomorrow.timeInMillis

            sampleEvents.add(
                EventEntity(
                    title = "🚀 On-site Client Proposal Pitch",
                    description = "Pitching the SmartSync enterprise planner SDK suite. High stake client meeting.",
                    startTime = startTimeTomorrow1,
                    endTime = endTimeTomorrow1,
                    location = "Infinity HQ Office (Downtown)",
                    category = "Travel",
                    platform = "Google Calendar",
                    participants = "CEO of Client Corp, VP Marketing",
                    documents = "Enterprise Pitch deck v3, Demo Video mp4",
                    meetingUrl = "",
                    isRecurring = false,
                    travelTimeMinutes = 35 // AI estimates 35 minutes of travel time!
                )
            )

            // Past Completed Meeting: Q3 Planning meeting - Yesterday 11:00 AM to 12:30 PM
            val yesterday = calendar.clone() as Calendar
            yesterday.add(Calendar.DAY_OF_YEAR, -1)
            yesterday.set(Calendar.HOUR_OF_DAY, 11)
            yesterday.set(Calendar.MINUTE, 0)
            val startTimeYesterday = yesterday.timeInMillis
            yesterday.set(Calendar.HOUR_OF_DAY, 12)
            yesterday.set(Calendar.MINUTE, 30)
            val endTimeYesterday = yesterday.timeInMillis

            sampleEvents.add(
                EventEntity(
                    title = "Q3 Roadmap Planning & OKR Alignment",
                    description = "Comprehensive alignment session to finalize our Q3 engineering, product design, and marketing key results. Evaluated capacity planning and resource bottlenecks.",
                    startTime = startTimeYesterday,
                    endTime = endTimeYesterday,
                    location = "HQ Main Conference Room",
                    category = "Work",
                    platform = "Slack",
                    participants = "Jane DevLead, Sarah Jenkins (PM), Dave Miller (QA), John Doe (Designer), Sachin John (CEO)",
                    documents = "OKR Proposal Sheet draft, Capacity Limits v1.0",
                    meetingUrl = "https://meet.google.com/q3-roadmap-align",
                    isRecurring = false,
                    travelTimeMinutes = 0
                )
            )

            // Insert all sample events
            dao.insertEvents(sampleEvents)
        }

        // Tasks prepopulation
        val existingTasks = allTasks.firstOrNull() ?: emptyList()
        if (existingTasks.isEmpty()) {
            val sampleTasks = listOf(
                TaskEntity(
                    title = "Prepare pitch deck for Infinity HQ Pitch",
                    description = "Make sure slides cover security aspects of integration APIs and credential storage.",
                    deadline = tomorrow.timeInMillis - (3 * 3600 * 1000), // 3 hours before meeting tomorrow
                    priority = "High",
                    platform = "Notion",
                    isCompleted = false,
                    category = "Work"
                ),
                TaskEntity(
                    title = "Resolve SQLite Room migration bugs",
                    description = "Add fallbackToDestructiveMigration or proper migration logic to schema v2.",
                    deadline = today.timeInMillis + (6 * 3600 * 1000), // tonight
                    priority = "High",
                    platform = "Todoist",
                    isCompleted = false,
                    category = "Work"
                ),
                TaskEntity(
                    title = "Meditation & Focus Hour setup",
                    description = "Set up Slack Do Not Disturb auto-triggers for focus blocks.",
                    deadline = today.timeInMillis + (24 * 3600 * 1000), // tomorrow
                    priority = "Medium",
                    platform = "Slack",
                    isCompleted = false,
                    category = "Personal"
                ),
                TaskEntity(
                    title = "Review Weekly Workout Schedule",
                    description = "Ensure study or work blocks don't push gym sessions into late night.",
                    deadline = today.timeInMillis + (3 * 24 * 3600 * 1000), // in 3 days
                    priority = "Low",
                    platform = "Local",
                    isCompleted = true,
                    category = "Health"
                )
            )
            for (task in sampleTasks) {
                dao.insertTask(task)
            }
        }

        // Notifications prepopulation
        val existingNotifications = allNotifications.firstOrNull() ?: emptyList()
        if (existingNotifications.isEmpty()) {
            val sampleNotifications = listOf(
                NotificationEntity(
                    title = "⚠️ Calendar Overlap Detected",
                    message = "'Product Strategy Committee' overlaps with 'Tech Debt Review' at 2:30 PM. Tap to resolve.",
                    timestamp = System.currentTimeMillis() - 10 * 60 * 1000,
                    isRead = false,
                    type = "Alert"
                ),
                NotificationEntity(
                    title = "💡 Reschedule Recommendation",
                    message = "AI recommends rescheduling 'Tech Debt Review' to 3:30 PM. All participants are free then.",
                    timestamp = System.currentTimeMillis() - 8 * 60 * 1000,
                    isRead = false,
                    type = "Suggestion"
                ),
                NotificationEntity(
                    title = "📋 Meeting Preparation Card Ready",
                    message = "Standup documents and participant bios are aggregated for 'Daily Team Standup'. Check preparation pane.",
                    timestamp = System.currentTimeMillis() - 60 * 60 * 1000,
                    isRead = false,
                    type = "Prep"
                )
            )
            for (notification in sampleNotifications) {
                dao.insertNotification(notification)
            }
        }

        // Preferences prepopulation
        val existingPref = preferences.firstOrNull()
        if (existingPref == null) {
            val samplePref = PreferencesEntity(
                id = 1,
                timeZone = "EDT (UTC-4)",
                workingHourStart = 9,
                workingHourEnd = 17,
                dailySummary = "Your morning is looking productive! You have 3 meetings, including 1 calendar conflict in the afternoon. AI recommends dealing with the 'Tech Debt Review' meeting overlap before 1:00 PM. High-priority tasks are synced from Todoist and Notion.",
                learnPreferencesJson = "Preferred Focus Blocks: 10:00 AM - 12:00 PM. No lunch meetings. Preferred Meeting Length: 30 minutes."
            )
            dao.insertPreferences(samplePref)
        }
    }
}
