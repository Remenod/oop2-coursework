package com.remenod.oop2_coursework.data.persistence.model

import com.remenod.oop2_coursework.domain.model.*
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
)

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
    val commitCount: Int? = null,
    val closedIssues: Int? = null,
    val testsPassed: Double? = null,

    // Reading specialized fields
    val totalPages: Int? = null,
    val readPages: Int? = null,

    // Seminar specialized fields
    val seminarTopic: String? = null,
    val topicSelected: Boolean? = null,
    val materialsCollected: Boolean? = null,
    val speechPrepared: Boolean? = null,
    val slidesPrepared: Boolean? = null,
    val rehearsalDone: Boolean? = null,

    // Project specialized fields
    val projectGoal: String? = null
)

data class ChecklistItemRecord(
    val id: Long,
    val workItemId: Long,
    val text: String,
    val isCompleted: Boolean,
    val sortOrder: Int
)

data class ExamTopicRecord(
    val id: Long,
    val workItemId: Long,
    val name: String,
    val confidence: Int,
    val sortOrder: Int
)

data class AttachmentRecord(
    val id: Long,
    val workItemId: Long,
    val name: String,
    val type: String, // "LINK" or "RESOURCE"
    val subType: String, // "GITHUB", "GOOGLE_CLASSROOM", "LOCAL", "CLOUD"
    val urlOrPath: String,
    val provider: String? = null,
    val createdAt: LocalDateTime
)

data class WorkLogEntryRecord(
    val id: Long,
    val workItemId: Long,
    val timestamp: LocalDateTime,
    val minutesSpent: Int,
    val oldStatus: WorkStatus,
    val newStatus: WorkStatus,
    val progressPercent: Double,
    val comment: String
)
