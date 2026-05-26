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
    val hasGitHubAttachment: Boolean,
    val hasLogs: Boolean,
    val deadlineText: String,
    val timeLeftText: String,
    val estimatedTimeText: String
)

data class WorkListUiState(
    val isLoading: Boolean = false,
    val disciplineName: String = "",
    val items: List<WorkItemCardUiModel> = emptyList(),
    val totalItems: Int = 0,
    val query: String = "",
    val typeFilter: WorkListTypeFilter = WorkListTypeFilter.ALL,
    val statusFilter: WorkStatus? = null,
    val priorityFilter: Priority? = null,
    val overdueOnly: Boolean = false,
    val githubOnly: Boolean = false,
    val withLogsOnly: Boolean = false,
    val sortOption: WorkListSortOption = WorkListSortOption.UPDATED,
    val error: String? = null,
    val actionError: String? = null
)

enum class WorkListTypeFilter(val label: String) {
    ALL("All"),
    GENERIC("Generic"),
    PROGRAMMING("Programming"),
    EXAM("Exam"),
    SEMINAR("Seminar"),
    READING("Reading"),
    PROJECT("Project")
}

enum class WorkListSortOption(val label: String) {
    DEADLINE("Deadline"),
    PRIORITY("Priority"),
    PROGRESS("Progress"),
    UPDATED("Updated")
}
