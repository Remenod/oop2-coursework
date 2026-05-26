package com.remenod.oop2_coursework.presentation.worklist

import com.remenod.oop2_coursework.domain.model.Priority
import com.remenod.oop2_coursework.domain.model.WorkItemType
import com.remenod.oop2_coursework.domain.model.WorkStatus
import java.time.LocalDateTime

data class WorkItemEditResult(
    val title: String,
    val description: String,
    val type: WorkItemType,
    val status: WorkStatus,
    val priority: Priority,
    val deadline: LocalDateTime?,
    val estimatedMinutes: Int,

    val totalPages: Int? = null,
    val readPages: Int? = null,

    val commitsCount: Int? = null,
    val requiredCommits: Int? = null,
    val issuesResolved: Int? = null,
    val requiredIssues: Int? = null,
    val testsPassed: Double? = null
)
