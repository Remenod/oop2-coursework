package com.remenod.oop2_coursework.data.repository

import com.remenod.oop2_coursework.domain.model.*
import com.remenod.oop2_coursework.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.concurrent.atomic.AtomicLong

class InMemoryTaskRepository : TaskRepository {
    private val _disciplines = MutableStateFlow<List<Discipline>>(emptyList())
    private val idGenerator = AtomicLong(2000L) // Start above demo data IDs

    override fun observeDisciplines(): Flow<List<Discipline>> = _disciplines

    override fun observeDiscipline(id: Long): Flow<Discipline?> = 
        _disciplines.map { list -> list.find { it.id == id } }

    override fun observeWorkItem(id: Long): Flow<WorkItem?> = 
        _disciplines.map { list -> 
            list.flatMap { it.workItems }.findRecursive(id) 
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

    override suspend fun addDiscipline(discipline: Discipline): Discipline {
        if (discipline.id == 0L) {
            setId(discipline, idGenerator.incrementAndGet())
        }
        _disciplines.value = _disciplines.value + discipline
        return discipline
    }

    override suspend fun updateDiscipline(discipline: Discipline) {
        val current = _disciplines.value.toMutableList()
        val index = current.indexOfFirst { it.id == discipline.id }
        if (index != -1) {
            current[index] = discipline
            _disciplines.value = current.toList()
        }
    }

    override suspend fun deleteDiscipline(id: Long) {
        _disciplines.value = _disciplines.value.filterNot { it.id == id }
    }

    override suspend fun addRootWorkItem(disciplineId: Long, item: WorkItem): WorkItem {
        val current = _disciplines.value.toMutableList()
        val discipline = current.find { it.id == disciplineId } ?: return item
        
        if (item.id == 0L) setId(item, idGenerator.incrementAndGet())
        discipline.addWorkItem(item)
        _disciplines.value = current.toList()
        return item
    }

    override suspend fun addSubTask(parentId: Long, item: WorkItem): WorkItem {
        val current = _disciplines.value.toMutableList()
        val parent = current.flatMap { it.workItems }.findRecursive(parentId)
        
        require(parent is CompositeWorkItem) { "Subtasks can only be added to CompositeWorkItem types" }
        
        if (item.id == 0L) setId(item, idGenerator.incrementAndGet())
        parent.addSubTask(item)
        _disciplines.value = current.toList()
        return item
    }

    override suspend fun updateWorkItem(item: WorkItem) {
        // Domain objects are modified, but we need to notify listeners
        _disciplines.value = _disciplines.value.toList()
    }

    override suspend fun deleteWorkItem(id: Long) {
        val current = _disciplines.value.toMutableList()
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
            _disciplines.value = current.toList()
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
        val item = _disciplines.value.flatMap { it.workItems }.findRecursive(id)
        item?.let {
            setStatus(it, status)
            _disciplines.value = _disciplines.value.toList()
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
