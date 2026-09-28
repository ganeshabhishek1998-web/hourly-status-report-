package com.ganeshabhishek.hourlystatusreport.network

import com.ganeshabhishek.hourlystatusreport.BuildConfig
import com.ganeshabhishek.hourlystatusreport.data.DailyHourlyReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.Locale
import java.util.concurrent.TimeUnit

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val role: String? = null,
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null
)

@Serializable
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate> = emptyList()
)

@Serializable
data class Candidate(
    val content: Content? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

data class AiWriteDownAction(
    val slotId: String,
    val slotTimeRange: String,
    val activity: String
)

data class AiChatResult(
    val reply: String,
    val writtenDown: AiWriteDownAction? = null
)

object GeminiAiAssistant {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private val apiService: GeminiApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun processChatMessage(
        message: String,
        currentSlotId: String?,
        currentReport: DailyHourlyReport
    ): AiChatResult = withContext(Dispatchers.IO) {
        val trimmed = message.trim()
        val lower = trimmed.lowercase(Locale.US)

        val resolvedSlot = detectReferencedSlot(lower, currentReport, currentSlotId)
        val targetSlotId = resolvedSlot.id

        val isWriteDownIntent =
            lower.contains("write it down") ||
                lower.contains("write down") ||
                lower.contains("record this") ||
                lower.contains("i'm just saying") ||
                lower.contains("just saying") ||
                lower.contains("it's a machine") ||
                lower.startsWith("note:") ||
                lower.startsWith("log:")

        var extractedText = ""
        when {
            lower.contains("it's a machine") -> {
                extractedText = "It's a machine"
            }
            lower.contains("i'm just saying, write it down") ||
                lower.contains("i'm just saying write it down") -> {
                extractedText = "I'm just saying, write it down."
            }
            lower.contains("write it down") || lower.contains("write down") -> {
                val parts = trimmed.split(Regex("write (?:it )?down(?::)?", RegexOption.IGNORE_CASE), limit = 2)
                extractedText = parts.getOrNull(1)?.trim().orEmpty().ifEmpty { trimmed }
            }
            lower.contains("i'm just saying") -> {
                val parts = trimmed.split(Regex("i'm just saying(?::|,)?", RegexOption.IGNORE_CASE), limit = 2)
                extractedText = parts.getOrNull(1)?.trim().orEmpty().ifEmpty { trimmed }
            }
            lower.startsWith("note:") || lower.startsWith("log:") -> {
                extractedText = trimmed.substringAfter(":").trim().ifEmpty { trimmed }
            }
        }

        val activityToWrite = extractedText.ifEmpty {
            trimmed.replace(Regex("^(?:please )?write (?:it )?down(?::)?\\s*", RegexOption.IGNORE_CASE), "")
                .trim()
                .ifEmpty { trimmed }
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasValidApiKey = apiKey.isNotBlank() &&
            apiKey != "YOUR_GEMINI_API_KEY" &&
            apiKey != "your_api_key_here" &&
            apiKey != "null"

        if (hasValidApiKey) {
            try {
                val systemInstruction = """
                    You are the AI Workday Assistant for the "Hourly Status Report (0/12 hours)" system.
                    The user works a 12-hour schedule from 9:00 AM to 9:00 PM across these slots:
                    1. 9:00 – 10:00 AM (slot_1)
                    2. 10:00 – 11:00 AM (slot_2)
                    3. 11:00 AM – 12:00 PM (slot_3)
                    4. 12:00 – 1:00 PM (slot_4)
                    5. 1:00 – 1:30 PM Lunch break (slot_5)
                    6. 1:30 – 2:30 PM (slot_6)
                    7. 2:30 – 3:30 PM (slot_7)
                    8. 3:30 – 4:30 PM (slot_8)
                    9. 4:30 – 5:30 PM (slot_9)
                    10. 5:30 – 6:30 PM (slot_10)
                    11. 6:30 – 7:30 PM (slot_11)
                    12. 7:30 – 8:30 PM (slot_12)
                    13. 8:30 – 9:00 PM Day wrap-up (slot_13)

                    When the user tells you what they are doing, or says "write it down", or says "I'm just saying...", your job is to:
                    1. Identify the activity description to write down into the report.
                    2. If they specified a time (e.g. 10:00 AM or slot_2), use that slot. Otherwise use current slot ($targetSlotId - ${resolvedSlot.timeRange}).
                    3. Return a concise, helpful response confirming what was written down.
                """.trimIndent()

                val promptText = """
                    User says: "$trimmed"
                    Current active slot: $targetSlotId (${resolvedSlot.timeRange})
                    Is write-down intent: $isWriteDownIntent

                    If the user wants you to write something down (for example "It's a machine" or a task description), formulate the exact text to log and acknowledge it clearly in 1-2 short sentences.
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(Content(role = "user", parts = listOf(Part(text = promptText)))),
                    generationConfig = GenerationConfig(temperature = 0.6f),
                    systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
                )

                val response = apiService.generateContent(apiKey, request)
                val aiReply = response.candidates.firstOrNull()
                    ?.content?.parts?.firstOrNull()?.text
                    ?.trim()
                    .orEmpty()
                    .ifEmpty { "Written down: \"$activityToWrite\" into ${resolvedSlot.timeRange}." }

                return@withContext AiChatResult(
                    reply = aiReply,
                    writtenDown = AiWriteDownAction(
                        slotId = resolvedSlot.id,
                        slotTimeRange = resolvedSlot.timeRange,
                        activity = activityToWrite
                    )
                )
            } catch (_: Exception) {
                // Fall through to deterministic handler
            }
        }

        AiChatResult(
            reply = "Written down! Logged \"$activityToWrite\" into your ${resolvedSlot.timeRange} slot.",
            writtenDown = AiWriteDownAction(
                slotId = resolvedSlot.id,
                slotTimeRange = resolvedSlot.timeRange,
                activity = activityToWrite
            )
        )
    }

    private fun detectReferencedSlot(
        lowerMessage: String,
        report: DailyHourlyReport,
        currentSlotId: String?
    ) = report.slots.find { slot ->
        lowerMessage.contains(slot.id)
    } ?: report.slots.find { slot ->
        !slot.isBreak && (
            (slot.startHour == 9 && Regex("\\b9(?::00)?\\s*am\\b").containsMatchIn(lowerMessage)) ||
                (slot.startHour == 10 && Regex("\\b10(?::00)?\\s*am\\b").containsMatchIn(lowerMessage)) ||
                (slot.startHour == 11 && Regex("\\b11(?::00)?\\s*am\\b").containsMatchIn(lowerMessage)) ||
                (slot.startHour == 12 && Regex("\\b12(?::00)?\\s*pm\\b").containsMatchIn(lowerMessage)) ||
                (slot.startHour == 13 && slot.startMinute == 30 && Regex("\\b1:30\\s*pm\\b").containsMatchIn(lowerMessage)) ||
                (slot.startHour == 14 && Regex("\\b2(?::30)?\\s*pm\\b").containsMatchIn(lowerMessage)) ||
                (slot.startHour == 15 && Regex("\\b3(?::30)?\\s*pm\\b").containsMatchIn(lowerMessage)) ||
                (slot.startHour == 16 && Regex("\\b4(?::30)?\\s*pm\\b").containsMatchIn(lowerMessage)) ||
                (slot.startHour == 17 && Regex("\\b5(?::30)?\\s*pm\\b").containsMatchIn(lowerMessage)) ||
                (slot.startHour == 18 && Regex("\\b6(?::30)?\\s*pm\\b").containsMatchIn(lowerMessage)) ||
                (slot.startHour == 19 && Regex("\\b7(?::30)?\\s*pm\\b").containsMatchIn(lowerMessage)) ||
                (slot.startHour == 20 && Regex("\\b8(?::30)?\\s*pm\\b").containsMatchIn(lowerMessage))
            )
    } ?: report.slots.find { it.id == currentSlotId && !it.isBreak }
        ?: report.slots.firstOrNull { !it.isBreak && it.activity.isBlank() }
        ?: report.slots.first()
}
