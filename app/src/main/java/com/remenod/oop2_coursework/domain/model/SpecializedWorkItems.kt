package com.remenod.oop2_coursework.domain.model

class ProgrammingTask(
    id: Long,
    title: String,
    description: String,
    var commitsCount: Int = 0,
    var requiredCommits: Int = 5,
    var issuesResolved: Int = 0,
    var requiredIssues: Int = 2,
    var testsPassed: Double = 0.0, // 0.0 to 1.0
    var repositoryUrl: String? = null,
    var branch: String? = null,
    estimatedMinutes: Int = 0
) : AtomicWorkItem(id, title, description, estimatedMinutes = estimatedMinutes) {
    
    override fun calculateProgress(): ProgressSnapshot {
        val components = mutableListOf<Pair<Double, Double>>()

        if (checklist.isNotEmpty()) {
            components += getChecklistProgress() to 0.25
        }

        if (requiredCommits > 0) {
            components += (commitsCount.toDouble() / requiredCommits).coerceIn(0.0, 1.0) to 0.25
        }

        if (requiredIssues > 0) {
            components += (issuesResolved.toDouble() / requiredIssues).coerceIn(0.0, 1.0) to 0.25
        }

        components += testsPassed.coerceIn(0.0, 1.0) to 0.25

        val totalWeight = components.sumOf { it.second }
        val percent = if (totalWeight == 0.0) 0.0 else components.sumOf { it.first * it.second } / totalWeight
        
        val explanation = buildString {
            append("$commitsCount/$requiredCommits commits, ")
            append("$issuesResolved/$requiredIssues issues, ")
            append("${(testsPassed * 100).toInt()}% tests")
            if (checklist.isNotEmpty()) {
                append(", ${(getChecklistProgress() * 100).toInt()}% checklist")
            }
        }

        return ProgressSnapshot(percent, explanation)
    }

    override fun validateCompletion(): Boolean {
        val commitsOk = requiredCommits <= 0 || commitsCount >= requiredCommits
        val issuesOk = requiredIssues <= 0 || issuesResolved >= requiredIssues
        val testsOk = testsPassed >= 1.0
        val checklistOk = checklist.isEmpty() || getChecklistProgress() >= 1.0

        return commitsOk && issuesOk && testsOk && checklistOk
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
        return rehearsalDone
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
        val safeTotal = totalPages.coerceAtLeast(1)
        val safeRead = readPages.coerceIn(0, safeTotal)
        val percent = safeRead.toDouble() / safeTotal
        return ProgressSnapshot(percent, "Read $safeRead of $safeTotal pages")
    }

    fun updatePages(readPages: Int, totalPages: Int) {
        require(totalPages > 0) { "Total pages must be positive" }
        require(readPages in 0..totalPages) { "Read pages must be within 0 and total pages" }

        this.readPages = readPages
        this.totalPages = totalPages
        touch()
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
        return when {
            status == WorkStatus.DONE -> ProgressSnapshot(1.0, "Completed")
            checklist.isEmpty() -> ProgressSnapshot(0.0, "No checklist items")
            else -> ProgressSnapshot(getChecklistProgress(), getChecklistExplanation())
        }
    }

    override fun validateCompletion(): Boolean {
        return checklist.isEmpty() || getChecklistProgress() >= 1.0
    }
}

class ProjectTask(
    id: Long,
    title: String,
    description: String
) : CompositeWorkItem(id, title, description) {
    
    override fun calculateProgress(): ProgressSnapshot {
        val activeSubTasks = subTasks.filter { it.status != WorkStatus.CANCELLED }

        if (activeSubTasks.isEmpty()) {
            return ProgressSnapshot(0.0, "No active sub-tasks")
        }
        
        val totalEstimated = activeSubTasks.sumOf { it.estimatedMinutes.toDouble() }
        val avgProgress = if (totalEstimated <= 0.0) {
            activeSubTasks.map { it.getProgress() }.average()
        } else {
            activeSubTasks.sumOf { it.getProgress() * (it.estimatedMinutes.toDouble() / totalEstimated) }
        }
        
        val completed = activeSubTasks.count { it.status == WorkStatus.DONE }
        return ProgressSnapshot(
            percent = avgProgress.coerceIn(0.0, 1.0),
            explanation = "$completed/${activeSubTasks.size} active sub-tasks completed"
        )
    }

    override fun validateCompletion(): Boolean {
        return subTasks.isNotEmpty() && subTasks.all { it.status == WorkStatus.DONE || it.status == WorkStatus.CANCELLED }
    }
}
