package com.remenod.oop2_coursework.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkItemProgressTest {

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
        // GenericTask with no checklist and not DONE is 0%
        val sub1 = GenericTask(2L, "Sub 1", "Desc").apply { 
            status = WorkStatus.DONE 
            estimatedMinutes = 100 
        }
        val sub2 = GenericTask(3L, "Sub 2", "Desc").apply { 
            addChecklistItem("Task")
            setChecklistItemCompleted(0, true)
            estimatedMinutes = 300 
        }
        
        project.addSubTask(sub1)
        project.addSubTask(sub2)
        
        // sub1 is DONE -> 100%
        // sub2 has 1/1 checklist -> 100%
        assertEquals(1.0, project.getProgress(), 0.01)
    }
}
