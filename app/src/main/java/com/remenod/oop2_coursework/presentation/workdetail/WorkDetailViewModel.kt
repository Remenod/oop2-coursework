package com.remenod.oop2_coursework.presentation.workdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remenod.oop2_coursework.domain.model.*
import com.remenod.oop2_coursework.domain.repository.TaskRepository
import com.remenod.oop2_coursework.domain.interfaces.Syncable
import com.remenod.oop2_coursework.domain.interfaces.Submittable
import com.remenod.oop2_coursework.domain.service.GitHubRepositoryService
import com.remenod.oop2_coursework.presentation.common.DateTimeUiFormatter
import com.remenod.oop2_coursework.presentation.worklist.WorkItemEditResult
import com.remenod.oop2_coursework.presentation.worklist.WorkItemFactory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WorkDetailViewModel(
    private val repository: TaskRepository,
    private val workItemId: Long,
    private val gitHubRepositoryService: GitHubRepositoryService
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

    fun addExamTopic(name: String, confidence: Int) {
        if (name.isBlank()) return
        if (confidence !in 0..100) {
            _actionError.value = "Confidence must be between 0 and 100"
            return
        }
        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                if (item is ExamTask) {
                    try {
                        item.addTopic(name, confidence)
                        repository.updateWorkItem(item)
                        addAutoLog("Added exam topic: $name")
                        _actionError.value = null
                    } catch (e: Exception) {
                        _actionError.value = e.message ?: "Could not add topic"
                    }
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
                    try {
                        val topic = item.topics[index]
                        item.updateTopicConfidence(index, confidence)
                        repository.updateWorkItem(item)
                        addAutoLog("Updated exam topic confidence: ${topic.name}")
                        _actionError.value = null
                    } catch (e: Exception) {
                        _actionError.value = e.message ?: "Could not update topic"
                    }
                }
            }
        }
    }

    fun removeExamTopic(index: Int) {
        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                if (item is ExamTask && index in item.topics.indices) {
                    val removed = item.removeTopic(index)
                    repository.updateWorkItem(item)
                    addAutoLog("Removed exam topic: ${removed.name}")
                }
            }
        }
    }

    fun setSeminarStage(stage: SeminarStageType, checked: Boolean) {
        viewModelScope.launch {
            repository.observeWorkItem(workItemId).first()?.let { item ->
                if (item is SeminarTask) {
                    item.setStage(stage, checked)
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
                val attachment = AttachmentFactory.createFrom(result)
                repository.addAttachment(workItemId, attachment)

                if (attachment is GitHubRepositoryLink) {
                    addAutoLog("Linked GitHub repository ${attachment.fullName ?: attachment.title}. ${attachment.syncHint()}")
                } else {
                    addAutoLog("Added attachment: ${attachment.getDisplayName()}")
                }

                _actionError.value = null
            } catch (e: Exception) {
                _actionError.value = e.message ?: "Attachment creation failed"
            }
        }
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

                val message = if (attachment is GitHubRepositoryLink) {
                    val result = gitHubRepositoryService.sync(attachment)
                    attachment.repositorySnapshot = result.snapshot
                    result.message
                } else {
                    require(attachment is Syncable) { "This attachment type does not support sync" }
                    attachment.sync()
                    "Synced attachment: ${attachment.title}"
                }
                repository.updateWorkItem(item)

                addAutoLog(message)
                _actionError.value = message
            } catch (e: Exception) {
                _actionError.value = e.message ?: "Sync failed"
            }
        }
    }

    fun importGitHubCandidates(attachmentId: Long) {
        viewModelScope.launch {
            try {
                val item = repository.observeWorkItem(workItemId).first() ?: error("Task not found")
                require(item is AtomicWorkItem) { "Checklist import is available for atomic tasks only" }

                val attachment = item.attachments.find { it.id == attachmentId } as? GitHubRepositoryLink
                    ?: error("GitHub attachment not found")
                val candidates = attachment.importableCandidates
                require(candidates.isNotEmpty()) { "No GitHub items are available to import yet" }

                val existing = item.checklist.map { it.text }.toSet()
                val imported = candidates
                    .map { it.toChecklistText() }
                    .filterNot { it in existing }

                imported.forEach(item::addChecklistItem)
                repository.updateWorkItem(item)

                val message = if (imported.isEmpty()) {
                    "GitHub checklist import skipped: all items already exist"
                } else {
                    "Imported ${imported.size} GitHub item(s) into checklist"
                }
                addAutoLog(message)
                _actionError.value = message
            } catch (e: Exception) {
                _actionError.value = e.message ?: "GitHub import failed"
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

    private fun WorkItem.toDetailUiModel(): WorkItemDetailUiModel {
        val snapshot = getProgressSnapshot()
        val type = when (this) {
            is ProjectTask -> WorkItemType.PROJECT
            is ReadingTask -> WorkItemType.READING
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
        val github = this as? GitHubRepositoryLink
        val cloud = this as? CloudFileResource
        val hasGitHubSnapshot = github?.repositorySnapshot?.let {
            it.syncedAt != null || it.workCandidates.isNotEmpty()
        } == true

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
            syncHint = github?.syncHint(),
            providerLabel = cloud?.cloudProvider,
            branchLabel = github?.effectiveBranch,
            repositoryFullName = github?.fullName,
            activeIssuesCount = github?.activeIssuesCount?.takeIf { hasGitHubSnapshot },
            openPullRequestsCount = github?.openPullRequestsCount?.takeIf { hasGitHubSnapshot },
            lastRepositoryActivityText = github?.repositorySnapshot?.lastRepositoryActivityAt
                ?.let(DateTimeUiFormatter::formatDateTime),
            syncedAtText = github?.repositorySnapshot?.syncedAt
                ?.let(DateTimeUiFormatter::formatDateTime),
            importableCandidateCount = github?.importableCandidates?.size ?: 0,
            canImportCandidates = github?.importableCandidates?.isNotEmpty() == true
        )
    }

    private fun GitHubWorkCandidate.toChecklistText(): String {
        val prefix = when (type) {
            GitHubWorkCandidateType.ISSUE -> "Issue"
            GitHubWorkCandidateType.PULL_REQUEST -> "PR"
        }
        return "$prefix #$number: $title"
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
}
