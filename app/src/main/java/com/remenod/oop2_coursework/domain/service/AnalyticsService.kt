package com.remenod.oop2_coursework.domain.service

import com.remenod.oop2_coursework.domain.model.Discipline
import com.remenod.oop2_coursework.domain.model.WorkItem
import com.remenod.oop2_coursework.domain.model.WorkLogEntry
import java.time.LocalDateTime

class AnalyticsService {

    fun calculateDisciplineLoad(discipline: Discipline): Double {
        // TODO: Calculate load based on estimatedMinutes of all items
        return 0.0
    }

    fun findOverdueItems(items: List<WorkItem>): List<WorkItem> {
        return items.filter { it.isOverdue() }
    }

    fun calculateProductivity(logs: List<WorkLogEntry>): Double {
        // TODO: Calculate productivity based on progress change per minute spent
        return 0.0
    }

    fun predictDeadlineRisk(item: WorkItem): Double {
        // TODO: Logic to predict if task will be overdue based on current progress and time left
        return 0.0
    }
}
