package com.remenod.oop2_coursework.data.persistence.model

import com.remenod.oop2_coursework.domain.model.*
import java.io.Serializable
import java.time.LocalDateTime

/**
 * Mirror of a future 'disciplines' table
 */
data class DisciplineRecord(
    val id: Long,
    val name: String,
    val teacherName: String,
    val semester: Int,
    val color: Int
) : Serializable

/**
 * Mirror of a future 'work_items' table. 
 * A 'flat' record supporting polymorphism through [type] and nullable fields.
 */
data class WorkItemRecord(
    val id: Long,
    val disciplineId: Long,
    val parentId: Long?,
    val sortOrder: Int,
    val type: WorkItemType,
    val title: String,
    val description: String,
    val status: WorkStatus,
    val priority: Priority,
    val deadline: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val estimatedMinutes: Int,

    // Programming specialized fields
    val repositoryUrl: String? = null,
    val branch: String? = null,
    val requiredCommits: Int? = null,
    val commitsCount: Int? = null,
    val requiredClosedIssues: Int? = null,
    val issuesResolved: Int? = null,
    val testsPassed: Double? = null,

    // Reading specialized fields
    val totalPages: Int? = null,
    val readPages: Int? = null,

    // Seminar specialized fields
    val seminarTopic: String? = null,
    val presentationRequired: Boolean? = null,
    val topicSelected: Boolean? = null,
    val materialsCollected: Boolean? = null,
    val speechPrepared: Boolean? = null,
    val slidesPrepared: Boolean? = null,
    val rehearsalDone: Boolean? = null,

    // Project specialized fields
    val projectGoal: String? = null,

    // Exam specialized fields
    val targetGrade: Int? = null
) : Serializable

data class ChecklistItemRecord(
    val id: Long,
    val workItemId: Long,
    val text: String,
    val isCompleted: Boolean,
    val sortOrder: Int
) : Serializable

data class ExamTopicRecord(
    val id: Long,
    val workItemId: Long,
    val name: String,
    val confidence: Int,
    val sortOrder: Int
) : Serializable

data class AttachmentRecord(
    val id: Long,
    val workItemId: Long,
    val name: String,
    val type: AttachmentType,
    val subType: AttachmentSubtype,
    val urlOrPath: String,
    val provider: String? = null,
    val purpose: AttachmentPurpose = AttachmentPurpose.REFERENCE,
    val notes: String = "",
    val branch: String? = null,
    val repositoryOwner: String? = null,
    val repositoryName: String? = null,
    val lastOpenedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime
) : Serializable

data class WorkLogEntryRecord(
    val id: Long,
    val workItemId: Long,
    val message: String,
    val createdAt: LocalDateTime,
    val minutesSpent: Int
) : Serializable

/**
 * A flat collection of all records for a discipline or task tree.
 * Ready for bulk insertion into a database.
 */
data class PersistenceBundle(
    val workItems: List<WorkItemRecord> = emptyList(),
    val checklistItems: List<ChecklistItemRecord> = emptyList(),
    val examTopics: List<ExamTopicRecord> = emptyList(),
    val attachments: List<AttachmentRecord> = emptyList(),
    val logs: List<WorkLogEntryRecord> = emptyList()
) : Serializable

/**
 * A full app snapshot for simple local file persistence.
 */
data class PersistenceSnapshot(
    val version: Int = 1,
    val disciplines: List<DisciplineRecord> = emptyList(),
    val bundle: PersistenceBundle = PersistenceBundle()
) : Serializable
