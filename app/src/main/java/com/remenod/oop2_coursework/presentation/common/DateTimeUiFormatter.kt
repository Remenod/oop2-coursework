package com.remenod.oop2_coursework.presentation.common

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeUiFormatter {
    private val displayFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    private val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    fun formatDateTime(value: LocalDateTime?): String {
        return value?.format(displayFormatter) ?: "No deadline"
    }

    fun formatInput(value: LocalDateTime?): String {
        return value?.format(inputFormatter) ?: ""
    }

    fun isValidInput(value: String): Boolean {
        if (value.isBlank()) return true
        return parseInput(value) != null
    }

    fun parseInput(value: String): LocalDateTime? {
        if (value.isBlank()) return null
        return runCatching {
            LocalDateTime.parse(value.trim(), inputFormatter)
        }.getOrNull()
    }

    fun timeLeft(deadline: LocalDateTime?, now: LocalDateTime = LocalDateTime.now()): String {
        if (deadline == null) return "No deadline"

        val duration = Duration.between(now, deadline)
        val overdue = duration.isNegative

        val abs = duration.abs()
        val days = abs.toDays()
        val hours = abs.toHours() % 24
        val minutes = abs.toMinutes() % 60

        val text = when {
            days > 0 -> "${days}d ${hours}h"
            abs.toHours() > 0 -> "${abs.toHours()}h ${minutes}m"
            else -> "${abs.toMinutes()}m"
        }

        return if (overdue) "Overdue by $text" else "$text left"
    }

    fun estimatedTime(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h > 0 && m > 0 -> "${h}h ${m}m"
            h > 0 -> "${h}h"
            else -> "${m}m"
        }
    }
}
