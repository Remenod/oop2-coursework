package com.remenod.oop2_coursework.domain.model

/**
 * Extension to flatten a WorkItem tree into a flat list recursively.
 */
fun WorkItem.flatten(): List<WorkItem> {
    return if (this is CompositeWorkItem) {
        listOf(this) + subTasks.flatMap { it.flatten() }
    } else {
        listOf(this)
    }
}
