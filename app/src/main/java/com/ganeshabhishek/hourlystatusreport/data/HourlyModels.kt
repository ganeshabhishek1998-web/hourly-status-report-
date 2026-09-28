package com.ganeshabhishek.hourlystatusreport.data

import androidx.room.Entity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Locale
import kotlin.math.min

@Serializable
enum class SlotStatus(val key: String, val label: String) {
    COMPLETED("completed", "Completed"),
    IN_PROGRESS("in_progress", "In Progress"),
    PENDING("pending", "Pending"),
    BLOCKED("blocked", "Blocked");

    fun nextToggleStatus(): SlotStatus = when (this) {
        COMPLETED -> IN_PROGRESS
        IN_PROGRESS -> BLOCKED
        BLOCKED -> COMPLETED
        PENDING -> COMPLETED
    }

    companion object {
        fun fromKey(raw: String?): SlotStatus {
            val normalized = raw?.trim()?.lowercase(Locale.US)?.replace(" ", "_") ?: return PENDING
            return entries.find { it.key == normalized } ?: PENDING
        }
    }
}

@Serializable
data class HourlySlot(
    val id: String,
    val timeRange: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val isBreak: Boolean = false,
    val defaultTitle: String? = null,
    val activity: String = "",
    val status: SlotStatus = SlotStatus.PENDING,
    val durationHours: Double = 1.0
)

@Serializable
data class DailyHourlyReport(
    val id: String,
    val date: String, // YYYY-MM-DD
    val slots: List<HourlySlot>,
    val totalLoggedHours: Double,
    val targetHours: Double = 12.0,
    val updatedAt: String
) {
    val formattedLoggedHours: String
        get() = if (totalLoggedHours % 1.0 == 0.0) {
            totalLoggedHours.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", totalLoggedHours)
        }
}

@Entity(tableName = "hourly_slots", primaryKeys = ["date", "slotId"])
data class HourlySlotEntity(
    val date: String,
    val slotId: String,
    val sortOrder: Int,
    val timeRange: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val isBreak: Boolean,
    val defaultTitle: String?,
    val activity: String,
    val statusKey: String,
    val durationHours: Double,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): HourlySlot = HourlySlot(
        id = slotId,
        timeRange = timeRange,
        startHour = startHour,
        startMinute = startMinute,
        endHour = endHour,
        endMinute = endMinute,
        isBreak = isBreak,
        defaultTitle = defaultTitle,
        activity = activity,
        status = SlotStatus.fromKey(statusKey),
        durationHours = durationHours
    )
}

data class ImportedSlotItem(
    val timeRange: String? = null,
    val slotId: String? = null,
    val activity: String,
    val status: String? = null
)

object DefaultHourlySlots {
    val SLOTS: List<HourlySlot> = listOf(
        HourlySlot(
            id = "slot_1",
            timeRange = "9:00 – 10:00 AM",
            startHour = 9,
            startMinute = 0,
            endHour = 10,
            endMinute = 0,
            activity = "",
            status = SlotStatus.PENDING,
            durationHours = 1.0
        ),
        HourlySlot(
            id = "slot_2",
            timeRange = "10:00 – 11:00 AM",
            startHour = 10,
            startMinute = 0,
            endHour = 11,
            endMinute = 0,
            activity = "",
            status = SlotStatus.PENDING,
            durationHours = 1.0
        ),
        HourlySlot(
            id = "slot_3",
            timeRange = "11:00 AM – 12:00 PM",
            startHour = 11,
            startMinute = 0,
            endHour = 12,
            endMinute = 0,
            activity = "",
            status = SlotStatus.PENDING,
            durationHours = 1.0
        ),
        HourlySlot(
            id = "slot_4",
            timeRange = "12:00 – 1:00 PM",
            startHour = 12,
            startMinute = 0,
            endHour = 13,
            endMinute = 0,
            activity = "",
            status = SlotStatus.PENDING,
            durationHours = 1.0
        ),
        HourlySlot(
            id = "slot_5",
            timeRange = "1:00 – 1:30 PM",
            startHour = 13,
            startMinute = 0,
            endHour = 13,
            endMinute = 30,
            isBreak = true,
            defaultTitle = "Lunch break",
            activity = "Lunch break",
            status = SlotStatus.COMPLETED,
            durationHours = 0.5
        ),
        HourlySlot(
            id = "slot_6",
            timeRange = "1:30 – 2:30 PM",
            startHour = 13,
            startMinute = 30,
            endHour = 14,
            endMinute = 30,
            activity = "",
            status = SlotStatus.PENDING,
            durationHours = 1.0
        ),
        HourlySlot(
            id = "slot_7",
            timeRange = "2:30 – 3:30 PM",
            startHour = 14,
            startMinute = 30,
            endHour = 15,
            endMinute = 30,
            activity = "",
            status = SlotStatus.PENDING,
            durationHours = 1.0
        ),
        HourlySlot(
            id = "slot_8",
            timeRange = "3:30 – 4:30 PM",
            startHour = 15,
            startMinute = 30,
            endHour = 16,
            endMinute = 30,
            activity = "",
            status = SlotStatus.PENDING,
            durationHours = 1.0
        ),
        HourlySlot(
            id = "slot_9",
            timeRange = "4:30 – 5:30 PM",
            startHour = 16,
            startMinute = 30,
            endHour = 17,
            endMinute = 30,
            activity = "",
            status = SlotStatus.PENDING,
            durationHours = 1.0
        ),
        HourlySlot(
            id = "slot_10",
            timeRange = "5:30 – 6:30 PM",
            startHour = 17,
            startMinute = 30,
            endHour = 18,
            endMinute = 30,
            activity = "",
            status = SlotStatus.PENDING,
            durationHours = 1.0
        ),
        HourlySlot(
            id = "slot_11",
            timeRange = "6:30 – 7:30 PM",
            startHour = 18,
            startMinute = 30,
            endHour = 19,
            endMinute = 30,
            activity = "",
            status = SlotStatus.PENDING,
            durationHours = 1.0
        ),
        HourlySlot(
            id = "slot_12",
            timeRange = "7:30 – 8:30 PM",
            startHour = 19,
            startMinute = 30,
            endHour = 20,
            endMinute = 30,
            activity = "",
            status = SlotStatus.PENDING,
            durationHours = 1.0
        ),
        HourlySlot(
            id = "slot_13",
            timeRange = "8:30 – 9:00 PM",
            startHour = 20,
            startMinute = 30,
            endHour = 21,
            endMinute = 0,
            activity = "",
            status = SlotStatus.PENDING,
            durationHours = 0.5
        )
    )

