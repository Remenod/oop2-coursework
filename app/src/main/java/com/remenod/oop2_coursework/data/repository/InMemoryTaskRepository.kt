package com.remenod.oop2_coursework.data.repository

import com.remenod.oop2_coursework.domain.model.*
import com.remenod.oop2_coursework.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.concurrent.atomic.AtomicLong

class InMemoryTaskRepository : TaskRepository {
    
    private data class RepositoryState(
        val revision: Long = 0L,
        val disciplines: List<Discipline> = emptyList()
    )

    private val _state = MutableStateFlow(RepositoryState())
    private val idGenerator = AtomicLong(2000L)

    override fun observeDisciplines(): Flow<List<Discipline>> = 
        _state.map { it.disciplines }

    override fun observeDiscipline(id: Long): Flow<Discipline?> = 
        _state.map { state -> state.disciplines.find { it.id == id } }

    override fun observeWorkItem(id: Long): Flow<WorkItem?> = 
        _state.map { state -> 
            state.disciplines.flatMap { it.workItems }.findRecursive(id) 
        }

    private fun List<WorkItem>.findRecursive(id: Long): WorkItem? {
        this.forEach { 
            if (it.id == id) return it
            if (it is CompositeWorkItem) {
                val found = it.subTasks.findRecursive(id)
                if (found != null) return found
            }
        }
        return null
    }

    private fun notifyChanged() {
        _state.value = _state.value.copy(revision = _state.value.revision + 1)
    }

    private fun setDisciplines(value: List<Discipline>) {
        _state.value = RepositoryState(
            revision = _state.value.revision + 1,
            disciplines = value
        )
    }

    override suspend fun addDiscipline(discipline: Discipline): Discipline {
        if (discipline.id == 0L) {
            setId(discipline, idGenerator.incrementAndGet())
        }
        setDisciplines(_state.value.disciplines + discipline)
        return discipline
    }

    override suspend fun updateDiscipline(discipline: Discipline) {
        val current = _state.value.disciplines.toMutableList()
        val index = current.indexOfFirst { it.id == discipline.id }
        if (index != -1) {
            current[index] = discipline
            setDisciplines(current)
        }
    }

    override suspend fun deleteDiscipline(id: Long) {
        setDisciplines(_state.value.disciplines.filterNot { it.id == id })
    }

    override suspend fun addRootWorkItem(disciplineId: Long, item: WorkItem): WorkItem {
        val current = _state.value.disciplines.toMutableList()
        val discipline = current.find { it.id == disciplineId } ?: return item
        
        if (item.id == 0L) setId(item, idGenerator.incrementAndGet())
        discipline.addWorkItem(item)
        setDisciplines(current)
        return item
    }

    override suspend fun addSubTask(parentId: Long, item: WorkItem): WorkItem {
        val current = _state.value.disciplines.toMutableList()
        val parent = current.flatMap { it.workItems }.findRecursive(parentId)
        
        require(parent is CompositeWorkItem) { "Subtasks can only be added to CompositeWorkItem types" }
        
        if (item.id == 0L) setId(item, idGenerator.incrementAndGet())
        parent.addSubTask(item)
        setDisciplines(current)
        return item
    }

    override suspend fun updateWorkItem(item: WorkItem) {
        notifyChanged()
    }

    override suspend fun deleteWorkItem(id: Long) {
        val current = _state.value.disciplines.toMutableList()
        var found = false
        
        current.forEach { discipline ->
            val rootItem = discipline.workItems.find { it.id == id }
            if (rootItem != null) {
                discipline.removeWorkItem(rootItem)
                found = true
            } else {
                if (removeFromParentRecursive(discipline.workItems, id)) {
                    found = true
                }
            }
        }
        
        if (found) {
            setDisciplines(current)
        }
    }

    private fun removeFromParentRecursive(items: List<WorkItem>, id: Long): Boolean {
        items.forEach { item ->
            if (item is CompositeWorkItem) {
                val child = item.subTasks.find { it.id == id }
                if (child != null) {
                    item.removeSubTask(child)
                    return true
                }
                if (removeFromParentRecursive(item.subTasks, id)) {
                    return true
                }
            }
        }
        return false
    }

    override suspend fun changeWorkItemStatus(id: Long, status: WorkStatus) {
        val item = _state.value.disciplines.flatMap { it.workItems }.findRecursive(id)
        item?.let {
            setStatus(it, status)
            notifyChanged()
        }
    }

    private fun setId(obj: Any, id: Long) {
        var current: Class<*>? = obj.javaClass
        while (current != null) {
            try {
                val field = current.getDeclaredField("id")
                field.isAccessible = true
                field.set(obj, id)
                return
            } catch (_: NoSuchFieldException) {
                current = current.superclass
            } catch (_: Exception) {
                break
            }
        }
    }

    private fun setStatus(item: WorkItem, status: WorkStatus) {
        try {
            val field = WorkItem::class.java.getDeclaredField("status")
            field.isAccessible = true
            field.set(item, status)
        } catch (_: Exception) {}
    }
}
