package com.remenod.oop2_coursework.domain.model

import java.time.LocalDateTime

class ProgrammingTask(
    id: String,
    title: String,
    description: String,
    var commitsCount: Int = 0,   // Probably I would not figure out how to make dis automatic
    var issuesResolved: Int = 0, // Probably I would not figure out how to make dis automatic
    var testsPassed: Double = 0.0 // 0.0 to 1.0
) : AtomicWorkItem(id, title, description) {
    
    override fun calculateProgress(): Double {
        val checklistProgress = getChecklistProgress()
        // Mocking commitProgress and issueProgress as we don't have real values here
        val commitProgress = if (commitsCount > 5) 1.0 else commitsCount / 5.0
        val issueProgress = if (issuesResolved > 2) 1.0 else issuesResolved / 2.0
        val testProgress = testsPassed

        return checklistProgress * 0.4 + commitProgress * 0.25 + issueProgress * 0.25 + testProgress * 0.1
    }

    override fun validateCompletion(): Boolean {
        return calculateProgress() >= 0.95 // Require 95% progress to complete
    }
}

class ExamTask(
    id: String,
    title: String,
    description: String,
    val topics: List<TopicConfidence> = emptyList()
) : AtomicWorkItem(id, title, description) {
    
    data class TopicConfidence(val name: String, val confidence: Int) // 0 to 100

    override fun calculateProgress(): Double {
        if (topics.isEmpty()) return 0.0
        return topics.map { it.confidence }.average() / 100.0
    }

    override fun validateCompletion(): Boolean {
        return calculateProgress() >= 0.8
    }
}

class SeminarTask(
    id: String,
    title: String,
    description: String,
    var topicSelected: Boolean = false,
    var materialsCollected: Boolean = false,
    var speechPrepared: Boolean = false,
    var slidesPrepared: Boolean = false,
    var rehearsalDone: Boolean = false
) : AtomicWorkItem(id, title, description) {
    
    override fun calculateProgress(): Double {
        val stages = listOf(topicSelected, materialsCollected, speechPrepared, slidesPrepared, rehearsalDone)
        return stages.count { it }.toDouble() / stages.size
    }

    override fun validateCompletion(): Boolean {
        return rehearsalDone // Must have done rehearsal to complete (idk, probably not actually)
    }
}

class ReadingTask(
    id: String,
    title: String,
    description: String,
    var readPages: Int = 0,
    var totalPages: Int = 100
) : AtomicWorkItem(id, title, description) {
    
    override fun calculateProgress(): Double {
        if (totalPages == 0) return 1.0
        return readPages.toDouble() / totalPages
    }

    override fun validateCompletion(): Boolean {
        return readPages >= totalPages
    }
}

class GenericTask(
    id: String,
    title: String,
    description: String
) : AtomicWorkItem(id, title, description) {
    
    override fun calculateProgress(): Double {
        return if (status == WorkStatus.DONE) 1.0 else getChecklistProgress()
    }

    override fun validateCompletion(): Boolean {
        return getChecklistProgress() >= 1.0
    }
}

class ProjectTask(
    id: String,
    title: String,
    description: String
) : CompositeWorkItem(id, title, description) {
    
    override fun calculateProgress(): Double {
        if (subTasks.isEmpty()) return 0.0
        
        val totalEstimated = subTasks.sumOf { it.estimatedMinutes.toDouble() }
        if (totalEstimated == 0.0) {
            return subTasks.map { it.getProgress() }.average()
        }
        
        return subTasks.sumOf { it.getProgress() * (it.estimatedMinutes / totalEstimated) }
    }

    override fun validateCompletion(): Boolean {
        return subTasks.isNotEmpty() && subTasks.all { it.status == WorkStatus.DONE }
    }
}
