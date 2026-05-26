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

    fun addChecklistItem(text: String) {
        if (text.isBlank()) return
        _checklist.add(ChecklistItem(text.trim()))
        this.updatedAt = LocalDateTime.now()
    }

    fun addChecklistItem(item: ChecklistItem) {
        _checklist.add(item)
        this.updatedAt = LocalDateTime.now()
    }

    fun updateChecklistItem(index: Int, text: String) {
        if (index !in _checklist.indices || text.isBlank()) return
        _checklist[index] = _checklist[index].copy(text = text.trim())
        this.updatedAt = LocalDateTime.now()
    }

    fun setChecklistItemCompleted(index: Int, completed: Boolean) {
        if (index !in _checklist.indices) return
        _checklist[index].isCompleted = completed
        this.updatedAt = LocalDateTime.now()
    }

    fun removeChecklistItem(index: Int) {
        if (index !in _checklist.indices) return
        _checklist.removeAt(index)
        this.updatedAt = LocalDateTime.now()
    }

    protected fun getChecklistProgress(): Double {
        if (_checklist.isEmpty()) return 0.0
        val completed = _checklist.count { it.isCompleted }
        return completed.toDouble() / _checklist.size
    }

    protected fun getChecklistExplanation(): String {
        if (_checklist.isEmpty()) return "No checklist items"
        val completed = _checklist.count { it.isCompleted }
        return "$completed/${_checklist.size} checklist items completed"
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
