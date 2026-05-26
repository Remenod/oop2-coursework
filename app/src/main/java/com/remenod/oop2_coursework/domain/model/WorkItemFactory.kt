package com.remenod.oop2_coursework.domain.model

import com.remenod.oop2_coursework.presentation.worklist.WorkItemEditResult

object WorkItemFactory {
    fun createFrom(result: WorkItemEditResult): WorkItem {
        val item = when (result.type) {
            WorkItemType.PROJECT -> ProjectTask(0, result.title, result.description)
            WorkItemType.READING -> ReadingTask(
                id = 0,
                title = result.title,
                description = result.description,
                readPages = result.readPages ?: 0,
                totalPages = result.totalPages ?: 100
            )
            WorkItemType.PROGRAMMING -> ProgrammingTask(
                id = 0,
                title = result.title,
                description = result.description,
                commitsCount = result.commitsCount ?: 0,
                requiredCommits = result.requiredCommits ?: 5,
                issuesResolved = result.issuesResolved ?: 0,
                requiredIssues = result.requiredIssues ?: 2,
                testsPassed = result.testsPassed ?: 0.0
            )
            WorkItemType.EXAM -> ExamTask(0, result.title, result.description)
            WorkItemType.SEMINAR -> SeminarTask(0, result.title, result.description)
            WorkItemType.GENERIC -> GenericTask(0, result.title, result.description)
        }

        item.updateMetadata(
            title = result.title,
            description = result.description,
            status = result.status,
            priority = result.priority,
            deadline = result.deadline,
            estimatedMinutes = result.estimatedMinutes
        )

        return item
    }
}
