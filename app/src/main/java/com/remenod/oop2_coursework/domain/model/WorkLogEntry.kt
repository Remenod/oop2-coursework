package com.remenod.oop2_coursework.domain.model

import java.time.LocalDateTime

data class WorkLogEntry(
    val id: Long,
    val message: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val minutesSpent: Int = 0
)
