package com.remenod.oop2_coursework.domain.model

import java.time.LocalDateTime

data class ExamTopic(val name: String, val confidence: Int) // 0 to 100

class ExamTask(
    id: Long,
    title: String,
    description: String,
    val topics: MutableList<ExamTopic> = mutableListOf(),
    status: WorkStatus = WorkStatus.CREATED,
    priority: Priority = Priority.NORMAL,
    deadline: LocalDateTime? = null,
    createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),
    estimatedMinutes: Int = 0
) : AtomicWorkItem(
    id = id,
    title = title,
    description = description,
    status = status,
    priority = priority,
    deadline = deadline,
    createdAt = createdAt,
    updatedAt = updatedAt,
    estimatedMinutes = estimatedMinutes
) {
    
    override fun calculateProgress(): ProgressSnapshot {
        if (topics.isEmpty()) return ProgressSnapshot(0.0, "No topics defined")
        val avgConfidence = topics.map { it.confidence }.average()
        val weakTopics = topics.count { it.confidence < MIN_TOPIC_CONFIDENCE }
        val percent = avgConfidence / 100.0
        return ProgressSnapshot(
            percent,
            "Average confidence: ${(percent * 100).toInt()}% across ${topics.size} topics, weak topics: $weakTopics"
        )
    }

    fun addTopic(name: String, confidence: Int = DEFAULT_TOPIC_CONFIDENCE) {
        require(name.isNotBlank()) { "Topic name cannot be blank" }
        require(confidence in 0..100) { "Confidence must be between 0 and 100" }

        topics.add(ExamTopic(name.trim(), confidence))
        touch()
    }

    fun updateTopicConfidence(index: Int, confidence: Int) {
        require(confidence in 0..100) { "Confidence must be between 0 and 100" }
        require(index in topics.indices) { "Topic index is out of bounds" }

        topics[index] = topics[index].copy(confidence = confidence)
        touch()
    }

    fun removeTopic(index: Int): ExamTopic {
        require(index in topics.indices) { "Topic index is out of bounds" }

        val removed = topics.removeAt(index)
        touch()
        return removed
    }

    override fun validateCompletion(): Boolean {
        return topics.isNotEmpty() &&
                topics.all { it.confidence >= MIN_TOPIC_CONFIDENCE } &&
                calculateProgress().percent >= MIN_AVERAGE_CONFIDENCE
    }

    companion object {
        private const val DEFAULT_TOPIC_CONFIDENCE = 50
        private const val MIN_TOPIC_CONFIDENCE = 60
        private const val MIN_AVERAGE_CONFIDENCE = 0.8
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
    var rehearsalDone: Boolean = false,
    status: WorkStatus = WorkStatus.CREATED,
    priority: Priority = Priority.NORMAL,
    deadline: LocalDateTime? = null,
    createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),
    estimatedMinutes: Int = 0
) : AtomicWorkItem(
    id = id,
    title = title,
    description = description,
    status = status,
    priority = priority,
    deadline = deadline,
    createdAt = createdAt,
    updatedAt = updatedAt,
    estimatedMinutes = estimatedMinutes
) {
    
    override fun calculateProgress(): ProgressSnapshot {
        val stages = stageStates()
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
        return stageStates().all { it }
    }

    fun setStage(stage: SeminarStageType, completed: Boolean) {
        when (stage) {
            SeminarStageType.TOPIC_SELECTED -> topicSelected = completed
            SeminarStageType.MATERIALS_COLLECTED -> materialsCollected = completed
            SeminarStageType.SPEECH_PREPARED -> speechPrepared = completed
            SeminarStageType.SLIDES_PREPARED -> slidesPrepared = completed
            SeminarStageType.REHEARSAL_DONE -> rehearsalDone = completed
        }
        touch()
    }

    private fun stageStates(): List<Boolean> {
        return listOf(topicSelected, materialsCollected, speechPrepared, slidesPrepared, rehearsalDone)
    }
}

class ReadingTask(
    id: Long,
    title: String,
    description: String,
    var readPages: Int = 0,
    var totalPages: Int = 100,
    status: WorkStatus = WorkStatus.CREATED,
    priority: Priority = Priority.NORMAL,
    deadline: LocalDateTime? = null,
    createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),
    estimatedMinutes: Int = 0
) : AtomicWorkItem(
    id = id,
    title = title,
    description = description,
    status = status,
    priority = priority,
    deadline = deadline,
    createdAt = createdAt,
    updatedAt = updatedAt,
    estimatedMinutes = estimatedMinutes
) {
    
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
    description: String,
    status: WorkStatus = WorkStatus.CREATED,
    priority: Priority = Priority.NORMAL,
    deadline: LocalDateTime? = null,
    createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),
    estimatedMinutes: Int = 0
) : AtomicWorkItem(
    id = id,
    title = title,
    description = description,
    status = status,
    priority = priority,
    deadline = deadline,
    createdAt = createdAt,
    updatedAt = updatedAt,
    estimatedMinutes = estimatedMinutes
) {
    
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
    description: String,
    status: WorkStatus = WorkStatus.CREATED,
    priority: Priority = Priority.NORMAL,
    deadline: LocalDateTime? = null,
    createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),
    estimatedMinutes: Int = 0
) : CompositeWorkItem(
    id = id,
    title = title,
    description = description,
    status = status,
    priority = priority,
    deadline = deadline,
    createdAt = createdAt,
    updatedAt = updatedAt,
    estimatedMinutes = estimatedMinutes
) {
    
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
