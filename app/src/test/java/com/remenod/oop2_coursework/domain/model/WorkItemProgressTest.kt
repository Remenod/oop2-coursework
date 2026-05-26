package com.remenod.oop2_coursework.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkItemProgressTest {

    @Test
    fun testProgrammingTaskProgress() {
        val task = ProgrammingTask(1L, "Test", "Desc", commitsCount = 5, issuesResolved = 2, testsPassed = 1.0)
        // checklist: 100% (empty), weight 0.4 -> 0.4
        // commits: 100% (5/5), weight 0.25 -> 0.25
        // issues: 100% (2/2), weight 0.25 -> 0.25
        // tests: 100% (1.0), weight 0.1 -> 0.1
        // Total: 1.0
        assertEquals(1.0, task.getProgress(), 0.01)
    }

    @Test
    fun testExamTaskProgress() {
        val task = ExamTask(1L, "Test", "Desc", mutableListOf(
            ExamTopic("Topic 1", 100),
            ExamTopic("Topic 2", 50)
        ))
        assertEquals(0.75, task.getProgress(), 0.01)
    }

    @Test
    fun testSeminarTaskProgress() {
        val task = SeminarTask(1L, "Test", "Desc", topicSelected = true, materialsCollected = true)
        // 2 out of 5 stages
        assertEquals(0.4, task.getProgress(), 0.01)
    }

    @Test
    fun testReadingTaskProgress() {
        val task = ReadingTask(1L, "Test", "Desc", readPages = 50, totalPages = 100)
        assertEquals(0.5, task.getProgress(), 0.01)
    }

    @Test
    fun testProjectTaskProgress() {
        val project = ProjectTask(1L, "Project", "Desc")
        val sub1 = GenericTask(2L, "Sub 1", "Desc").apply { estimatedMinutes = 100 }
        val sub2 = GenericTask(3L, "Sub 2", "Desc").apply { estimatedMinutes = 300 }
        
        project.addSubTask(sub1)
        project.addSubTask(sub2)
        
        // sub1 progress 1.0 (empty checklist)
        // sub2 progress 1.0 (empty checklist)
        assertEquals(1.0, project.getProgress(), 0.01)
    }
}
