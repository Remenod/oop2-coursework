package com.remenod.oop2_coursework.presentation.workdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remenod.oop2_coursework.domain.model.*
import com.remenod.oop2_coursework.domain.repository.TaskRepository
import com.remenod.oop2_coursework.presentation.common.DateTimeUiFormatter
import com.remenod.oop2_coursework.presentation.worklist.WorkItemEditResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WorkDetailViewModel(
    private val repository: TaskRepository,
    private val workItemId: Long
) : ViewModel() {

    private val _actionError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<WorkDetailUiState> = repository.observeWorkItem(workItemId)
        .combine(_actionError) { item, actionError ->
            if (item == null) {
                WorkDetailUiState(error = "Task not found")
            } else {
                WorkDetailUiState(
                    item = item.toDetailUiModel(),
                    actionError = actionError
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WorkDetailUiState(isLoading = true)
        )

    fun clearActionError() {
        _actionError.value = null
    }

    fun updateMetadata(result: WorkItemEditResult) {
        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                try {
                    item.updateMetadata(
                        title = result.title,
                        description = result.description,
                        status = result.status,
                        priority = result.priority,
                        deadline = result.deadline,
                        estimatedMinutes = result.estimatedMinutes
                    )
                    repository.updateWorkItem(item)
                    _actionError.value = null
                } catch (e: Exception) {
                    _actionError.value = e.message ?: "Update failed"
                }
            }
        }
    }

    fun updateReadingProgress(readPages: Int, totalPages: Int) {
        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                if (item is ReadingTask) {
                    try {
                        item.updatePages(readPages, totalPages)
                        repository.updateWorkItem(item)
                        _actionError.value = null
                    } catch (e: Exception) {
                        _actionError.value = e.message ?: "Update failed"
                    }
                }
            }
        }
    }

    fun updateProgrammingStats(commitsCount: Int, requiredCommits: Int, issuesResolved: Int, requiredIssues: Int, testsPassed: Double) {
        if (commitsCount < 0 || requiredCommits < 0 || issuesResolved < 0 || requiredIssues < 0 || testsPassed !in 0.0..1.0) {
            _actionError.value = "Invalid programming stats"
            return
        }

        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                if (item is ProgrammingTask) {
                    item.commitsCount = commitsCount
                    item.requiredCommits = requiredCommits
                    item.issuesResolved = issuesResolved
                    item.requiredIssues = requiredIssues
                    item.testsPassed = testsPassed
                    item.touch()
                    repository.updateWorkItem(item)
                    _actionError.value = null
                }
            }
        }
    }

    fun addExamTopic(name: String, confidence: Int) {
        if (name.isBlank()) return
        if (confidence !in 0..100) {
            _actionError.value = "Confidence must be between 0 and 100"
            return
        }
        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                if (item is ExamTask) {
                    item.topics.add(ExamTopic(name, confidence))
                    item.touch()
                    repository.updateWorkItem(item)
                }
            }
        }
    }

    fun updateExamTopic(index: Int, confidence: Int) {
        if (confidence !in 0..100) {
            _actionError.value = "Confidence must be between 0 and 100"
            return
        }
        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                if (item is ExamTask && index in item.topics.indices) {
                    val topic = item.topics[index]
                    item.topics[index] = topic.copy(confidence = confidence)
                    item.touch()
                    repository.updateWorkItem(item)
                }
            }
        }
    }

    fun removeExamTopic(index: Int) {
        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                if (item is ExamTask && index in item.topics.indices) {
                    item.topics.removeAt(index)
                    item.touch()
                    repository.updateWorkItem(item)
                }
            }
        }
    }

    fun setSeminarStage(stage: SeminarStageType, checked: Boolean) {
        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                if (item is SeminarTask) {
                    when (stage) {
                        SeminarStageType.TOPIC_SELECTED -> item.topicSelected = checked
                        SeminarStageType.MATERIALS_COLLECTED -> item.materialsCollected = checked
                        SeminarStageType.SPEECH_PREPARED -> item.speechPrepared = checked
                        SeminarStageType.SLIDES_PREPARED -> item.slidesPrepared = checked
                        SeminarStageType.REHEARSAL_DONE -> item.rehearsalDone = checked
                    }
                    item.touch()
                    repository.updateWorkItem(item)
                }
            }
        }
    }

    fun addChecklistItem(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                if (item is AtomicWorkItem) {
                    item.addChecklistItem(text)
                    repository.updateWorkItem(item)
                }
            }
        }
    }

    fun setChecklistItemCompleted(index: Int, completed: Boolean) {
        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                if (item is AtomicWorkItem) {
                    item.setChecklistItemCompleted(index, completed)
                    repository.updateWorkItem(item)
                }
            }
        }
    }

    fun removeChecklistItem(index: Int) {
        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                if (item is AtomicWorkItem) {
                    item.removeChecklistItem(index)
                    repository.updateWorkItem(item)
                }
            }
        }
    }

    fun toggleCompletion() {
        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                try {
                    if (item.status == WorkStatus.DONE) {
                        item.changeStatus(WorkStatus.IN_PROGRESS)
                    } else {
                        item.complete()
                    }
                    repository.updateWorkItem(item)
                    _actionError.value = null
                } catch (e: Exception) {
                    _actionError.value = e.message ?: "Completion toggle failed"
                }
            }
        }
    }

    fun addSubTask(result: WorkItemEditResult) {
        viewModelScope.launch {
            try {
                val item = WorkItemFactory.createFrom(result)
                repository.addSubTask(workItemId, item)
                _actionError.value = null
            } catch (e: Exception) {
                _actionError.value = e.message ?: "Could not add subtask"
            }
        }
    }

    fun deleteThisTask() {
        viewModelScope.launch {
            repository.deleteWorkItem(workItemId)
        }
    }

    private fun WorkItem.toDetailUiModel(): WorkItemDetailUiModel {
        val snapshot = getProgressSnapshot()
        val type = when (this) {
            is ProjectTask -> WorkItemType.PROJECT
            is ReadingTask -> WorkItemType.READING
            is ProgrammingTask -> WorkItemType.PROGRAMMING
            is ExamTask -> WorkItemType.EXAM
            is SeminarTask -> WorkItemType.SEMINAR
            else -> WorkItemType.GENERIC
        }
        
        return WorkItemDetailUiModel(
            id = id,
            title = title,
            description = description,
            status = status,
            priority = priority,
            type = type,
            typeName = this::class.simpleName ?: "Task",
            
            deadline = deadline,
            deadlineText = DateTimeUiFormatter.formatDateTime(deadline),
            createdAtText = DateTimeUiFormatter.formatDateTime(createdAt),
            updatedAtText = DateTimeUiFormatter.formatDateTime(updatedAt),
            estimatedMinutes = estimatedMinutes,
            estimatedTimeText = DateTimeUiFormatter.estimatedTime(estimatedMinutes),
            isOverdue = isOverdue(),
            timeLeftText = DateTimeUiFormatter.timeLeft(deadline),

            progressPercent = snapshot.percent,
            progressExplanation = snapshot.explanation,
            canBeCompleted = canBeCompleted(),
            
            readPages = (this as? ReadingTask)?.readPages,
            totalPages = (this as? ReadingTask)?.totalPages,
            commitsCount = (this as? ProgrammingTask)?.commitsCount,
            requiredCommits = (this as? ProgrammingTask)?.requiredCommits,
            issuesResolved = (this as? ProgrammingTask)?.issuesResolved,
            requiredIssues = (this as? ProgrammingTask)?.requiredIssues,
            testsPassed = (this as? ProgrammingTask)?.testsPassed,

            examTopics = (this as? ExamTask)?.topics?.mapIndexed { i, t ->
                ExamTopicUiModel(i, t.name, t.confidence)
            } ?: emptyList(),
            seminarStages = (this as? SeminarTask)?.let {
                SeminarStagesUiModel(
                    it.topicSelected, it.materialsCollected, it.speechPrepared, it.slidesPrepared, it.rehearsalDone
                )
            },
            checklist = if (this is AtomicWorkItem) {
                checklist.mapIndexed { index, item ->
                    ChecklistUiModel(index, item.text, item.isCompleted)
                }
            } else emptyList(),
            subTasks = if (this is CompositeWorkItem) {
                subTasks.map { SubTaskUiModel(it.id, it.title, it.getProgress(), it.status == WorkStatus.DONE) }
            } else emptyList()
        )
    }
}
