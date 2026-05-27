package com.remenod.oop2_coursework.data.repository

import com.remenod.oop2_coursework.data.persistence.local.LocalTaskStorage
import com.remenod.oop2_coursework.domain.model.Attachment
import com.remenod.oop2_coursework.domain.model.Discipline
import com.remenod.oop2_coursework.domain.model.WorkItem
import com.remenod.oop2_coursework.domain.model.WorkLogEntry
import com.remenod.oop2_coursework.domain.model.WorkStatus
import com.remenod.oop2_coursework.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class FileBackedTaskRepository(
    private val delegate: InMemoryTaskRepository,
    private val storage: LocalTaskStorage
) : TaskRepository {
    private val saveMutex = Mutex()

    override fun observeDisciplines(): Flow<List<Discipline>> = delegate.observeDisciplines()

    override fun observeDiscipline(id: Long): Flow<Discipline?> = delegate.observeDiscipline(id)

    override fun observeWorkItem(id: Long): Flow<WorkItem?> = delegate.observeWorkItem(id)

    override suspend fun addDiscipline(discipline: Discipline): Discipline {
        return saveAfterChange {
            delegate.addDiscipline(discipline)
        }
    }

    override suspend fun updateDiscipline(discipline: Discipline) {
        saveAfterChange {
            delegate.updateDiscipline(discipline)
        }
    }

    override suspend fun deleteDiscipline(id: Long) {
        saveAfterChange {
            delegate.deleteDiscipline(id)
        }
    }

    override suspend fun addRootWorkItem(disciplineId: Long, item: WorkItem): WorkItem {
        return saveAfterChange {
            delegate.addRootWorkItem(disciplineId, item)
        }
    }

    override suspend fun addSubTask(parentId: Long, item: WorkItem): WorkItem {
        return saveAfterChange {
            delegate.addSubTask(parentId, item)
        }
    }

    override suspend fun updateWorkItem(item: WorkItem) {
        saveAfterChange {
            delegate.updateWorkItem(item)
        }
    }

    override suspend fun deleteWorkItem(id: Long) {
        saveAfterChange {
            delegate.deleteWorkItem(id)
        }
    }

    override suspend fun changeWorkItemStatus(id: Long, status: WorkStatus) {
        saveAfterChange {
            delegate.changeWorkItemStatus(id, status)
        }
    }

    override suspend fun addAttachment(workItemId: Long, attachment: Attachment): Attachment {
        return saveAfterChange {
            delegate.addAttachment(workItemId, attachment)
        }
    }

    override suspend fun removeAttachment(workItemId: Long, attachmentId: Long) {
        saveAfterChange {
            delegate.removeAttachment(workItemId, attachmentId)
        }
    }

    override suspend fun addWorkLogEntry(workItemId: Long, entry: WorkLogEntry): WorkLogEntry {
        return saveAfterChange {
            delegate.addWorkLogEntry(workItemId, entry)
        }
    }

    override suspend fun removeWorkLogEntry(workItemId: Long, entryId: Long) {
        saveAfterChange {
            delegate.removeWorkLogEntry(workItemId, entryId)
        }
    }

    private suspend fun <T> saveAfterChange(change: suspend () -> T): T {
        return saveMutex.withLock {
            val result = change()
            val snapshot = delegate.getDisciplinesSnapshot()
            withContext(Dispatchers.IO) {
                storage.save(snapshot)
            }
            result
        }
    }
}
