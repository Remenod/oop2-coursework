package com.remenod.oop2_coursework.domain.model

import com.remenod.oop2_coursework.domain.interfaces.ProgressTrackable
import java.time.LocalDateTime

class Discipline(
    val id: String,
    var name: String,
    var teacherName: String,
    var semester: Int,
    var color: Int
) : ProgressTrackable {

    private val _workItems = mutableListOf<WorkItem>()
    val workItems: List<WorkItem> get() = _workItems

    fun addWorkItem(item: WorkItem) {
        _workItems.add(item)
    }

    fun removeWorkItem(item: WorkItem) {
        _workItems.remove(item)
    }

    override fun getProgress(): Double {
        if (_workItems.isEmpty()) return 1.0
        return _workItems.map { it.getProgress() }.average()
    }

    fun getOverdueItems(now: LocalDateTime = LocalDateTime.now()): List<WorkItem> {
        return _workItems.filter { it.isOverdue(now) }
    }
}
