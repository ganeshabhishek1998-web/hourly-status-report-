package com.ganeshabhishek.hourlystatusreport

import com.ganeshabhishek.hourlystatusreport.data.DailyHourlyReport
import com.ganeshabhishek.hourlystatusreport.data.DefaultHourlySlots
import com.ganeshabhishek.hourlystatusreport.data.ReportFormatUtils
import com.ganeshabhishek.hourlystatusreport.data.SlotStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HourlyReportLogicTest {

    @Test
    fun testSlotCountAndScheduleWindow() {
        val slots = DefaultHourlySlots.SLOTS
        assertEquals("There should be 13 slots covering 9:00 AM to 9:00 PM", 13, slots.size)

        val firstSlot = slots.first()
        assertEquals("9:00 – 10:00 AM", firstSlot.timeRange)
        assertEquals(9, firstSlot.startHour)

        val lunchSlot = slots.find { it.isBreak }
        assertNotNull("Lunch break slot must exist", lunchSlot)
        assertEquals("slot_5", lunchSlot?.id)
        assertEquals("1:00 – 1:30 PM", lunchSlot?.timeRange)
        assertEquals(0.5, lunchSlot?.durationHours ?: 0.0, 0.01)

        val lastSlot = slots.last()
        assertEquals("8:30 – 9:00 PM", lastSlot.timeRange)
        assertEquals(21, lastSlot.endHour)
    }

    @Test
    fun testCalculateLoggedHours() {
        val baseSlots = DefaultHourlySlots.SLOTS
        // Only lunch is completed initially
        val initialHours = DefaultHourlySlots.calculateLoggedHours(baseSlots)
        assertEquals(0.0, initialHours, 0.01) // non-break slots have empty activity

        val withLunchActivity = baseSlots.map {
            if (it.isBreak) it.copy(activity = "Lunch break") else it
        }
        assertEquals(0.5, DefaultHourlySlots.calculateLoggedHours(withLunchActivity), 0.01)

        val allFilled = baseSlots.map {
            it.copy(activity = "Task completed")
        }
        val fullHours = DefaultHourlySlots.calculateLoggedHours(allFilled)
        assertEquals(12.0, fullHours, 0.01)
    }

    @Test
    fun testStatusTransitions() {
        assertEquals(SlotStatus.IN_PROGRESS, SlotStatus.COMPLETED.nextToggleStatus())
        assertEquals(SlotStatus.BLOCKED, SlotStatus.IN_PROGRESS.nextToggleStatus())
        assertEquals(SlotStatus.COMPLETED, SlotStatus.BLOCKED.nextToggleStatus())
        assertEquals(SlotStatus.COMPLETED, SlotStatus.PENDING.nextToggleStatus())
    }

    @Test
    fun testFormatForCopyAndCsv() {
        val slots = DefaultHourlySlots.SLOTS.mapIndexed { idx, s ->
            if (idx == 0) s.copy(activity = "Morning standup", status = SlotStatus.COMPLETED)
            else if (s.isBreak) s.copy(activity = "Lunch break", status = SlotStatus.COMPLETED)
            else s
        }
        val report = DailyHourlyReport(
            id = "rep_2026-09-28",
            date = "2026-09-28",
            slots = slots,
            totalLoggedHours = 1.5,
            targetHours = 12.0,
            updatedAt = ""
        )

        val copiedText = ReportFormatUtils.formatForCopy(report)
        assertTrue(copiedText.contains("Hourly Status Report (1.5/12 hours) - 2026-09-28"))
        assertTrue(copiedText.contains("Morning standup"))
        assertTrue(copiedText.contains("Total Logged Hours: 1.5 / 12 hours"))

        val csvText = ReportFormatUtils.formatAsCsv(report)
        assertTrue(csvText.startsWith("Time,Activity,Status,Duration (Hours),Date"))
        assertTrue(csvText.contains("\"9:00 – 10:00 AM\",\"Morning standup\",completed,1.0,2026-09-28"))
    }

    @Test
    fun testParseImportedReport() {
        val sampleText = """
            Time                    Activity
            --------------------------------------------------
            9:00 – 10:00 AM         Sprint ticket review
            10:00 – 11:00 AM        Architecture refactor
        """.trimIndent()

        val parsed = ReportFormatUtils.parseImportedText(sampleText)
        assertEquals(2, parsed.size)
        assertEquals("Sprint ticket review", parsed[0].activity)
        assertEquals("Architecture refactor", parsed[1].activity)
    }
}
