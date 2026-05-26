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

    private val _actionError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<WorkListUiState> = repository.observeDiscipline(disciplineId)
        .combine(_actionError) { discipline, actionError ->
            if (discipline == null) {
                WorkListUiState(error = "Discipline not found")
            } else {
                WorkListUiState(
                    disciplineName = discipline.name,
                    items = discipline.workItems.map { it.toCardUiModel() },
                    actionError = actionError
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WorkListUiState(isLoading = true)
        )

    fun clearActionError() {
        _actionError.value = null
    }

    fun addTask(result: WorkItemEditResult) {
        viewModelScope.launch {
            try {
                val item = WorkItemFactory.createFrom(result)
                repository.addRootWorkItem(disciplineId, item)
                _actionError.value = null
            } catch (e: Exception) {
                _actionError.value = e.message ?: "Could not add task"
            }
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
