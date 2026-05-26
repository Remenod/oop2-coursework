package com.remenod.oop2_coursework.presentation.workdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remenod.oop2_coursework.domain.model.*
import com.remenod.oop2_coursework.domain.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WorkDetailViewModel(
    private val repository: TaskRepository,
    private val workItemId: Long
) : ViewModel() {

    val uiState: StateFlow<WorkDetailUiState> = repository.observeWorkItem(workItemId)
        .map { item ->
            if (item == null) {
                WorkDetailUiState(error = "Task not found")
            } else {
                WorkDetailUiState(item = item.toDetailUiModel())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WorkDetailUiState(isLoading = true)
        )

    fun updateBasicInfo(title: String, description: String, priority: Priority) {
        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                item.title = title
                item.description = description
                item.priority = priority
                repository.updateWorkItem(item)
            }
        }
    }

    fun updateReadingProgress(readPages: Int, totalPages: Int) {
        if (totalPages <= 0 || readPages < 0 || readPages > totalPages) return

        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                if (item is ReadingTask) {
                    item.readPages = readPages
                    item.totalPages = totalPages
                    repository.updateWorkItem(item)
                }
            }
        }
    }

    fun addSubTask(title: String, description: String, type: WorkItemType, priority: Priority, totalPages: Int?) {
        viewModelScope.launch {
            val item = when (type) {
                WorkItemType.PROJECT -> ProjectTask(0, title, description)
                WorkItemType.READING -> ReadingTask(0, title, description, totalPages = totalPages ?: 100)
                else -> GenericTask(0, title, description)
            }.apply { this.priority = priority }
            
            repository.addSubTask(workItemId, item)
        }
    }

    fun deleteThisTask() {
        viewModelScope.launch {
            repository.deleteWorkItem(workItemId)
        }
    }

    fun completeTask() {
        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                try {
                    item.complete()
                    repository.updateWorkItem(item)
                } catch (_: Exception) {}
            }
        }
    }

    private fun WorkItem.toDetailUiModel(): WorkItemDetailUiModel {
        val snapshot = getProgressSnapshot()
        return WorkItemDetailUiModel(
            id = id,
            title = title,
            description = description,
            status = status,
            priority = priority,
            typeName = this::class.simpleName ?: "Task",
            deadline = deadline?.toString() ?: "No deadline",
            progressPercent = snapshot.percent,
            progressExplanation = snapshot.explanation,
            canBeCompleted = canBeCompleted(),
            checklist = if (this is AtomicWorkItem) {
                checklist.map { ChecklistUiModel(it.text, it.isCompleted) }
            } else emptyList(),
            subTasks = if (this is CompositeWorkItem) {
                subTasks.map { SubTaskUiModel(it.id, it.title, it.getProgress(), it.status == WorkStatus.DONE) }
            } else emptyList()
        )
    }
}