    val SAMPLE_TEMPLATE_TASKS: Map<String, String> = mapOf(
        "slot_1" to "Morning standup, priority check-in, and Slack inbox triage",
        "slot_2" to "Feature architecture design and component API drafting",
        "slot_3" to "Core system logic engineering & test coverage",
        "slot_4" to "Cross-functional sync & client feedback review",
        "slot_5" to "Lunch break",
        "slot_6" to "Code review, PR revisions & CI pipeline checks",
        "slot_7" to "High-focus module development and database queries",
        "slot_8" to "Internal team alignment & staging deployment",
        "slot_9" to "Bug triage, performance tuning & latency reduction",
        "slot_10" to "Documentation update & customer onboarding setup",
        "slot_11" to "Production monitoring & release candidate verification",
        "slot_12" to "Daily retrospective and next-day roadmap sync",
        "slot_13" to "Shift handover log and desk wrap-up notes"
    )

    fun createEntitiesForDate(date: String): List<HourlySlotEntity> {
        val now = System.currentTimeMillis()
        return SLOTS.mapIndexed { index, slot ->
            HourlySlotEntity(
                date = date,
                slotId = slot.id,
                sortOrder = index,
                timeRange = slot.timeRange,
                startHour = slot.startHour,
                startMinute = slot.startMinute,
                endHour = slot.endHour,
                endMinute = slot.endMinute,
                isBreak = slot.isBreak,
                defaultTitle = slot.defaultTitle,
                activity = if (slot.isBreak) "Lunch break" else "",
                statusKey = if (slot.isBreak) SlotStatus.COMPLETED.key else SlotStatus.PENDING.key,
                durationHours = slot.durationHours,
                updatedAt = now
            )
        }
    }

    fun calculateLoggedHours(slots: List<HourlySlot>): Double {
        val raw = slots
            .filter { it.activity.trim().isNotEmpty() }
            .sumOf { it.durationHours }
        val rounded = Math.round(raw * 10.0) / 10.0
        return min(12.0, rounded)
    }
}

object ReportFormatUtils {
    private val jsonParser = Json { ignoreUnknownKeys = true }

    fun formatForCopy(report: DailyHourlyReport): String {
        val lines = mutableListOf(
            "Hourly Status Report (${report.formattedLoggedHours}/12 hours) - ${report.date}",
            "--------------------------------------------------",
            "Time                    Activity",
            "--------------------------------------------------"
        )
        report.slots.forEach { s ->
            val padTime = s.timeRange.padEnd(23, ' ')
            val act = s.activity.trim().ifEmpty { "(Pending)" }
            lines.add("$padTime $act")
        }
        lines.add("--------------------------------------------------")
        lines.add("Total Logged Hours: ${report.formattedLoggedHours} / 12 hours")
        return lines.joinToString("\n")
    }

    fun formatAsCsv(report: DailyHourlyReport): String {
        val headers = listOf("Time", "Activity", "Status", "Duration (Hours)", "Date")
        val rows = report.slots.map { s ->
            val escapedActivity = s.activity.replace("\"", "\"\"")
            listOf(
                "\"${s.timeRange}\"",
                "\"$escapedActivity\"",
                s.status.key,
                s.durationHours.toString(),
                report.date
            ).joinToString(",")
        }
        return (listOf(headers.joinToString(",")) + rows).joinToString("\n")
    }

