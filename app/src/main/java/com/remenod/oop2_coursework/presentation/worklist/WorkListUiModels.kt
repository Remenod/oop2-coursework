package com.remenod.oop2_coursework.presentation.worklist

import com.remenod.oop2_coursework.domain.model.Priority
import com.remenod.oop2_coursework.domain.model.WorkStatus

data class WorkItemCardUiModel(
    val id: Long,
    val title: String,
    val typeLabel: String,
    val priority: Priority,
    val status: WorkStatus,
    val progressPercent: Double,
    val progressExplanation: String,
    val isOverdue: Boolean,
    val deadlineText: String,
    val timeLeftText: String,
    val estimatedTimeText: String
)

data class WorkListUiState(
    val isLoading: Boolean = false,
    val disciplineName: String = "",
    val items: List<WorkItemCardUiModel> = emptyList(),
    val error: String? = null
)
