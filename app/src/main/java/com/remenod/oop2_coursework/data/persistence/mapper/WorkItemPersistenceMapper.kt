package com.remenod.oop2_coursework.data.persistence.mapper

import com.remenod.oop2_coursework.data.persistence.model.*
import com.remenod.oop2_coursework.domain.model.*

object WorkItemPersistenceMapper {

    // --- Domain to Persistence ---

    fun decomposeToBundle(
        disciplineId: Long,
        workItems: List<WorkItem>
    ): PersistenceBundle {
        val bundleWorkItems = mutableListOf<WorkItemRecord>()
        val bundleChecklist = mutableListOf<ChecklistItemRecord>()
        val bundleTopics = mutableListOf<ExamTopicRecord>()
        val bundleAttachments = mutableListOf<AttachmentRecord>()
        val bundleLogs = mutableListOf<WorkLogEntryRecord>()

        fun process(item: WorkItem, parentId: Long?, order: Int) {
            val type = when (item) {
                is ProgrammingTask -> WorkItemType.PROGRAMMING
                is ExamTask -> WorkItemType.EXAM
                is SeminarTask -> WorkItemType.SEMINAR
                is ReadingTask -> WorkItemType.READING
                is ProjectTask -> WorkItemType.PROJECT
                else -> WorkItemType.GENERIC
            }

            bundleWorkItems.add(WorkItemRecord(
                id = item.id,
                disciplineId = disciplineId,
                parentId = parentId,
                sortOrder = order,
                type = type,
                title = item.title,
                description = item.description,
                status = item.status,
                priority = item.priority,
                deadline = item.deadline,
                createdAt = item.createdAt,
                updatedAt = item.updatedAt,
                estimatedMinutes = item.estimatedMinutes,

                // Programming
                commitsCount = (item as? ProgrammingTask)?.commitsCount,
                requiredCommits = (item as? ProgrammingTask)?.requiredCommits,
                issuesResolved = (item as? ProgrammingTask)?.issuesResolved,
                requiredClosedIssues = (item as? ProgrammingTask)?.requiredIssues,
                testsPassed = (item as? ProgrammingTask)?.testsPassed,

                // Reading
                readPages = (item as? ReadingTask)?.readPages,
                totalPages = (item as? ReadingTask)?.totalPages,

                // Seminar
                seminarTopic = null,
                presentationRequired = null,
                topicSelected = (item as? SeminarTask)?.topicSelected,
                materialsCollected = (item as? SeminarTask)?.materialsCollected,
                speechPrepared = (item as? SeminarTask)?.speechPrepared,
                slidesPrepared = (item as? SeminarTask)?.slidesPrepared,
                rehearsalDone = (item as? SeminarTask)?.rehearsalDone,
                
                // Project
                projectGoal = (item as? ProjectTask)?.description
            ))

            if (item is AtomicWorkItem) {
                item.checklist.forEachIndexed { i, ch ->
                    bundleChecklist.add(ChecklistItemRecord(0, item.id, ch.text, ch.isCompleted, i))
                }
            }

            if (item is ExamTask) {
                item.topics.forEachIndexed { i, t ->
                    bundleTopics.add(ExamTopicRecord(0, item.id, t.name, t.confidence, i))
                }
            }

            item.attachments.forEach {
                bundleAttachments.add(AttachmentPersistenceMapper.mapToRecord(item.id, it))
            }

            item.logs.forEach {
                bundleLogs.add(WorkLogEntryRecord(
                    id = it.id,
                    workItemId = item.id,
                    message = it.message,
                    createdAt = it.createdAt,
                    minutesSpent = it.minutesSpent
                ))
            }

            if (item is CompositeWorkItem) {
                item.subTasks.forEachIndexed { i, child ->
                    process(child, item.id, i)
                }
            }
        }

        workItems.forEachIndexed { i, it -> process(it, null, i) }

        return PersistenceBundle(
            workItems = bundleWorkItems,
            checklistItems = bundleChecklist,
            examTopics = bundleTopics,
            attachments = bundleAttachments,
            logs = bundleLogs
        )
    }

    // --- Persistence to Domain ---

    fun restoreHierarchy(
        bundle: PersistenceBundle
    ): List<WorkItem> {
        val checklistMap = bundle.checklistItems.groupBy { it.workItemId }
        val topicsMap = bundle.examTopics.groupBy { it.workItemId }
        val attachmentsMap = bundle.attachments.groupBy { it.workItemId }
        val logsMap = bundle.logs.groupBy { it.workItemId }

        // 1. Create instances
        val domainMap = bundle.workItems.associate { record ->
            record.id to restoreConcrete(record).apply {
                if (this is AtomicWorkItem) {
                    checklistMap[record.id]?.sortedBy { it.sortOrder }?.forEach {
                        addChecklistItem(ChecklistItem(it.text, it.isCompleted))
                    }
                }
                if (this is ExamTask) {
                    topicsMap[record.id]?.sortedBy { it.sortOrder }?.forEach {
                        topics.add(ExamTopic(it.name, it.confidence))
                    }
                }
                attachmentsMap[record.id]?.forEach { 
                    addAttachment(AttachmentPersistenceMapper.restore(it)) 
                }
                logsMap[record.id]?.forEach {
                    addLog(WorkLogEntry(it.id, it.message, it.createdAt, it.minutesSpent))
                }
            }
        }

        // 2. Build tree with sorted children
        bundle.workItems.sortedBy { it.sortOrder }.forEach { record ->
            if (record.parentId != null) {
                val parent = domainMap[record.parentId]
                val child = domainMap[record.id]
                if (parent is CompositeWorkItem && child != null) {
                    parent.addSubTask(child)
                }
            }
        }

        // 3. Return sorted roots
        return bundle.workItems
            .filter { it.parentId == null }
            .sortedBy { it.sortOrder }
            .mapNotNull { domainMap[it.id] }
    }

    private fun restoreConcrete(record: WorkItemRecord): WorkItem {
        return when (record.type) {
            WorkItemType.PROGRAMMING -> ProgrammingTask(
                id = record.id,
                title = record.title,
                description = record.description,
                commitsCount = record.commitsCount ?: 0,
                requiredCommits = record.requiredCommits ?: 5,
                issuesResolved = record.issuesResolved ?: 0,
                requiredIssues = record.requiredClosedIssues ?: 2,
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
}