    fun parseImportedText(text: String, fileName: String = ""): List<ImportedSlotItem> {
        val trimmedText = text.trim()
        if (trimmedText.isEmpty()) return emptyList()

        val parsedSlots = mutableListOf<ImportedSlotItem>()

        // 1. Try parsing as JSON
        if (fileName.endsWith(".json", ignoreCase = true) ||
            trimmedText.startsWith("{") ||
            trimmedText.startsWith("[")
        ) {
            try {
                val element: JsonElement = jsonParser.parseToJsonElement(trimmedText)
                val list: JsonArray? = when (element) {
                    is JsonArray -> element
                    is JsonObject -> element["slots"] as? JsonArray
                    else -> null
                }
                if (list != null) {
                    list.forEach { itemEl ->
                        val obj = itemEl as? JsonObject ?: return@forEach
                        val timeRange = obj["timeRange"]?.jsonPrimitive?.content
                            ?: obj["time"]?.jsonPrimitive?.content
                        val slotId = obj["slotId"]?.jsonPrimitive?.content
                            ?: obj["id"]?.jsonPrimitive?.content
                        val activity = obj["activity"]?.jsonPrimitive?.content
                            ?: obj["task"]?.jsonPrimitive?.content
                            ?: ""
                        val status = obj["status"]?.jsonPrimitive?.content ?: "completed"
                        parsedSlots.add(
                            ImportedSlotItem(
                                timeRange = timeRange,
                                slotId = slotId,
                                activity = activity,
                                status = status
                            )
                        )
                    }
                }
            } catch (_: Exception) {
                // Fall through to CSV / plain text parsing
            }
        }

        // 2. Try parsing as CSV or human-readable report lines
        if (parsedSlots.isEmpty()) {
            val rawLines = trimmedText
                .split(Regex("\\r?\\n"))
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            val isCsv = rawLines.any { it.contains(",") && !it.startsWith("---") }
            if (isCsv) {
                val rows = rawLines.map { parseCsvRow(it) }
                val headerRow = rows.firstOrNull().orEmpty()
                val timeCol = headerRow.indexOfFirst { it.contains("time", ignoreCase = true) }
                val actCol = headerRow.indexOfFirst {
                    Regex("activity|task|description|log", RegexOption.IGNORE_CASE).containsMatchIn(it)
                }
                val statusCol = headerRow.indexOfFirst { it.contains("status", ignoreCase = true) }

                val dataRows = if (timeCol != -1 || actCol != -1) rows.drop(1) else rows
                dataRows.forEach { cols ->
                    val timeRange = (if (timeCol != -1) cols.getOrNull(timeCol) else cols.getOrNull(0)).orEmpty()
                    val activity = (if (actCol != -1) cols.getOrNull(actCol) else cols.getOrNull(1)).orEmpty()
                    val status = (if (statusCol != -1) cols.getOrNull(statusCol) else cols.getOrNull(2))
                        ?.ifEmpty { "completed" } ?: "completed"
                    if (activity.isNotEmpty() || timeRange.isNotEmpty()) {
                        parsedSlots.add(
                            ImportedSlotItem(
                                timeRange = timeRange,
                                activity = activity,
                                status = status
                            )
                        )
                    }
                }
            } else {
                val lineRegex = Regex(
                    "^(\\d{1,2}:\\d{2}\\s*(?:AM|PM)?\\s*[\\u2013\\u2014-]\\s*\\d{1,2}:\\d{2}\\s*(?:AM|PM))\\s+(.*)$",
                    RegexOption.IGNORE_CASE
                )
                for (line in rawLines) {
                    if (line.startsWith("Hourly Status Report") ||
                        line.startsWith("---") ||
                        line.startsWith("Time ") ||
                        line.startsWith("Total Logged")
                    ) {
                        continue
                    }
                    val match = lineRegex.find(line)
                    if (match != null) {
                        val timeRange = match.groupValues[1].trim()
                        val activity = match.groupValues[2].trim()
                        val cleanActivity = if (activity.equals("(Pending)", ignoreCase = true)) "" else activity
                        parsedSlots.add(
                            ImportedSlotItem(
                                timeRange = timeRange,
                                activity = cleanActivity,
                                status = if (cleanActivity.isNotEmpty()) "completed" else "pending"
                            )
                        )
                    } else if (line.isNotEmpty()) {
                        parsedSlots.add(
                            ImportedSlotItem(
                                activity = line,
                                status = "completed"
                            )
                        )
                    }
                }
            }
        }

        return parsedSlots
    }

    private fun parseCsvRow(line: String): List<String> {
        val res = mutableListOf<String>()
        val cur = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        cur.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                ch == ',' && !inQuotes -> {
                    res.add(cur.toString().trim())
                    cur.clear()
                }
                else -> cur.append(ch)
            }
            i++
        }
        res.add(cur.toString().trim())
        return res
    }
}
