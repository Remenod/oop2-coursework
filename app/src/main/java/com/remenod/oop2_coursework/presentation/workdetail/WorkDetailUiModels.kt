package com.remenod.oop2_coursework.presentation.workdetail

import com.remenod.oop2_coursework.domain.model.Priority
import com.remenod.oop2_coursework.domain.model.WorkItemType
import com.remenod.oop2_coursework.domain.model.WorkStatus
import java.time.LocalDateTime

data class WorkDetailUiState(
    val isLoading: Boolean = false,
    val item: WorkItemDetailUiModel? = null,
    val error: String? = null,
    val actionError: String? = null
)

data class WorkItemDetailUiModel(
    val id: Long,
    val title: String,
    val description: String,
    val status: WorkStatus,
    val priority: Priority,
    val type: WorkItemType,
    val typeName: String,
    
    val deadline: LocalDateTime?,
    val deadlineText: String,
    val createdAtText: String,
    val updatedAtText: String,
    val estimatedMinutes: Int,
    val estimatedTimeText: String,
    val isOverdue: Boolean,
    val timeLeftText: String,

    val progressPercent: Double,
    val progressExplanation: String,
    val canBeCompleted: Boolean,
    
    // Reading
    val readPages: Int? = null,
    val totalPages: Int? = null,

    // Exam
    val examTopics: List<ExamTopicUiModel> = emptyList(),
    
    // Seminar
    val seminarStages: SeminarStagesUiModel? = null,
    
    val checklist: List<ChecklistUiModel> = emptyList(),
    val subTasks: List<SubTaskUiModel> = emptyList(),

    // Attachments & Logs
    val attachments: List<AttachmentUiModel> = emptyList(),
    val logs: List<WorkLogEntryUiModel> = emptyList(),
    val lastLogsSummary: String = ""
)

data class ChecklistUiModel(
    val index: Int,
    val text: String,
    val isCompleted: Boolean
)

data class SubTaskUiModel(
    val id: Long,
    val title: String,
    val progress: Double,
    val isDone: Boolean
)

data class ExamTopicUiModel(
    val index: Int,
    val name: String,
    val confidence: Int
)

data class SeminarStagesUiModel(
    val topicSelected: Boolean,
    val materialsCollected: Boolean,
    val speechPrepared: Boolean,
    val slidesPrepared: Boolean,
    val rehearsalDone: Boolean
)

data class AttachmentUiModel(
    val id: Long,
    val title: String,
    val typeLabel: String,
    val subtypeLabel: String,
    val purposeLabel: String,
    val target: String,
    val notes: String,
    val createdAtText: String,
    val lastOpenedText: String,
    val canSync: Boolean,
    val canSubmit: Boolean,
    val syncHint: String?,
    val providerLabel: String?,
    val branchLabel: String?,
    val repositoryFullName: String?,
    val activeIssuesCount: Int? = null,
    val openPullRequestsCount: Int? = null,
    val lastRepositoryActivityText: String? = null,
    val syncedAtText: String? = null,
    val importableCandidateCount: Int = 0,
    val canImportCandidates: Boolean = false
)

data class WorkLogEntryUiModel(
    val id: Long,
    val message: String,
    val minutesSpent: Int,
    val minutesSpentText: String,
    val createdAtText: String
)
