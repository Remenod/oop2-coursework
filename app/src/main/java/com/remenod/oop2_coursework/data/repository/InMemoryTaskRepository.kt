package com.remenod.oop2_coursework.data.repository

import com.remenod.oop2_coursework.domain.model.Discipline
import com.remenod.oop2_coursework.domain.model.WorkItem
import com.remenod.oop2_coursework.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryTaskRepository : TaskRepository {
    private val _disciplines = MutableStateFlow<List<Discipline>>(emptyList())

    override fun observeDisciplines(): Flow<List<Discipline>> = _disciplines

    override fun observeDiscipline(id: Long): Flow<Discipline?> = 
        _disciplines.map { list -> list.find { it.id == id } }

    override fun observeWorkItem(id: Long): Flow<WorkItem?> = 
        _disciplines.map { list -> 
            list.flatMap { it.workItems }.find { it.id == id } 
        }

    override suspend fun addDiscipline(discipline: Discipline) {
        _disciplines.value = _disciplines.value + discipline
    }

    override suspend fun addWorkItem(disciplineId: Long, item: WorkItem) {
        val current = _disciplines.value.toMutableList()
        val index = current.indexOfFirst { it.id == disciplineId }
        if (index != -1) {
            val discipline = current[index]
            discipline.addWorkItem(item)
            _disciplines.value = current // Trigger update
        }
    }

    override suspend fun updateWorkItem(item: WorkItem) {
        // Since we are using references in-memory, updating the object might be enough,
        // but we trigger the flow update by re-assigning the list.
        _disciplines.value = _disciplines.value.toList()
    }

    override suspend fun deleteWorkItem(id: Long) {
        // Complex because we need to find which discipline it belongs to
        // For in-memory simplification, we can just search and remove
        _disciplines.value.forEach { it.removeWorkItem(it.workItems.find { item -> item.id == id } ?: return@forEach) }
        _disciplines.value = _disciplines.value.toList()
    }
}
