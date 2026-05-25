package com.remenod.oop2_coursework.domain.repository

import com.remenod.oop2_coursework.domain.model.Discipline
import com.remenod.oop2_coursework.domain.model.WorkItem
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeDisciplines(): Flow<List<Discipline>>
    fun observeDiscipline(id: Long): Flow<Discipline?>
    fun observeWorkItem(id: Long): Flow<WorkItem?>

    suspend fun addDiscipline(discipline: Discipline)
    suspend fun addWorkItem(disciplineId: Long, item: WorkItem)
    suspend fun updateWorkItem(item: WorkItem)
    suspend fun deleteWorkItem(id: Long)
}
