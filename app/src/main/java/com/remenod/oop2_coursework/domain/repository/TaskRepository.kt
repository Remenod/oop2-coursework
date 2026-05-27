package com.remenod.oop2_coursework.domain.repository

import com.remenod.oop2_coursework.domain.model.*
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeDisciplines(): Flow<List<Discipline>>
    fun observeDiscipline(id: Long): Flow<Discipline?>
    fun observeWorkItem(id: Long): Flow<WorkItem?>

    fun getDisciplinesSnapshot(): List<Discipline>
    fun getDisciplineSnapshot(id: Long): Discipline?
    fun getWorkItemSnapshot(id: Long): WorkItem?

    suspend fun addDiscipline(discipline: Discipline): Discipline
    suspend fun updateDiscipline(discipline: Discipline)
    suspend fun deleteDiscipline(id: Long)

    suspend fun addRootWorkItem(disciplineId: Long, item: WorkItem): WorkItem
    suspend fun addSubTask(parentId: Long, item: WorkItem): WorkItem

    suspend fun updateWorkItem(item: WorkItem)
    suspend fun deleteWorkItem(id: Long)

    suspend fun changeWorkItemStatus(id: Long, status: WorkStatus)

    // Attachments
    suspend fun addAttachment(workItemId: Long, attachment: Attachment): Attachment
    suspend fun removeAttachment(workItemId: Long, attachmentId: Long)

    // Work Logs
    suspend fun addWorkLogEntry(workItemId: Long, entry: WorkLogEntry): WorkLogEntry
    suspend fun removeWorkLogEntry(workItemId: Long, entryId: Long)
}
