package com.remenod.oop2_coursework.data.persistence.mapper

import com.remenod.oop2_coursework.data.persistence.model.*
import com.remenod.oop2_coursework.domain.model.*

object PersistenceMapper {

    // --- Domain to Persistence ---

    fun mapDisciplineToRecord(domain: Discipline): DisciplineRecord {
        return DisciplineRecord(
            id = domain.id,
            name = domain.name,
            teacherName = domain.teacherName,
            semester = domain.semester,
            color = domain.color
        )
    }

    /**
     * Package for a WorkItem and its flattened relations
     */
    data class WorkItemPackage(
        val record: WorkItemRecord,
        val checklist: List<ChecklistItemRecord>,
        val topics: List<ExamTopicRecord>,
        val attachments: List<AttachmentRecord>,
        val logs: List<WorkLogEntryRecord>,
        val children: List<WorkItemPackage> = emptyList()
    )

    fun decomposeWorkItem(
        disciplineId: Long,
        domain: WorkItem,
        parentId: Long? = null,
        sortOrder: Int = 0
    ): WorkItemPackage {
        val type = when (domain) {
            is ProgrammingTask -> WorkItemType.PROGRAMMING
            is ExamTask -> WorkItemType.EXAM
            is SeminarTask -> WorkItemType.SEMINAR
            is ReadingTask -> WorkItemType.READING
            is ProjectTask -> WorkItemType.PROJECT
            else -> WorkItemType.GENERIC
        }

        val record = WorkItemRecord(
            id = domain.id,
            disciplineId = disciplineId,
            parentId = parentId,
            sortOrder = sortOrder,
            type = type,
            title = domain.title,
            description = domain.description,
            status = domain.status,
            priority = domain.priority,
            deadline = domain.deadline,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            estimatedMinutes = domain.estimatedMinutes,

            // Specialized
            commitCount = (domain as? ProgrammingTask)?.commitsCount,
            closedIssues = (domain as? ProgrammingTask)?.issuesResolved,
            testsPassed = (domain as? ProgrammingTask)?.testsPassed,
            readPages = (domain as? ReadingTask)?.readPages,
            totalPages = (domain as? ReadingTask)?.totalPages,
            seminarTopic = null,
            topicSelected = (domain as? SeminarTask)?.topicSelected,
            materialsCollected = (domain as? SeminarTask)?.materialsCollected,
            speechPrepared = (domain as? SeminarTask)?.speechPrepared,
            slidesPrepared = (domain as? SeminarTask)?.slidesPrepared,
            rehearsalDone = (domain as? SeminarTask)?.rehearsalDone
        )

        val checklist = if (domain is AtomicWorkItem) {
            domain.checklist.mapIndexed { i, it -> 
                ChecklistItemRecord(0, domain.id, it.text, it.isCompleted, i)
            }
        } else emptyList()

        val topics = if (domain is ExamTask) {
            domain.topics.mapIndexed { i, it ->
                ExamTopicRecord(0, domain.id, it.name, it.confidence, i)
            }
        } else emptyList()

        val attachments = domain.attachments.map { mapAttachmentToRecord(domain.id, it) }
        val logs = domain.logs.map { mapLogToRecord(domain.id, it) }

        val children = if (domain is CompositeWorkItem) {
            domain.subTasks.mapIndexed { i, it -> 
                decomposeWorkItem(disciplineId, it, domain.id, i)
            }
        } else emptyList()

        return WorkItemPackage(record, checklist, topics, attachments, logs, children)
    }

    private fun mapAttachmentToRecord(workItemId: Long, domain: Attachment): AttachmentRecord {
        val type = if (domain is LinkAttachment) "LINK" else "RESOURCE"
        val subType = when (domain) {
            is GitHubRepositoryLink -> "GITHUB"
            is GoogleClassroomLink -> "GOOGLE_CLASSROOM"
            is LocalFileResource -> "LOCAL"
            is CloudFileResource -> "CLOUD"
            else -> "UNKNOWN"
        }
        return AttachmentRecord(
            id = domain.id,
            workItemId = workItemId,
            name = domain.name,
            type = type,
            subType = subType,
            urlOrPath = when (domain) {
                is LinkAttachment -> domain.url
                is ResourceAttachment -> domain.path
                else -> ""
            },
            provider = (domain as? CloudFileResource)?.cloudProvider,
            createdAt = domain.createdAt
        )
    }

    private fun mapLogToRecord(workItemId: Long, domain: WorkLogEntry): WorkLogEntryRecord {
        return WorkLogEntryRecord(
            id = domain.id,
            workItemId = workItemId,
            timestamp = domain.timestamp,
            minutesSpent = domain.minutesSpent,
            oldStatus = domain.oldStatus,
            newStatus = domain.newStatus,
            progressPercent = domain.progressPercent,
            comment = domain.comment
        )
    }

