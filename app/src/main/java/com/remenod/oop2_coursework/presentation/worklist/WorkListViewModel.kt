package com.remenod.oop2_coursework.presentation.worklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remenod.oop2_coursework.domain.model.*
import com.remenod.oop2_coursework.domain.repository.TaskRepository
import com.remenod.oop2_coursework.presentation.common.DateTimeUiFormatter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class WorkListViewModel(
    private val repository: TaskRepository,
    private val disciplineId: Long
) : ViewModel() {

    val uiState: StateFlow<WorkListUiState> = repository.observeDiscipline(disciplineId)
        .map { discipline ->
            if (discipline == null) {
                WorkListUiState(error = "Discipline not found")
            } else {
                WorkListUiState(
                    disciplineName = discipline.name,
                    items = discipline.workItems.map { it.toCardUiModel() }
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WorkListUiState(isLoading = true)
        )

    fun addTask(result: WorkItemEditResult) {
        viewModelScope.launch {
            val item = when (result.type) {
                WorkItemType.PROJECT -> ProjectTask(0, result.title, result.description)
                WorkItemType.READING -> ReadingTask(
                    id = 0,
                    title = result.title,
                    description = result.description,
                    totalPages = result.totalPages ?: 100
                )
                WorkItemType.PROGRAMMING -> ProgrammingTask(
                    id = 0,
                    title = result.title,
                    description = result.description,
                    commitsCount = result.commitsCount ?: 0,
                    requiredCommits = result.requiredCommits ?: 5,
                    issuesResolved = result.issuesResolved ?: 0,
                    requiredIssues = result.requiredIssues ?: 2,
                    testsPassed = result.testsPassed ?: 0.0
                )
                WorkItemType.EXAM -> ExamTask(0, result.title, result.description)
                WorkItemType.SEMINAR -> SeminarTask(0, result.title, result.description)
                else -> GenericTask(0, result.title, result.description)
            }.apply {
                this.priority = result.priority
                this.status = result.status
                this.deadline = result.deadline
                this.estimatedMinutes = result.estimatedMinutes
                this.touch()
            }
            
            repository.addRootWorkItem(disciplineId, item)
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteWorkItem(id)
        }
    }

    private fun WorkItem.toCardUiModel(): WorkItemCardUiModel {
        val progress = getProgressSnapshot()
        return WorkItemCardUiModel(
            id = id,
            title = title,
            typeLabel = when (this) {
                is ProgrammingTask -> "Programming"
                is ExamTask -> "Exam"
                is SeminarTask -> "Seminar"
                is ReadingTask -> "Reading"
                is ProjectTask -> "Project"
                else -> "Generic"
            },
            priority = priority,
            status = status,
            progressPercent = progress.percent,
            progressExplanation = progress.explanation,
            isOverdue = isOverdue(),
            deadlineText = DateTimeUiFormatter.formatDateTime(deadline),
            timeLeftText = DateTimeUiFormatter.timeLeft(deadline),
            estimatedTimeText = DateTimeUiFormatter.estimatedTime(estimatedMinutes)
        )
    }
}
