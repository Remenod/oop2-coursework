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
    
    // Programming
    val commitsCount: Int? = null,
    val requiredCommits: Int? = null,
    val issuesResolved: Int? = null,
    val requiredIssues: Int? = null,
    val testsPassed: Double? = null,
    
    // Exam
    val examTopics: List<ExamTopicUiModel> = emptyList(),
    
    // Seminar
    val seminarStages: SeminarStagesUiModel? = null,
    
    val checklist: List<ChecklistUiModel> = emptyList(),
    val subTasks: List<SubTaskUiModel> = emptyList(),

    val attachments: List<AttachmentUiModel> = emptyList(),
    val logs: List<WorkLogEntryUiModel> = emptyList()
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
    val target: String,
    val createdAtText: String
)

data class WorkLogEntryUiModel(
    val id: Long,
    val message: String,
    val minutesSpent: Int,
    val minutesSpentText: String,
    val createdAtText: String
)
