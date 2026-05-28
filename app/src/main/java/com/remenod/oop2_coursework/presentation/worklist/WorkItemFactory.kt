package com.remenod.oop2_coursework.presentation.worklist

import com.remenod.oop2_coursework.domain.model.ExamTask
import com.remenod.oop2_coursework.domain.model.GenericTask
import com.remenod.oop2_coursework.domain.model.ProjectTask
import com.remenod.oop2_coursework.domain.model.ReadingTask
import com.remenod.oop2_coursework.domain.model.SeminarTask
import com.remenod.oop2_coursework.domain.model.WorkItem
import com.remenod.oop2_coursework.domain.model.WorkItemType

object WorkItemFactory {
    fun createFrom(result: WorkItemEditResult): WorkItem {
        val item = when (result.type) {
            WorkItemType.PROJECT -> ProjectTask(0, result.title, result.description)
            WorkItemType.READING -> {
                val readPages = result.readPages ?: 0
                val totalPages = result.totalPages ?: 100
                ReadingTask(
                    id = 0,
                    title = result.title,
                    description = result.description,
                    readPages = readPages,
                    totalPages = totalPages
                ).also {
                    it.updatePages(readPages, totalPages)
                }
            }
            WorkItemType.PROGRAMMING -> GenericTask(0, result.title, result.description)
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
