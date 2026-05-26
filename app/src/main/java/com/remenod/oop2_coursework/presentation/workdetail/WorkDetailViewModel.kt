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

    fun updateProgrammingStats(commitsCount: Int, requiredCommits: Int, issuesResolved: Int, requiredIssues: Int, testsPassed: Double) {
        if (commitsCount < 0 || requiredCommits < 0 || issuesResolved < 0 || requiredIssues < 0 || testsPassed !in 0.0..1.0) return

        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                if (item is ProgrammingTask) {
                    item.commitsCount = commitsCount
                    item.requiredCommits = requiredCommits
                    item.issuesResolved = issuesResolved
                    item.requiredIssues = requiredIssues
                    item.testsPassed = testsPassed
                    repository.updateWorkItem(item)
                }
            }
        }
    }

    fun addExamTopic(name: String, confidence: Int) {
        if (name.isBlank() || confidence !in 0..100) return

        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                if (item is ExamTask) {
                    item.topics.add(ExamTopic(name, confidence))
                    repository.updateWorkItem(item)
                }
            }
        }
    }

    fun updateExamTopic(index: Int, confidence: Int) {
        if (confidence !in 0..100) return

        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                if (item is ExamTask && index in item.topics.indices) {
                    val topic = item.topics[index]
                    item.topics[index] = topic.copy(confidence = confidence)
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
                if (item.status == WorkStatus.DONE) {
                    repository.changeWorkItemStatus(workItemId, WorkStatus.IN_PROGRESS)
                } else {
                    try {
                        item.complete()
                        repository.updateWorkItem(item)
                    } catch (_: Exception) {}
                }
            }
        }
    }

    fun addSubTask(title: String, description: String, type: WorkItemType, priority: Priority, initialData: Map<String, Any>) {
        viewModelScope.launch {
            val item = when (type) {
                WorkItemType.PROJECT -> ProjectTask(0, title, description)
                WorkItemType.READING -> ReadingTask(
                    id = 0,
                    title = title,
                    description = description,
                    totalPages = initialData["totalPages"] as? Int ?: 100
                )
                WorkItemType.PROGRAMMING -> ProgrammingTask(
                    id = 0,
                    title = title,
                    description = description,
                    commitsCount = initialData["commitsCount"] as? Int ?: 0,
                    issuesResolved = initialData["issuesResolved"] as? Int ?: 0,
                    testsPassed = initialData["testsPassed"] as? Double ?: 0.0
                )
                WorkItemType.EXAM -> ExamTask(0, title, description)
                WorkItemType.SEMINAR -> SeminarTask(0, title, description)
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
            deadline = deadline?.toString() ?: "No deadline",
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
