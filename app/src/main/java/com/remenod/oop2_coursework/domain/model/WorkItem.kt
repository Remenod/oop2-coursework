package com.remenod.oop2_coursework.domain.model

import com.remenod.oop2_coursework.domain.interfaces.Completable
import com.remenod.oop2_coursework.domain.interfaces.ProgressTrackable
import java.time.LocalDateTime

abstract class WorkItem(
    val id: String,
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

    fun addAttachment(attachment: Attachment) {
        _attachments.add(attachment)
        updatedAt = LocalDateTime.now()
    }

    fun removeAttachment(attachment: Attachment) {
        _attachments.remove(attachment)
        updatedAt = LocalDateTime.now()
    }

    fun addLog(entry: WorkLogEntry) {
        updatedAt = LocalDateTime.now()
    }

    fun isOverdue(now: LocalDateTime = LocalDateTime.now()): Boolean {
        return deadline?.isBefore(now) ?: false && status != WorkStatus.DONE
    }

    // Template Method
    final override fun getProgress(): Double {
        return calculateProgress()
    }

    protected abstract fun calculateProgress(): Double

    override fun canBeCompleted(): Boolean {
        return validateCompletion()
    }

    override fun complete() {
        if (canBeCompleted()) {
            status = WorkStatus.DONE
            updatedAt = LocalDateTime.now()
        }
    }

    abstract override fun validateCompletion(): Boolean
}
