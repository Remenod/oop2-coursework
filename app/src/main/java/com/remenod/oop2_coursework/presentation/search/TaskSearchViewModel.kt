package com.remenod.oop2_coursework.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remenod.oop2_coursework.domain.model.*
import com.remenod.oop2_coursework.domain.repository.TaskRepository
import com.remenod.oop2_coursework.presentation.common.DateTimeUiFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class TaskSearchViewModel(
    repository: TaskRepository
) : ViewModel() {

    private val controls = MutableStateFlow(TaskSearchControls())

    val uiState: StateFlow<TaskSearchUiState> = combine(
        repository.observeDisciplines(),
        controls
    ) { disciplines, controls ->
        val allItems = disciplines.flatMap { discipline ->
            discipline.getAllWorkItemsRecursive().map { item ->
                SearchableTask(
                    item = item,
                    disciplineId = discipline.id,
                    disciplineName = discipline.name
                )
            }
        }

        val visibleItems = allItems
            .filter { it.item.matchesQuery(controls.query) }
            .filter { it.item.matchesType(controls.typeFilter) }
            .filter { controls.statusFilter == null || it.item.status == controls.statusFilter }
            .filter { controls.priorityFilter == null || it.item.priority == controls.priorityFilter }
            .filter { controls.attachmentPurposeFilter == null || it.item.hasAttachmentPurpose(controls.attachmentPurposeFilter) }
            .filter { !controls.overdueOnly || it.item.isOverdue() }
            .filter { !controls.githubOnly || it.item.hasGitHubAttachment() }
            .filter { !controls.withLogsOnly || it.item.logs.isNotEmpty() }
            .sortBy(controls.sortOption)

        TaskSearchUiState(
            items = visibleItems.map { it.toUiModel() },
            totalItems = allItems.size,
            query = controls.query,
            typeFilter = controls.typeFilter,
            statusFilter = controls.statusFilter,
            priorityFilter = controls.priorityFilter,
            attachmentPurposeFilter = controls.attachmentPurposeFilter,
            overdueOnly = controls.overdueOnly,
            githubOnly = controls.githubOnly,
            withLogsOnly = controls.withLogsOnly,
            sortOption = controls.sortOption
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskSearchUiState(isLoading = true)
    )

    fun updateQuery(value: String) {
        controls.value = controls.value.copy(query = value)
    }

    fun setTypeFilter(value: TaskSearchTypeFilter) {
        controls.value = controls.value.copy(typeFilter = value)
    }

    fun setStatusFilter(value: WorkStatus?) {
        controls.value = controls.value.copy(statusFilter = value)
    }

    fun setPriorityFilter(value: Priority?) {
        controls.value = controls.value.copy(priorityFilter = value)
    }

    fun setAttachmentPurposeFilter(value: AttachmentPurpose?) {
        controls.value = controls.value.copy(attachmentPurposeFilter = value)
    }

    fun setOverdueOnly(value: Boolean) {
        controls.value = controls.value.copy(overdueOnly = value)
    }

    fun setGithubOnly(value: Boolean) {
        controls.value = controls.value.copy(githubOnly = value)
    }

    fun setWithLogsOnly(value: Boolean) {
        controls.value = controls.value.copy(withLogsOnly = value)
    }

    fun setSortOption(value: TaskSearchSortOption) {
        controls.value = controls.value.copy(sortOption = value)
    }

    fun clearFilters() {
        controls.value = TaskSearchControls()
    }

    private fun SearchableTask.toUiModel(): TaskSearchItemUiModel {
        return TaskSearchItemUiModel(
            id = item.id,
            disciplineId = disciplineId,
            disciplineName = disciplineName,
            title = item.title,
            typeLabel = item.typeLabel(),
            priority = item.priority,
            status = item.status,
            progressPercent = item.getProgress(),
            isOverdue = item.isOverdue(),
            hasGitHubAttachment = item.hasGitHubAttachment(),
            hasLogs = item.logs.isNotEmpty(),
            deadlineText = DateTimeUiFormatter.formatDateTime(item.deadline),
            timeLeftText = DateTimeUiFormatter.timeLeft(item.deadline)
        )
    }

    private fun WorkItem.matchesQuery(query: String): Boolean {
        val normalized = query.trim()
        if (normalized.isBlank()) return true
        return title.contains(normalized, ignoreCase = true) ||
                description.contains(normalized, ignoreCase = true)
    }

    private fun WorkItem.matchesType(typeFilter: TaskSearchTypeFilter): Boolean {
        return when (typeFilter) {
            TaskSearchTypeFilter.ALL -> true
            TaskSearchTypeFilter.GENERIC -> this is GenericTask
            TaskSearchTypeFilter.PROGRAMMING -> this is ProgrammingTask
            TaskSearchTypeFilter.EXAM -> this is ExamTask
            TaskSearchTypeFilter.SEMINAR -> this is SeminarTask
            TaskSearchTypeFilter.READING -> this is ReadingTask
            TaskSearchTypeFilter.PROJECT -> this is ProjectTask
        }
    }

    private fun WorkItem.typeLabel(): String {
        return when (this) {
            is ProgrammingTask -> "Programming"
            is ExamTask -> "Exam"
            is SeminarTask -> "Seminar"
            is ReadingTask -> "Reading"
            is ProjectTask -> "Project"
            else -> "Generic"
        }
    }

    private fun WorkItem.hasGitHubAttachment(): Boolean {
        return attachments.any { it is GitHubRepositoryLink }
    }

    private fun WorkItem.hasAttachmentPurpose(purpose: AttachmentPurpose): Boolean {
        return attachments.any { it.purpose == purpose }
    }

    private fun List<SearchableTask>.sortBy(sortOption: TaskSearchSortOption): List<SearchableTask> {
        return when (sortOption) {
            TaskSearchSortOption.DEADLINE -> sortedWith(
                compareBy<SearchableTask> { it.item.deadline == null }
                    .thenBy { it.item.deadline }
                    .thenByDescending { it.item.priority.ordinal }
            )
            TaskSearchSortOption.PRIORITY -> sortedWith(
                compareByDescending<SearchableTask> { it.item.priority.ordinal }
                    .thenBy { it.item.deadline == null }
                    .thenBy { it.item.deadline }
            )
            TaskSearchSortOption.PROGRESS -> sortedBy { it.item.getProgress() }
            TaskSearchSortOption.UPDATED -> sortedByDescending { it.item.updatedAt }
        }
    }

    private data class SearchableTask(
        val item: WorkItem,
        val disciplineId: Long,
        val disciplineName: String
    )

    private data class TaskSearchControls(
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
}
