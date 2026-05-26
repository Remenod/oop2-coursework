package com.remenod.oop2_coursework.domain.service

import com.remenod.oop2_coursework.domain.model.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class AnalyticsService {

    fun getAllItems(disciplines: List<Discipline>): List<WorkItem> {
        return disciplines.flatMap { it.getAllWorkItemsRecursive() }
    }

    fun countTotalTasks(disciplines: List<Discipline>): Int {
        return getAllItems(disciplines).size
    }

    fun countByStatus(disciplines: List<Discipline>): Map<WorkStatus, Int> {
        return getAllItems(disciplines).groupingBy { it.status }.eachCount()
    }

    fun countOverdue(disciplines: List<Discipline>, now: LocalDateTime = LocalDateTime.now()): Int {
        return getAllItems(disciplines).count { it.isOverdue(now) }
    }

    fun countDueToday(disciplines: List<Discipline>, now: LocalDateTime = LocalDateTime.now()): Int {
        val today = now.toLocalDate()
        return getAllItems(disciplines).count { 
            it.deadline?.toLocalDate() == today && it.status != WorkStatus.DONE && it.status != WorkStatus.CANCELLED
        }
    }

    fun countDueThisWeek(disciplines: List<Discipline>, now: LocalDateTime = LocalDateTime.now()): Int {
        val nextWeek = now.plusDays(7)
        return getAllItems(disciplines).count { 
            val deadline = it.deadline
            deadline != null && deadline.isAfter(now) && deadline.isBefore(nextWeek) &&
                    it.status != WorkStatus.DONE && it.status != WorkStatus.CANCELLED
        }
    }

    fun calculateAverageProgress(disciplines: List<Discipline>): Double {
        val items = getAllItems(disciplines).filter { it.status != WorkStatus.CANCELLED }
        if (items.isEmpty()) return 0.0
        return items.map { it.getProgress() }.average()
    }

    fun calculateTotalEstimatedMinutes(disciplines: List<Discipline>): Int {
        return getAllItems(disciplines).sumOf { it.estimatedMinutes }
    }

    fun calculateTotalLoggedMinutes(disciplines: List<Discipline>): Int {
        return getAllItems(disciplines).flatMap { it.logs }.sumOf { it.minutesSpent }
    }

    fun getHighPriorityActiveItems(disciplines: List<Discipline>): List<WorkItem> {
        return getAllItems(disciplines).filter { 
            (it.priority == Priority.HIGH || it.priority == Priority.CRITICAL) && 
                    it.status != WorkStatus.DONE && it.status != WorkStatus.CANCELLED 
        }.sortedByDescending { it.priority }
    }

    fun getAtRiskItems(disciplines: List<Discipline>, now: LocalDateTime = LocalDateTime.now()): List<WorkItem> {
        return getAllItems(disciplines).filter { item ->
            val deadline = item.deadline
            if (deadline == null || item.status == WorkStatus.DONE || item.status == WorkStatus.CANCELLED) return@filter false
            
            val progress = item.getProgress()
            if (progress >= 1.0) return@filter false

            val timeToDeadline = Duration.between(now, deadline).toMinutes()
            if (timeToDeadline <= 0) return@filter true // Already overdue

            val estimatedRemaining = item.estimatedMinutes * (1.0 - progress)
            // Risk if remaining work is more than 80% of available time
            estimatedRemaining > (timeToDeadline * 0.8)
        }.sortedBy { it.deadline }
    }
}
