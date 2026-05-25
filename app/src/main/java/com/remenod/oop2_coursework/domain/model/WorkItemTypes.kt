package com.remenod.oop2_coursework.domain.model

import java.time.LocalDateTime

abstract class AtomicWorkItem(
    id: Long,
    title: String,
    description: String,
    status: WorkStatus = WorkStatus.CREATED,
    priority: Priority = Priority.NORMAL,
    deadline: LocalDateTime? = null,
    createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),
    estimatedMinutes: Int = 0
) : WorkItem(id, title, description, status, priority, deadline, createdAt, updatedAt, estimatedMinutes) {
    
    private val _checklist = mutableListOf<ChecklistItem>()
    val checklist: List<ChecklistItem> get() = _checklist

    fun addChecklistItem(item: ChecklistItem) {
        _checklist.add(item)
    }

    protected fun getChecklistProgress(): Double {
        if (_checklist.isEmpty()) return 1.0
        val completed = _checklist.count { it.isCompleted }
        return completed.toDouble() / _checklist.size
    }
}

data class ChecklistItem(
    val text: String,
    var isCompleted: Boolean = false
)

abstract class CompositeWorkItem(
    id: Long,
    title: String,
    description: String,
    status: WorkStatus = WorkStatus.CREATED,
    priority: Priority = Priority.NORMAL,
    deadline: LocalDateTime? = null,
    createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),
    estimatedMinutes: Int = 0
) : WorkItem(id, title, description, status, priority, deadline, createdAt, updatedAt, estimatedMinutes) {

    private val _subTasks = mutableListOf<WorkItem>()
    val subTasks: List<WorkItem> get() = _subTasks

    fun addSubTask(item: WorkItem) {
        _subTasks.add(item)
    }

    fun removeSubTask(item: WorkItem) {
        _subTasks.remove(item)
    }
}
