package com.remenod.oop2_coursework.presentation.worklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remenod.oop2_coursework.domain.model.*
import com.remenod.oop2_coursework.domain.repository.TaskRepository
import com.remenod.oop2_coursework.presentation.common.DateTimeUiFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WorkListViewModel(
    private val repository: TaskRepository,
    private val disciplineId: Long
) : ViewModel() {

    private val _actionError = MutableStateFlow<String?>(null)
    private val _controls = MutableStateFlow(WorkListControls())

    val uiState: StateFlow<WorkListUiState> = combine(
        repository.observeDiscipline(disciplineId),
        _controls,
        _actionError
    ) { discipline, controls, actionError ->
        if (discipline == null) {
            WorkListUiState(error = "Discipline not found", actionError = actionError)
        } else {
            val allItems = discipline.workItems
            val visibleItems = allItems
                .filter { it.matchesQuery(controls.query) }
                .filter { it.matchesType(controls.typeFilter) }
                .filter { controls.statusFilter == null || it.status == controls.statusFilter }
                .filter { controls.priorityFilter == null || it.priority == controls.priorityFilter }
                .filter { controls.attachmentPurposeFilter == null || it.hasAttachmentPurpose(controls.attachmentPurposeFilter) }
                .filter { !controls.overdueOnly || it.isOverdue() }
                .filter { !controls.githubOnly || it.hasGitHubAttachment() }
                .filter { !controls.withLogsOnly || it.logs.isNotEmpty() }
                .sortBy(controls.sortOption)

            WorkListUiState(
                disciplineName = discipline.name,
                items = visibleItems.map { it.toCardUiModel() },
                totalItems = allItems.size,
                query = controls.query,
                typeFilter = controls.typeFilter,
                statusFilter = controls.statusFilter,
                priorityFilter = controls.priorityFilter,
                attachmentPurposeFilter = controls.attachmentPurposeFilter,
                overdueOnly = controls.overdueOnly,
                githubOnly = controls.githubOnly,
                withLogsOnly = controls.withLogsOnly,
                sortOption = controls.sortOption,
                actionError = actionError
            )
        }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WorkListUiState(isLoading = true)
        )

    fun clearActionError() {
        _actionError.value = null
    }

    fun updateQuery(value: String) {
        _controls.value = _controls.value.copy(query = value)
    }

    fun setTypeFilter(value: WorkListTypeFilter) {
        _controls.value = _controls.value.copy(typeFilter = value)
    }

    fun setStatusFilter(value: WorkStatus?) {
        _controls.value = _controls.value.copy(statusFilter = value)
    }

    fun setPriorityFilter(value: Priority?) {
        _controls.value = _controls.value.copy(priorityFilter = value)
    }

    fun setAttachmentPurposeFilter(value: AttachmentPurpose?) {
        _controls.value = _controls.value.copy(attachmentPurposeFilter = value)
    }

    fun setOverdueOnly(value: Boolean) {
        _controls.value = _controls.value.copy(overdueOnly = value)
    }

    fun setGithubOnly(value: Boolean) {
        _controls.value = _controls.value.copy(githubOnly = value)
    }

    fun setWithLogsOnly(value: Boolean) {
        _controls.value = _controls.value.copy(withLogsOnly = value)
    }

    fun setSortOption(value: WorkListSortOption) {
        _controls.value = _controls.value.copy(sortOption = value)
    }

    fun clearFilters() {
        _controls.value = WorkListControls()
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
            hasGitHubAttachment = hasGitHubAttachment(),
            hasLogs = logs.isNotEmpty(),
            deadlineText = DateTimeUiFormatter.formatDateTime(deadline),
            timeLeftText = DateTimeUiFormatter.timeLeft(deadline),
            estimatedTimeText = DateTimeUiFormatter.estimatedTime(estimatedMinutes)
        )
    }

    private fun WorkItem.matchesQuery(query: String): Boolean {
        val normalized = query.trim()
        if (normalized.isBlank()) return true
        return title.contains(normalized, ignoreCase = true) ||
                description.contains(normalized, ignoreCase = true)
    }

    private fun WorkItem.matchesType(typeFilter: WorkListTypeFilter): Boolean {
        return when (typeFilter) {
            WorkListTypeFilter.ALL -> true
            WorkListTypeFilter.GENERIC -> this is GenericTask
            WorkListTypeFilter.PROGRAMMING -> this is ProgrammingTask
            WorkListTypeFilter.EXAM -> this is ExamTask
            WorkListTypeFilter.SEMINAR -> this is SeminarTask
            WorkListTypeFilter.READING -> this is ReadingTask
            WorkListTypeFilter.PROJECT -> this is ProjectTask
        }
    }

    private fun WorkItem.hasGitHubAttachment(): Boolean {
        return attachments.any { it is GitHubRepositoryLink }
    }

    private fun WorkItem.hasAttachmentPurpose(purpose: AttachmentPurpose): Boolean {
        return attachments.any { it.purpose == purpose }
    }

    private fun List<WorkItem>.sortBy(sortOption: WorkListSortOption): List<WorkItem> {
        return when (sortOption) {
            WorkListSortOption.DEADLINE -> sortedWith(
                compareBy<WorkItem> { it.deadline == null }
                    .thenBy { it.deadline }
                    .thenByDescending { it.priority.ordinal }
            )
            WorkListSortOption.PRIORITY -> sortedWith(
                compareByDescending<WorkItem> { it.priority.ordinal }
                    .thenBy { it.deadline == null }
                    .thenBy { it.deadline }
            )
            WorkListSortOption.PROGRESS -> sortedBy { it.getProgress() }
            WorkListSortOption.UPDATED -> sortedByDescending { it.updatedAt }
        }
    }

    private data class WorkListControls(
        val query: String = "",
        val typeFilter: WorkListTypeFilter = WorkListTypeFilter.ALL,
        val statusFilter: WorkStatus? = null,
        val priorityFilter: Priority? = null,
        val attachmentPurposeFilter: AttachmentPurpose? = null,
        val overdueOnly: Boolean = false,
        val githubOnly: Boolean = false,
        val withLogsOnly: Boolean = false,
        val sortOption: WorkListSortOption = WorkListSortOption.UPDATED
    )
}
