package com.remenod.oop2_coursework.domain.model

class ProgrammingTask(
    id: Long,
    title: String,
    description: String,
    var commitsCount: Int = 0,   // Probably I would not figure out how to make dis automatic
    var issuesResolved: Int = 0, // Probably I would not figure out how to make dis automatic
    var testsPassed: Double = 0.0, // 0.0 to 1.0
    estimatedMinutes: Int = 0
) : AtomicWorkItem(id, title, description, estimatedMinutes = estimatedMinutes) {
    
    override fun calculateProgress(): ProgressSnapshot {
        val checklistProgress = getChecklistProgress()
        val commitProgress = (commitsCount / 5.0).coerceAtMost(1.0)
        val issueProgress = (issuesResolved / 2.0).coerceAtMost(1.0)
        val testProgress = testsPassed

        val totalProgress = checklistProgress * 0.4 + commitProgress * 0.25 + issueProgress * 0.25 + testProgress * 0.1
        
        val explanation = buildString {
            append("${(checklistProgress * 100).toInt()}% checklist, ")
            append("$commitsCount commits, ")
            append("$issuesResolved issues, ")
            append("${(testProgress * 100).toInt()}% tests")
        }

        return ProgressSnapshot(totalProgress, explanation)
    }

    override fun validateCompletion(): Boolean {
        return calculateProgress().percent >= 0.95
    }
}

data class ExamTopic(val name: String, val confidence: Int) // 0 to 100

class ExamTask(
    id: Long,
    title: String,
    description: String,
    val topics: MutableList<ExamTopic> = mutableListOf()
) : AtomicWorkItem(id, title, description) {
    
    override fun calculateProgress(): ProgressSnapshot {
        if (topics.isEmpty()) return ProgressSnapshot(0.0, "No topics defined")
        val avgConfidence = topics.map { it.confidence }.average()
        val percent = avgConfidence / 100.0
        return ProgressSnapshot(percent, "Average confidence: ${(percent * 100).toInt()}% across ${topics.size} topics")
    }

    override fun validateCompletion(): Boolean {
        return calculateProgress().percent >= 0.8
    }
}

class SeminarTask(
    id: Long,
    title: String,
    description: String,
    var topicSelected: Boolean = false,
    var materialsCollected: Boolean = false,
    var speechPrepared: Boolean = false,
    var slidesPrepared: Boolean = false,
    var rehearsalDone: Boolean = false
) : AtomicWorkItem(id, title, description) {
    
    override fun calculateProgress(): ProgressSnapshot {
        val stages = listOf(topicSelected, materialsCollected, speechPrepared, slidesPrepared, rehearsalDone)
        val completedCount = stages.count { it }
        val percent = completedCount.toDouble() / stages.size
        
        val nextStep = when {
            !topicSelected -> "Select topic"
            !materialsCollected -> "Collect materials"
            !speechPrepared -> "Prepare speech"
            !slidesPrepared -> "Prepare slides"
            !rehearsalDone -> "Do rehearsal"
            else -> "Ready"
        }

        return ProgressSnapshot(percent, "Stages: $completedCount/${stages.size}. Next: $nextStep")
    }

    override fun validateCompletion(): Boolean {
        return rehearsalDone // Must have done rehearsal to complete (idk, probably not actually)
    }
}

class ReadingTask(
    id: Long,
    title: String,
    description: String,
    var readPages: Int = 0,
    var totalPages: Int = 100
) : AtomicWorkItem(id, title, description) {
    
    override fun calculateProgress(): ProgressSnapshot {
        if (totalPages == 0) return ProgressSnapshot(1.0, "No pages to read")
        val percent = (readPages.toDouble() / totalPages).coerceAtMost(1.0)
        return ProgressSnapshot(percent, "Read $readPages of $totalPages pages")
    }

    override fun validateCompletion(): Boolean {
        return readPages >= totalPages
    }
}

class GenericTask(
    id: Long,
    title: String,
    description: String
) : AtomicWorkItem(id, title, description) {
    
    override fun calculateProgress(): ProgressSnapshot {
        return if (status == WorkStatus.DONE) {
            ProgressSnapshot(1.0, "Completed")
        } else {
            ProgressSnapshot(getChecklistProgress(), getChecklistExplanation())
        }
    }

    override fun validateCompletion(): Boolean {
        return getChecklistProgress() >= 1.0
    }
}

class ProjectTask(
    id: Long,
    title: String,
    description: String
) : CompositeWorkItem(id, title, description) {
    
    override fun calculateProgress(): ProgressSnapshot {
        
        val totalEstimated = subTasks.sumOf { it.estimatedMinutes.toDouble() }
        val avgProgress = if (totalEstimated == 0.0) {
            subTasks.map { it.getProgress() }.average()
        } else {
            subTasks.sumOf { it.getProgress() * (it.estimatedMinutes / totalEstimated) }
        }
        
        val completed = subTasks.count { it.status == WorkStatus.DONE }
        return ProgressSnapshot(avgProgress, "$completed/${subTasks.size} sub-tasks completed")
    }

    override fun validateCompletion(): Boolean {
        return subTasks.isNotEmpty() && subTasks.all { it.status == WorkStatus.DONE }
    }
}
