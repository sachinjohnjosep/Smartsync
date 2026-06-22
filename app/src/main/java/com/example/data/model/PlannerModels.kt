package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val startTime: Long,
    val endTime: Long,
    val location: String,
    val category: String, // "Work", "Personal", "Study", "Health", "Travel"
    val platform: String, // "Google Calendar", "Outlook", "Gmail", "Zoom", "Teams", "Slack", "Notion", "Trello", "Todoist", "Apple Calendar"
    val participants: String, // comma-separated strings/emails
    val documents: String, // comma-separated titles or URLs
    val meetingUrl: String, // e.g., Zoom/Teams link
    val isRecurring: Boolean = false,
    val recurrenceRule: String = "",
    val travelTimeMinutes: Int = 0,
    val isConflictResolved: Boolean = false,
    val aiSummary: String? = null
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val deadline: Long,
    val priority: String, // "High", "Medium", "Low"
    val platform: String, // e.g., "Notion", "Todoist", "Trello", "Local"
    val isCompleted: Boolean = false,
    val category: String = "General"
)

@Entity(tableName = "connections")
data class ConnectionEntity(
    @PrimaryKey val platform: String, // Platform name as unique key
    val isConnected: Boolean,
    val email: String,
    val lastSynced: Long
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val type: String // "Alert", "Suggestion", "Prep"
)

@Entity(tableName = "preferences")
data class PreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val timeZone: String = "UTC",
    val workingHourStart: Int = 9, // 9 AM
    val workingHourEnd: Int = 17, // 5 PM
    val dailySummary: String = "",
    val learnPreferencesJson: String = "", // Preferred hours or meeting types learned by AI
    val isUserRegistered: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userName: String = "",
    val userEmail: String = "",
    val userPasswordHex: String = ""
)
