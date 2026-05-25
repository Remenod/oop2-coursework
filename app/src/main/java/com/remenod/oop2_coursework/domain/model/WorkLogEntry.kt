package com.remenod.oop2_coursework.domain.model

import java.time.LocalDateTime

data class WorkLogEntry(
    val id: Long,
    val itemId: Long,
    val timestamp: LocalDateTime,
    val minutesSpent: Int,
    val oldStatus: WorkStatus,
    val newStatus: WorkStatus,
    val progressPercent: Double,
    val comment: String
)
