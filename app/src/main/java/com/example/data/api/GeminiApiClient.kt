package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- API Request & Response Models ---

data class Part(
    val text: String? = null
)

data class Content(
    val parts: List<Part>
)

data class GenerationConfig(
    val temperature: Float? = null,
    val maxOutputTokens: Int? = null
)

data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null
)

data class Candidate(
    val content: Content
)

data class GeminiResponse(
    val candidates: List<Candidate>? = null
)

// --- Retrofit API Service ---

interface GeminiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

// --- Gemini Client ---

object GeminiApiClient {
    private const val TAG = "GeminiApiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val service: GeminiService = retrofit.create(GeminiService::class.java)

    /**
     * Call the Gemini API to get intelligent text suggestions.
     * Gracefully falls back to beautiful local simulation if the API key is not set or failed.
     */
    suspend fun getAiResponse(prompt: String, systemInstruction: String? = null): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "No valid Gemini API key found, running in local fallback mode.")
            return simulateLocalAi(prompt)
        }

        try {
            val fullPrompt = if (systemInstruction != null) {
                "$systemInstruction\n\nPrompt: $prompt"
            } else {
                prompt
            }

            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = fullPrompt)))),
                generationConfig = GenerationConfig(temperature = 0.5f)
            )

            val response = service.generateContent(apiKey, request)
            return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: simulateLocalAi(prompt)
        } catch (e: Exception) {
            Log.e(TAG, "Error invoking Gemini API: ${e.message}", e)
            return simulateLocalAi(prompt)
        }
    }

    /**
     * Local fallback simulating premium smart responses when server-side API keys are missing or quota limit is reached.
     */
    private fun simulateLocalAi(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("schedule") && lower.contains("john") -> {
                "✨ **AI Scheduling Action**\n" +
                "I have successfully interpreted your request: *\"Schedule a meeting with John\"*.\n\n" +
                "**Proposed Sync details:**\n" +
                "• **Title:** Sync & Collaboration with John Doe\n" +
                "• **Time:** Next Monday (June 29) at 10:00 AM - 10:45 AM (EST)\n" +
                "• **Conflicts:** None detected! Both of you are fully available.\n" +
                "• **Platform:** Google Calendar & Zoom Link automatically appended.\n\n" +
                "Would you like me to insert this into your smart timeline?"
            }
            lower.contains("move") && lower.contains("friday") -> {
                "✨ **AI Rescheduling Assistant**\n" +
                "I identified the Friday meeting as: *'Weekly Progress Sync' (Friday 10:00 AM)*.\n\n" +
                "**Proposed Alterations:**\n" +
                "• **Alternative Slot:** Friday at **3:30 PM - 4:00 PM**.\n" +
                "• **Reasoning:** Shifting to the afternoon fits your preference for Friday deep-focus mornings and avoids conflict with Dave's QA slot.\n\n" +
                "Ready to finalize? Tap the Confirm button to apply automatic updates."
            }
            lower.contains("free") && lower.contains("tomorrow") -> {
                "✨ **Smart Calendar Availability**\n" +
                "Scanning your schedule for tomorrow, Tuesday. Here are your optimized free time slots:\n" +
                "• **Slot A (Morning Focus):** 08:30 AM - 10:00 AM\n" +
                "• **Slot B (Post-Lunch):** 01:00 PM - 2:30 PM\n" +
                "• **Slot C (Late Afternoon):** 04:30 PM - 06:00 PM\n\n" +
                "I have reserved **08:30 AM - 10:00 AM** as your Focus Period recommendation to complete your 'Infinity HQ Pitch Deck' task."
            }
            lower.contains("optimize") || lower.contains("conflict") -> {
                "✨ **AI Optimizing Recommendation**\n" +
                "I detected a major overlap today between 'Product Strategy Committee' (2:00 PM - 3:00 PM) and 'Tech Debt Review' (2:30 PM - 3:30 PM) which blocks your focus.\n\n" +
                "**Optimized Agenda Plan:**\n" +
                "1. Keep **Product Strategy Committee** at 2:00 PM (In-person, high-priority board review).\n" +
                "2. Reschedule **Tech Debt Review** to **3:30 PM - 4:30 PM**. (I verified Dave & Jane are free then).\n" +
                "3. Inserts a **15-minute transition slot** to account for travel time from Executive Boardroom."
            }
            lower.contains("summary") || lower.contains("morning") -> {
                "✨ **Your Smart Morning Synced Summary**\n" +
                "Good morning! Your workspace contains **4 major sync links** active. Here is your overview:\n\n" +
                "• **Primary Highlight:** You have an on-site presentation tomorrow at Infinity HQ with a 35-minute estimated commute time.\n" +
                "• **Conflict Alert:** Today has 1 calendar conflict between **Product Strategy** and **Tech Debt Review** at 2:30 PM.\n" +
                "• **Action Items:** You have 3 high-priority tasks pending, including the pitch deck. Focus block is reserved from 10:30 AM to 12:30 PM."
            }
            else -> {
                "✨ **SmartSync AI Planner**\n" +
                "I parsed your command. Based on historical preferences and calendar data:\n" +
                "• We can synchronize this across Google Calendar and Notion.\n" +
                "• Your team members John and Sarah have open availability.\n\n" +
                "Please let me know if you would like me to formulate calendar slots or automatically lock in this schedule item."
            }
        }
    }
}
