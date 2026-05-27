package com.remenod.oop2_coursework.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remenod.oop2_coursework.domain.model.Discipline
import com.remenod.oop2_coursework.domain.model.WorkItem
import com.remenod.oop2_coursework.domain.model.WorkStatus
import com.remenod.oop2_coursework.domain.repository.TaskRepository
import com.remenod.oop2_coursework.domain.service.AnalyticsService
import com.remenod.oop2_coursework.presentation.common.DateTimeUiFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class DashboardViewModel(
    private val repository: TaskRepository,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = repository.observeDisciplines()
        .map { disciplines -> disciplines.toDashboardUiState() }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = repository.getDisciplinesSnapshot().toDashboardUiState()
        )

    private fun List<Discipline>.toDashboardUiState(): DashboardUiState {
        if (isEmpty()) return DashboardUiState(totalTasks = 0)

        val statusCounts = analyticsService.countByStatus(this)
        val atRiskItems = analyticsService.getAtRiskItems(this).take(5)
        val atRiskIds = atRiskItems.map { it.id }.toSet()
        val highPriorityItems = analyticsService.getHighPriorityActiveItems(this)
            .filterNot { it.id in atRiskIds }
            .take(5)

        return DashboardUiState(
            totalTasks = analyticsService.countTotalTasks(this),
            activeTasks = (statusCounts[WorkStatus.CREATED] ?: 0) +
                    (statusCounts[WorkStatus.IN_PROGRESS] ?: 0) +
                    (statusCounts[WorkStatus.BLOCKED] ?: 0),
            doneTasks = statusCounts[WorkStatus.DONE] ?: 0,
            cancelledTasks = statusCounts[WorkStatus.CANCELLED] ?: 0,
            overdueTasks = analyticsService.countOverdue(this),
            dueTodayTasks = analyticsService.countDueToday(this),
            dueThisWeekTasks = analyticsService.countDueThisWeek(this),
            averageProgress = analyticsService.calculateAverageProgress(this),
            totalEstimatedTimeText = DateTimeUiFormatter.estimatedTime(
                analyticsService.calculateTotalEstimatedMinutes(this)
            ),
            totalLoggedTimeText = DateTimeUiFormatter.estimatedTime(
                analyticsService.calculateTotalLoggedMinutes(this)
            ),
            disciplineSummaries = map { it.toDashboardUiModel() },
            atRiskTasks = atRiskItems.map { it.toDashboardTaskUiModel(this) },
            highPriorityTasks = highPriorityItems.map { it.toDashboardTaskUiModel(this) }
        )
    }

    private fun Discipline.toDashboardUiModel() = DisciplineDashboardUiModel(
        id = id,
        name = name,
        progress = getProgress(),
        totalTaskCount = getAllWorkItemsRecursive().size,
        activeTaskCount = getAllWorkItemsRecursive().count { it.status != WorkStatus.DONE && it.status != WorkStatus.CANCELLED },
        overdueTaskCount = getOverdueItems().size,
        color = color
    )

    private fun WorkItem.toDashboardTaskUiModel(disciplines: List<Discipline>) = DashboardTaskUiModel(
        id = id,
        title = title,
        disciplineName = disciplines.find { d -> d.getAllWorkItemsRecursive().any { it.id == this.id } }?.name ?: "Unknown",
        deadlineText = DateTimeUiFormatter.formatDateTime(deadline),
        timeLeftText = DateTimeUiFormatter.timeLeft(deadline),
        priority = priority,
        status = status,
        progress = getProgress()
    )
}
