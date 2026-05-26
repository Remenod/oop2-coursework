package com.remenod.oop2_coursework.domain.service

import com.remenod.oop2_coursework.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class AnalyticsServiceTest {

    private val service = AnalyticsService()

    @Test
    fun testRecursiveAnalyticsCountsNestedItems() {
        val now = LocalDateTime.of(2026, 5, 27, 12, 0)
        val discipline = Discipline(1L, "OOP", "Teacher", 4, 0)
        val project = ProjectTask(10L, "Coursework", "D")
        val overdueChild = GenericTask(11L, "Old task", "D").apply {
            deadline = now.minusDays(1)
            estimatedMinutes = 60
            addLog(WorkLogEntry(1L, "Worked", minutesSpent = 30))
        }
        val cancelledChild = GenericTask(12L, "Cancelled", "D").apply {
            changeStatus(WorkStatus.CANCELLED)
            deadline = now.minusDays(2)
            estimatedMinutes = 120
        }
        val dueSoonChild = GenericTask(13L, "Soon", "D").apply {
            deadline = now.plusDays(2)
            priority = Priority.CRITICAL
            estimatedMinutes = 90
        }

        project.addSubTask(overdueChild)
        project.addSubTask(cancelledChild)
        project.addSubTask(dueSoonChild)
        discipline.addWorkItem(project)

        val disciplines = listOf(discipline)

        assertEquals(4, service.countTotalTasks(disciplines))
        assertEquals(1, service.countOverdue(disciplines, now))
        assertEquals(1, service.countDueThisWeek(disciplines, now))
        assertEquals(270, service.calculateTotalEstimatedMinutes(disciplines))
        assertEquals(30, service.calculateTotalLoggedMinutes(disciplines))
        assertEquals(1, service.getHighPriorityActiveItems(disciplines).size)
        assertTrue(service.getAtRiskItems(disciplines, now).any { it.id == overdueChild.id })
    }

    @Test
    fun testAverageProgressIgnoresCancelledItems() {
        val discipline = Discipline(1L, "OOP", "Teacher", 4, 0)
        val done = GenericTask(10L, "Done", "D").apply {
            complete()
        }
        val cancelled = GenericTask(11L, "Cancelled", "D").apply {
            changeStatus(WorkStatus.CANCELLED)
        }

        discipline.addWorkItem(done)
        discipline.addWorkItem(cancelled)

        assertEquals(1.0, service.calculateAverageProgress(listOf(discipline)), 0.01)
    }
}
