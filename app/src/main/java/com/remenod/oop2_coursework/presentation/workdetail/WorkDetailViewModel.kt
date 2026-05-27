package com.remenod.oop2_coursework.presentation.workdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remenod.oop2_coursework.domain.model.*
import com.remenod.oop2_coursework.domain.repository.TaskRepository
import com.remenod.oop2_coursework.domain.interfaces.Syncable
import com.remenod.oop2_coursework.domain.interfaces.Submittable
import com.remenod.oop2_coursework.presentation.common.DateTimeUiFormatter
import com.remenod.oop2_coursework.presentation.worklist.WorkItemEditResult
import com.remenod.oop2_coursework.presentation.worklist.WorkItemFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WorkDetailViewModel(
    private val repository: TaskRepository,
    private val workItemId: Long
) : ViewModel() {

    private val _actionError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<WorkDetailUiState> = repository.observeWorkItem(workItemId)
        .combine(_actionError) { item, actionError ->
            item.toUiState(actionError)
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = repository.getWorkItemSnapshot(workItemId).toUiState(_actionError.value)
        )

    fun clearActionError() {
        _actionError.value = null
    }

    private suspend fun addAutoLog(message: String, minutes: Int = 0) {
        repository.addWorkLogEntry(workItemId, WorkLogEntry(0, message, minutesSpent = minutes))
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
                    addAutoLog("Updated task metadata")
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
                        addAutoLog("Updated reading progress: $readPages/$totalPages")
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
                    addAutoLog("Updated programming stats")
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
                    addAutoLog("Added exam topic: $name")
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
                    addAutoLog("Updated exam topic confidence: ${topic.name}")
                }
            }
        }
    }

    fun removeExamTopic(index: Int) {
        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                if (item is ExamTask && index in item.topics.indices) {
                    val name = item.topics[index].name
                    item.topics.removeAt(index)
                    item.touch()
                    repository.updateWorkItem(item)
                    addAutoLog("Removed exam topic: $name")
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
                    addAutoLog("Updated seminar stage: $stage to $checked")
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
                    addAutoLog("Added checklist item: $text")
                }
            }
        }
    }

    fun setChecklistItemCompleted(index: Int, completed: Boolean) {
        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                if (item is AtomicWorkItem && index in item.checklist.indices) {
                    val text = item.checklist[index].text
                    item.setChecklistItemCompleted(index, completed)
                    repository.updateWorkItem(item)
                    addAutoLog("${if (completed) "Completed" else "Unchecked"} checklist item: $text")
                }
            }
        }
    }

    fun removeChecklistItem(index: Int) {
        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                if (item is AtomicWorkItem && index in item.checklist.indices) {
                    item.removeChecklistItem(index)
                    repository.updateWorkItem(item)
                    addAutoLog("Removed checklist item")
                }
            }
        }
    }

    fun toggleCompletion() {
        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                try {
                    val isDone = item.status == WorkStatus.DONE
                    if (isDone) {
                        item.changeStatus(WorkStatus.IN_PROGRESS)
                        addAutoLog("Marked task as not done")
                    } else {
                        item.complete()
                        addAutoLog("Marked task as done")
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
                addAutoLog("Added subtask: ${result.title}")
                _actionError.value = null
            } catch (e: Exception) {
                _actionError.value = e.message ?: "Could not add subtask"
            }
        }
    }

    fun addAttachment(result: AttachmentEditResult) {
        viewModelScope.launch {
            try {
                val currentItem = repository.observeWorkItem(workItemId).first()
                requireNotNull(currentItem) { "Task not found" }

                val attachment = AttachmentFactory.createFrom(result)
                repository.addAttachment(workItemId, attachment)

                if (currentItem is ProgrammingTask && attachment is GitHubRepositoryLink) {
                    addGitHubProgrammingScaffold(currentItem, attachment)
                    addAutoLog("Linked GitHub repository ${attachment.fullName ?: attachment.title}. ${attachment.programmingTaskSyncHint()}")
                } else {
                    addAutoLog("Added attachment: ${attachment.getDisplayName()}")
                }

                _actionError.value = null
            } catch (e: Exception) {
                _actionError.value = e.message ?: "Attachment creation failed"
            }
        }
    }

    private suspend fun addGitHubProgrammingScaffold(
        item: ProgrammingTask,
        attachment: GitHubRepositoryLink
    ) {
        item.repositoryUrl = attachment.repositoryInfo?.canonicalUrl ?: attachment.url
        item.branch = attachment.effectiveBranch

        if (item.checklist.isNotEmpty()) {
            repository.updateWorkItem(item)
            return
        }

        val repo = attachment.fullName ?: attachment.title
        val branchText = attachment.effectiveBranch?.let { " on $it" } ?: ""
        listOf(
            "Clone repository $repo",
            "Create or checkout working branch$branchText",
            "Push commits and keep tests passing"
        ).forEach { item.addChecklistItem(it) }
        repository.updateWorkItem(item)
    }

    fun removeAttachment(attachmentId: Long) {
        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                val title = item.attachments.find { it.id == attachmentId }?.title ?: "attachment"
                repository.removeAttachment(workItemId, attachmentId)
                addAutoLog("Removed attachment: $title")
            }
        }
    }

    fun openAttachment(attachmentId: Long) {
        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                item.attachments.find { it.id == attachmentId }?.let { attachment ->
                    attachment.open()
                    repository.updateWorkItem(item)
                    addAutoLog("Opened attachment: ${attachment.title}")
                }
            }
        }
    }

    fun syncAttachment(attachmentId: Long) {
        viewModelScope.launch {
            try {
                val item = repository.observeWorkItem(workItemId).first() ?: error("Task not found")
                val attachment = item.attachments.find { it.id == attachmentId } ?: error("Attachment not found")
                require(attachment is Syncable) { "This attachment type does not support sync" }

                attachment.sync()

                val message = if (item is ProgrammingTask && attachment is GitHubRepositoryLink) {
                    "GitHub sync placeholder: later this can update commits, issues and tests for ${attachment.fullName ?: attachment.title}."
                } else {
                    "Synced attachment: ${attachment.title}"
                }
                addAutoLog(message)
                _actionError.value = message
            } catch (e: Exception) {
                _actionError.value = e.message ?: "Sync failed"
            }
        }
    }

    fun submitAttachment(attachmentId: Long) {
        viewModelScope.launch {
            try {
                val item = repository.observeWorkItem(workItemId).first() ?: error("Task not found")
                val attachment = item.attachments.find { it.id == attachmentId } ?: error("Attachment not found")
                require(attachment is Submittable) { "This attachment type does not support submit" }

                attachment.submit()
                addAutoLog("Submit placeholder used for attachment: ${attachment.title}")
                _actionError.value = "Submit is mocked for now"
            } catch (e: Exception) {
                _actionError.value = e.message ?: "Submit failed"
            }
        }
    }

    fun addManualLog(message: String, minutesSpent: Int) {
        if (message.isBlank()) {
            _actionError.value = "Message cannot be blank"
            return
        }
        if (minutesSpent < 0) {
            _actionError.value = "Minutes spent cannot be negative"
            return
        }
        viewModelScope.launch {
            repository.addWorkLogEntry(workItemId, WorkLogEntry(0, message, minutesSpent = minutesSpent))
            _actionError.value = null
        }
    }

    fun removeLogEntry(logId: Long) {
        viewModelScope.launch {
            repository.removeWorkLogEntry(workItemId, logId)
        }
    }

    fun deleteThisTask() {
        viewModelScope.launch {
            repository.deleteWorkItem(workItemId)
        }
    }

    private fun WorkItem?.toUiState(actionError: String?): WorkDetailUiState {
        return if (this == null) {
            WorkDetailUiState(error = "Task not found")
        } else {
            WorkDetailUiState(
                item = toDetailUiModel(),
                actionError = actionError
            )
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
        
        val sortedLogs = logs.sortedByDescending { it.createdAt }
        
        return WorkItemDetailUiModel(
            id = id,
            title = title,
            description = description,
            status = status,
            priority = priority,
            type = type,
            typeName = type.displayName(),
            
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
            repositoryUrl = (this as? ProgrammingTask)?.repositoryUrl,
            branch = (this as? ProgrammingTask)?.branch,

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
            } else emptyList(),

            attachments = attachments.map { it.toUiModel() },
            logs = sortedLogs.map { it.toUiModel() },
            lastLogsSummary = if (sortedLogs.isEmpty()) "No activity" else "Last action: ${sortedLogs.first().message}"
        )
    }

    private fun Attachment.toUiModel(): AttachmentUiModel {
        return AttachmentUiModel(
            id = id,
            title = title,
            typeLabel = when(this) {
                is LinkAttachment -> "Link"
                is ResourceAttachment -> "Resource"
                else -> "Attachment"
            },
            subtypeLabel = when(this) {
                is GitHubRepositoryLink -> "GitHub"
                is GoogleClassroomLink -> "Classroom"
                is LocalFileResource -> "Local File"
                is CloudFileResource -> "Cloud File"
                is GenericWebLink -> "Web"
                else -> "Unknown"
            },
            purposeLabel = purpose.name.replace('_', ' '),
            target = when(this) {
                is GitHubRepositoryLink -> repositoryInfo?.canonicalUrl ?: url
                is LinkAttachment -> url
                is ResourceAttachment -> pathOrUrl
                else -> ""
            },
            notes = notes,
            createdAtText = DateTimeUiFormatter.formatDateTime(createdAt),
            lastOpenedText = lastOpenedAt?.let { DateTimeUiFormatter.formatDateTime(it) } ?: "Never opened",
            canSync = this is Syncable,
            canSubmit = this is Submittable,
            syncHint = (this as? GitHubRepositoryLink)?.programmingTaskSyncHint(),
            providerLabel = (this as? CloudFileResource)?.cloudProvider,
            branchLabel = (this as? GitHubRepositoryLink)?.effectiveBranch,
            repositoryFullName = (this as? GitHubRepositoryLink)?.fullName
        )
    }

    private fun WorkLogEntry.toUiModel(): WorkLogEntryUiModel {
        return WorkLogEntryUiModel(
            id = id,
            message = message,
            minutesSpent = minutesSpent,
            minutesSpentText = DateTimeUiFormatter.estimatedTime(minutesSpent),
            createdAtText = DateTimeUiFormatter.formatDateTime(createdAt)
        )
    }

    private fun WorkItemType.displayName(): String {
        return when (this) {
            WorkItemType.GENERIC -> "Generic Task"
            WorkItemType.PROGRAMMING -> "Programming Task"
            WorkItemType.EXAM -> "Exam Task"
            WorkItemType.SEMINAR -> "Seminar Task"
            WorkItemType.READING -> "Reading Task"
            WorkItemType.PROJECT -> "Project Task"
        }
    }
}
