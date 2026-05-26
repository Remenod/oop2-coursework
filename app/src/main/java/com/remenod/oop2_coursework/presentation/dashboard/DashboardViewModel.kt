package com.remenod.oop2_coursework.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remenod.oop2_coursework.domain.model.Discipline
import com.remenod.oop2_coursework.domain.model.WorkItem
import com.remenod.oop2_coursework.domain.model.WorkStatus
import com.remenod.oop2_coursework.domain.repository.TaskRepository
import com.remenod.oop2_coursework.domain.service.AnalyticsService
import com.remenod.oop2_coursework.presentation.common.DateTimeUiFormatter
import kotlinx.coroutines.flow.*

class DashboardViewModel(
    private val repository: TaskRepository,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = repository.observeDisciplines()
        .map { disciplines ->
            if (disciplines.isEmpty()) {
                DashboardUiState(totalTasks = 0)
            } else {
                val statusCounts = analyticsService.countByStatus(disciplines)
                
                DashboardUiState(
                    totalTasks = analyticsService.countTotalTasks(disciplines),
                    activeTasks = (statusCounts[WorkStatus.CREATED] ?: 0) + 
                                  (statusCounts[WorkStatus.IN_PROGRESS] ?: 0) + 
                                  (statusCounts[WorkStatus.BLOCKED] ?: 0),
                    doneTasks = statusCounts[WorkStatus.DONE] ?: 0,
                    cancelledTasks = statusCounts[WorkStatus.CANCELLED] ?: 0,
                    overdueTasks = analyticsService.countOverdue(disciplines),
                    dueTodayTasks = analyticsService.countDueToday(disciplines),
                    dueThisWeekTasks = analyticsService.countDueThisWeek(disciplines),
                    averageProgress = analyticsService.calculateAverageProgress(disciplines),
                    totalEstimatedTimeText = DateTimeUiFormatter.estimatedTime(
                        analyticsService.calculateTotalEstimatedMinutes(disciplines)
                    ),
                    totalLoggedTimeText = DateTimeUiFormatter.estimatedTime(
                        analyticsService.calculateTotalLoggedMinutes(disciplines)
                    ),
                    disciplineSummaries = disciplines.map { it.toDashboardUiModel() },
                    atRiskTasks = analyticsService.getAtRiskItems(disciplines).take(5).map { it.toDashboardTaskUiModel(disciplines) },
                    highPriorityTasks = analyticsService.getHighPriorityActiveItems(disciplines).take(5).map { it.toDashboardTaskUiModel(disciplines) }
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState(isLoading = true)
        )

    private fun Discipline.toDashboardUiModel() = DisciplineDashboardUiModel(
        id = id,
        name = name,
        progress = getProgress(),
        activeTaskCount = workItems.count { it.status != WorkStatus.DONE && it.status != WorkStatus.CANCELLED },
        color = color
    )

    private fun WorkItem.toDashboardTaskUiModel(disciplines: List<Discipline>) = DashboardTaskUiModel(
        id = id,
        title = title,
        disciplineName = disciplines.find { d -> d.workItems.any { it.id == this.id } }?.name ?: "Unknown",
        deadlineText = DateTimeUiFormatter.formatDateTime(deadline),
        timeLeftText = DateTimeUiFormatter.timeLeft(deadline),
        priority = priority,
        status = status,
        progress = getProgress()
    )
}
