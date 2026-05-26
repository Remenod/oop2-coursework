package com.remenod.oop2_coursework.presentation.workdetail

import com.remenod.oop2_coursework.domain.model.Priority
import com.remenod.oop2_coursework.domain.model.WorkStatus

data class WorkDetailUiState(
    val isLoading: Boolean = false,
    val item: WorkItemDetailUiModel? = null,
    val error: String? = null
)

data class WorkItemDetailUiModel(
    val id: Long,
    val title: String,
    val description: String,
    val status: WorkStatus,
    val priority: Priority,
    val typeName: String,
    val deadline: String,
    val progressPercent: Double,
    val progressExplanation: String,
    val canBeCompleted: Boolean,
    val checklist: List<ChecklistUiModel> = emptyList(),
    val subTasks: List<SubTaskUiModel> = emptyList()
)

data class ChecklistUiModel(
    val text: String,
    val isCompleted: Boolean
)

data class SubTaskUiModel(
    val id: Long,
    val title: String,
    val progress: Double,
    val isDone: Boolean
)
