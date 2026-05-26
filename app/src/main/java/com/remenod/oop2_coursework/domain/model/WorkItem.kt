package com.remenod.oop2_coursework.domain.model

import com.remenod.oop2_coursework.domain.interfaces.Completable
import com.remenod.oop2_coursework.domain.interfaces.ProgressTrackable
import java.time.LocalDateTime

abstract class WorkItem(
    val id: Long,
    var title: String,
    var description: String,
    var status: WorkStatus = WorkStatus.CREATED,
    var priority: Priority = Priority.NORMAL,
    var deadline: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    var estimatedMinutes: Int = 0
) : ProgressTrackable, Completable {

    private val _attachments = mutableListOf<Attachment>()
    val attachments: List<Attachment> get() = _attachments

    private val _logs = mutableListOf<WorkLogEntry>()
    val logs: List<WorkLogEntry> get() = _logs

    fun touch() {
        updatedAt = LocalDateTime.now()
    }

    fun changeStatus(newStatus: WorkStatus) {
        status = newStatus
        touch()
    }

    fun setUserStatus(newStatus: WorkStatus) {
        if (newStatus == WorkStatus.DONE) {
            complete()
        } else {
            changeStatus(newStatus)
        }
    }

    fun updateMetadata(
        title: String,
        description: String,
        status: WorkStatus,
        priority: Priority,
        deadline: LocalDateTime?,
        estimatedMinutes: Int
    ) {
        require(title.isNotBlank()) { "Title cannot be blank" }
        require(estimatedMinutes >= 0) { "Estimated time cannot be negative" }

        if (status == WorkStatus.DONE && !validateCompletion()) {
            throw IllegalStateException("Task cannot be marked as done yet")
        }

        this.title = title.trim()
        this.description = description.trim()
        this.priority = priority
        this.deadline = deadline
        this.estimatedMinutes = estimatedMinutes
        this.status = status
        touch()
    }

    fun addAttachment(attachment: Attachment) {
        _attachments.add(attachment)
        touch()
    }

    fun removeAttachment(attachment: Attachment) {
        _attachments.remove(attachment)
        touch()
    }

    fun addLog(entry: WorkLogEntry) {
        _logs.add(entry)
        touch()
    }

    fun removeLog(id: Long) {
        _logs.removeIf { it.id == id }
        touch()
    }

    fun isOverdue(now: LocalDateTime = LocalDateTime.now()): Boolean {
        return deadline != null && 
                deadline!!.isBefore(now) && 
                status != WorkStatus.DONE && 
                status != WorkStatus.CANCELLED
    }

    // Template Method
    final override fun getProgress(): Double {
        return getProgressSnapshot().percent
    }

    fun getProgressSnapshot(): ProgressSnapshot {
        return calculateProgress()
    }

    protected abstract fun calculateProgress(): ProgressSnapshot

    override fun canBeCompleted(): Boolean {
        return validateCompletion()
    }

    override fun complete() {
        if (canBeCompleted()) {
            status = WorkStatus.DONE
            touch()
        } else {
            throw IllegalStateException("Work item cannot be completed")
        }
    }

    abstract override fun validateCompletion(): Boolean
}
