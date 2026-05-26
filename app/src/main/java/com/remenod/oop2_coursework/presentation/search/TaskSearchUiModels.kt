package com.remenod.oop2_coursework.presentation.search

import com.remenod.oop2_coursework.domain.model.AttachmentPurpose
import com.remenod.oop2_coursework.domain.model.Priority
import com.remenod.oop2_coursework.domain.model.WorkStatus

data class TaskSearchUiState(
    val isLoading: Boolean = false,
    val items: List<TaskSearchItemUiModel> = emptyList(),
    val totalItems: Int = 0,
    val query: String = "",
    val typeFilter: TaskSearchTypeFilter = TaskSearchTypeFilter.ALL,
    val statusFilter: WorkStatus? = null,
    val priorityFilter: Priority? = null,
    val attachmentPurposeFilter: AttachmentPurpose? = null,
    val overdueOnly: Boolean = false,
    val githubOnly: Boolean = false,
    val withLogsOnly: Boolean = false,
    val sortOption: TaskSearchSortOption = TaskSearchSortOption.UPDATED
)

data class TaskSearchItemUiModel(
    val id: Long,
    val disciplineId: Long,
    val disciplineName: String,
    val title: String,
    val typeLabel: String,
    val priority: Priority,
    val status: WorkStatus,
    val progressPercent: Double,
    val isOverdue: Boolean,
    val hasGitHubAttachment: Boolean,
    val hasLogs: Boolean,
    val deadlineText: String,
    val timeLeftText: String
)

enum class TaskSearchTypeFilter(val label: String) {
    ALL("All"),
    GENERIC("Generic"),
    PROGRAMMING("Programming"),
    EXAM("Exam"),
    SEMINAR("Seminar"),
    READING("Reading"),
    PROJECT("Project")
}

enum class TaskSearchSortOption(val label: String) {
    DEADLINE("Deadline"),
    PRIORITY("Priority"),
    PROGRESS("Progress"),
    UPDATED("Updated")
}