    // --- Persistence to Domain ---

    fun restoreDiscipline(
        record: DisciplineRecord,
        workItems: List<WorkItem>
    ): Discipline {
        return Discipline(
            id = record.id,
            name = record.name,
            teacherName = record.teacherName,
            semester = record.semester,
            color = record.color
        ).apply {
            workItems.forEach { addWorkItem(it) }
        }
    }

    fun restoreWorkItemsHierarchy(
        records: List<WorkItemRecord>,
        checklistMap: Map<Long, List<ChecklistItemRecord>>,
        topicsMap: Map<Long, List<ExamTopicRecord>>,
        attachmentsMap: Map<Long, List<AttachmentRecord>>,
        logsMap: Map<Long, List<WorkLogEntryRecord>>
    ): List<WorkItem> {
        // 1. Create concrete instances
        val domainMap = records.associate { record ->
            record.id to restoreConcreteWorkItem(record).apply {
                if (this is AtomicWorkItem) {
                    checklistMap[record.id]?.forEach { 
                        addChecklistItem(ChecklistItem(it.text, it.isCompleted))
                    }
                }
                if (this is ExamTask) {
                    topicsMap[record.id]?.forEach { 
                        topics.add(ExamTopic(it.name, it.confidence))
                    }
                }
                attachmentsMap[record.id]?.forEach { addAttachment(restoreAttachment(it)) }
                logsMap[record.id]?.forEach { addLog(restoreLog(it)) }
            }
        }

        // 2. Build tree
        records.forEach { record ->
            if (record.parentId != null) {
                val parent = domainMap[record.parentId]
                val child = domainMap[record.id]
                if (parent is CompositeWorkItem && child != null) {
                    parent.addSubTask(child)
                }
            }
        }

        // 3. Return roots
        return records
            .filter { it.parentId == null }
            .sortedBy { it.sortOrder }
            .mapNotNull { domainMap[it.id] }
    }

    private fun restoreConcreteWorkItem(record: WorkItemRecord): WorkItem {
        return when (record.type) {
            WorkItemType.PROGRAMMING -> ProgrammingTask(
                id = record.id,
                title = record.title,
                description = record.description,
                commitsCount = record.commitCount ?: 0,
                issuesResolved = record.closedIssues ?: 0,
                testsPassed = record.testsPassed ?: 0.0,
                estimatedMinutes = record.estimatedMinutes
            )
            WorkItemType.EXAM -> ExamTask(record.id, record.title, record.description)
            WorkItemType.SEMINAR -> SeminarTask(
                id = record.id,
                title = record.title,
                description = record.description,
                topicSelected = record.topicSelected ?: false,
                materialsCollected = record.materialsCollected ?: false,
                speechPrepared = record.speechPrepared ?: false,
                slidesPrepared = record.slidesPrepared ?: false,
                rehearsalDone = record.rehearsalDone ?: false
            )
            WorkItemType.READING -> ReadingTask(
                id = record.id,
                title = record.title,
                description = record.description,
                readPages = record.readPages ?: 0,
                totalPages = record.totalPages ?: 100
            )
            WorkItemType.PROJECT -> ProjectTask(record.id, record.title, record.description)
            WorkItemType.GENERIC -> GenericTask(record.id, record.title, record.description)
        }.apply {
            status = record.status
            priority = record.priority
            deadline = record.deadline
            estimatedMinutes = record.estimatedMinutes
        }
    }

    private fun restoreAttachment(record: AttachmentRecord): Attachment {
        return when (record.subType) {
            "GITHUB" -> GitHubRepositoryLink(record.id, record.name, record.urlOrPath)
            "GOOGLE_CLASSROOM" -> GoogleClassroomLink(record.id, record.name, record.urlOrPath)
            "LOCAL" -> LocalFileResource(record.id, record.name, record.urlOrPath)
            "CLOUD" -> CloudFileResource(record.id, record.name, record.urlOrPath, record.provider ?: "Unknown")
            else -> GitHubRepositoryLink(record.id, record.name, record.urlOrPath)
        }
    }

    private fun restoreLog(record: WorkLogEntryRecord): WorkLogEntry {
        return WorkLogEntry(
            id = record.id,
            itemId = record.workItemId,
            timestamp = record.timestamp,
            minutesSpent = record.minutesSpent,
            oldStatus = record.oldStatus,
            newStatus = record.newStatus,
            progressPercent = record.progressPercent,
            comment = record.comment
        )
    }
}
