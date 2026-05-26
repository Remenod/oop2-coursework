package com.remenod.oop2_coursework.presentation.dashboard

import com.remenod.oop2_coursework.domain.model.Priority
import com.remenod.oop2_coursework.domain.model.WorkStatus

data class DashboardUiState(
    val isLoading: Boolean = false,
    val totalTasks: Int = 0,
    val activeTasks: Int = 0,
    val doneTasks: Int = 0,
    val cancelledTasks: Int = 0,
    val overdueTasks: Int = 0,
    val dueTodayTasks: Int = 0,
    val dueThisWeekTasks: Int = 0,
    val averageProgress: Double = 0.0,
    val totalEstimatedTimeText: String = "0m",
    val totalLoggedTimeText: String = "0m",
    val disciplineSummaries: List<DisciplineDashboardUiModel> = emptyList(),
    val atRiskTasks: List<DashboardTaskUiModel> = emptyList(),
    val highPriorityTasks: List<DashboardTaskUiModel> = emptyList(),
    val error: String? = null
)

data class DisciplineDashboardUiModel(
    val id: Long,
    val name: String,
    val progress: Double,
    val activeTaskCount: Int,
    val color: Int
)

data class DashboardTaskUiModel(
    val id: Long,
    val title: String,
    val disciplineName: String,
    val deadlineText: String,
    val timeLeftText: String,
    val priority: Priority,
    val status: WorkStatus,
    val progress: Double
)
