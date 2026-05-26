package com.remenod.oop2_coursework.data.persistence.mapper

import com.remenod.oop2_coursework.data.persistence.model.*
import com.remenod.oop2_coursework.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class PersistenceMapperTest {

    @Test
    fun testCompleteRoundTripWithBehavior() {
        val original = ProgrammingTask(
            id = 1L,
            title = "Progr Task",
            description = "Desc",
            commitsCount = 3,
            issuesResolved = 1,
            testsPassed = 0.5
        ).apply {
            addChecklistItem(ChecklistItem("Check 1", true))
            addAttachment(GitHubRepositoryLink(10L, "Repo", "url"))
            addLog(WorkLogEntry(100L, 1L, LocalDateTime.now(), 30, WorkStatus.CREATED, WorkStatus.IN_PROGRESS, 0.2, "Started"))
        }

        // 1. Decompose
        val bundle = WorkItemPersistenceMapper.decomposeToBundle(1000L, listOf(original))
        
        assertEquals(1, bundle.workItems.size)
        assertEquals(1, bundle.checklistItems.size)
        assertEquals(1, bundle.attachments.size)
        assertEquals(1, bundle.logs.size)
        assertEquals(WorkItemType.PROGRAMMING, bundle.workItems.first().type)

        // 2. Restore
        val restored = WorkItemPersistenceMapper.restoreHierarchy(bundle).first() as ProgrammingTask

        // 3. Verify Integrity
        assertEquals(original.title, restored.title)
        assertEquals(original.commitsCount, restored.commitsCount)
        assertEquals(1, restored.checklist.size)
        assertTrue(restored.attachments.first() is GitHubRepositoryLink)
        
        // 4. Verify Behavior
        assertEquals(original.getProgress(), restored.getProgress(), 0.01)
    }

    @Test
    fun testDeepHierarchyAndSorting() {
        // Root (Project)
        //  ├── Child 1 (Generic, sort 0)
        //  └── Child 2 (Project, sort 1)
        //       └── GrandChild (Programming, sort 0)
        
        val root = ProjectTask(1L, "Root", "Desc")
        val child1 = GenericTask(2L, "Child 1", "Desc")
        val child2 = ProjectTask(3L, "Child 2", "Desc")
        val grandChild = ProgrammingTask(4L, "GrandChild", "Desc", commitsCount = 5)
        
        child2.addSubTask(grandChild)
        root.addSubTask(child1)
        root.addSubTask(child2)

        // 1. Decompose
        val bundle = WorkItemPersistenceMapper.decomposeToBundle(1000L, listOf(root))
        assertEquals(4, bundle.workItems.size)

        // 2. Shuffle bundle to test robust reconstruction
        val shuffledBundle = bundle.copy(
            workItems = bundle.workItems.shuffled(),
            checklistItems = bundle.checklistItems.shuffled()
        )

        // 3. Restore
        val restored = WorkItemPersistenceMapper.restoreHierarchy(shuffledBundle)
        assertEquals(1, restored.size)
        
        val restoredRoot = restored.first() as ProjectTask
        assertEquals(2, restoredRoot.subTasks.size)
        
        // Verify Order
        assertEquals(2L, restoredRoot.subTasks[0].id)
        assertEquals(3L, restoredRoot.subTasks[1].id)
        
        val restoredChild2 = restoredRoot.subTasks[1] as ProjectTask
        assertEquals(1, restoredChild2.subTasks.size)
        assertEquals(4L, restoredChild2.subTasks[0].id)
        
        // Verify Aggregate Progress Behavior
        assertEquals(root.getProgress(), restoredRoot.getProgress(), 0.01)
    }

    @Test
    fun testAllTaskTypesRoundTrip() {
        val tasks = listOf(
            ProgrammingTask(1L, "P", "D", commitsCount = 2),
            ExamTask(2L, "E", "D").apply { topics.add(ExamTopic("T1", 80)) },
            SeminarTask(3L, "S", "D", topicSelected = true),
            ReadingTask(4L, "R", "D", readPages = 10, totalPages = 20),
            GenericTask(5L, "G", "D")
        )

        val bundle = WorkItemPersistenceMapper.decomposeToBundle(1000L, tasks)
        val restored = WorkItemPersistenceMapper.restoreHierarchy(bundle)

        assertEquals(tasks.size, restored.size)
        restored.forEachIndexed { index, item ->
            assertEquals(tasks[index].javaClass, item.javaClass)
            assertEquals(tasks[index].getProgress(), item.getProgress(), 0.01)
        }
    }
}
