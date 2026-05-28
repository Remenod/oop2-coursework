package com.remenod.oop2_coursework.data.persistence.mapper

import com.remenod.oop2_coursework.domain.model.ExamTask
import com.remenod.oop2_coursework.domain.model.GenericTask
import com.remenod.oop2_coursework.domain.model.GitHubRepositoryLink
import com.remenod.oop2_coursework.domain.model.ProjectTask
import com.remenod.oop2_coursework.domain.model.ReadingTask
import com.remenod.oop2_coursework.domain.model.SeminarTask
import com.remenod.oop2_coursework.domain.model.WorkItemType
import com.remenod.oop2_coursework.domain.model.WorkLogEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PersistenceMapperTest {

    @Test
    fun testCompleteRoundTripWithBehavior() {
        val original = GenericTask(
            id = 1L,
            title = "Repository task",
            description = "Desc"
        ).apply {
            addChecklistItem("Prepare repository notes")
            setChecklistItemCompleted(0, true)
            addAttachment(
                GitHubRepositoryLink(
                    id = 10L,
                    title = "Repo",
                    url = "https://github.com/owner/repo"
                ).apply { sync() }
            )
            addLog(WorkLogEntry(100L, "Started", minutesSpent = 30))
        }

        val bundle = WorkItemPersistenceMapper.decomposeToBundle(1000L, listOf(original))

        assertEquals(1, bundle.workItems.size)
        assertEquals(1, bundle.checklistItems.size)
        assertEquals(1, bundle.attachments.size)
        assertEquals(3, bundle.githubWorkCandidates.size)
        assertEquals(1, bundle.logs.size)
        assertEquals(WorkItemType.GENERIC, bundle.workItems.first().type)

        val restored = WorkItemPersistenceMapper.restoreHierarchy(bundle).first() as GenericTask
        val restoredAttachment = restored.attachments.first() as GitHubRepositoryLink

        assertEquals(original.title, restored.title)
        assertTrue(restored.checklist.first().isCompleted)
        assertEquals(2, restoredAttachment.activeIssuesCount)
        assertEquals(1, restoredAttachment.openPullRequestsCount)
        assertEquals(3, restoredAttachment.importableCandidates.size)
        assertEquals(original.getProgress(), restored.getProgress(), 0.01)
    }

    @Test
    fun testDeepHierarchyAndSorting() {
        // Root (Project)
        //  ├── Child 1 (Generic, sort 0)
        //  └── Child 2 (Project, sort 1)
        //       └── GrandChild (Generic, sort 0)

        val root = ProjectTask(1L, "Root", "Desc")
        val child1 = GenericTask(2L, "Child 1", "Desc")
        val child2 = ProjectTask(3L, "Child 2", "Desc")
        val grandChild = GenericTask(4L, "GrandChild", "Desc")

        child2.addSubTask(grandChild)
        root.addSubTask(child1)
        root.addSubTask(child2)

        val bundle = WorkItemPersistenceMapper.decomposeToBundle(1000L, listOf(root))
        assertEquals(4, bundle.workItems.size)

        val shuffledBundle = bundle.copy(
            workItems = bundle.workItems.shuffled(),
            checklistItems = bundle.checklistItems.shuffled()
        )

        val restored = WorkItemPersistenceMapper.restoreHierarchy(shuffledBundle)
        assertEquals(1, restored.size)

        val restoredRoot = restored.first() as ProjectTask
        assertEquals(2, restoredRoot.subTasks.size)
        assertEquals(2L, restoredRoot.subTasks[0].id)
        assertEquals(3L, restoredRoot.subTasks[1].id)

        val restoredChild2 = restoredRoot.subTasks[1] as ProjectTask
        assertEquals(1, restoredChild2.subTasks.size)
        assertEquals(4L, restoredChild2.subTasks[0].id)
        assertEquals(root.getProgress(), restoredRoot.getProgress(), 0.01)
    }

    @Test
    fun testAllTaskTypesRoundTrip() {
        val tasks = listOf(
            ExamTask(2L, "E", "D").apply { addTopic("T1", 80) },
            SeminarTask(3L, "S", "D", topicSelected = true),
            ReadingTask(4L, "R", "D", readPages = 10, totalPages = 20),
            ProjectTask(5L, "P", "D"),
            GenericTask(6L, "G", "D")
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
