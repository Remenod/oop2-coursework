package com.remenod.oop2_coursework.presentation.worklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remenod.oop2_coursework.domain.model.*
import com.remenod.oop2_coursework.domain.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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

    fun addTask(title: String, description: String, type: WorkItemType, priority: Priority, totalPages: Int?) {
        viewModelScope.launch {
            val item = when (type) {
                WorkItemType.PROJECT -> ProjectTask(0, title, description)
                WorkItemType.READING -> ReadingTask(0, title, description, totalPages = totalPages ?: 100)
                else -> GenericTask(0, title, description)
            }.apply { this.priority = priority }
            
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
            isOverdue = isOverdue()
        )
    }
}
