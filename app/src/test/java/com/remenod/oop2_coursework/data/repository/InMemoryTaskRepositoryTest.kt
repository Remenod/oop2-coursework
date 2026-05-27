package com.remenod.oop2_coursework.data.repository

import com.remenod.oop2_coursework.domain.model.*
import com.remenod.oop2_coursework.presentation.worklist.WorkItemEditResult
import com.remenod.oop2_coursework.presentation.worklist.WorkItemFactory
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class InMemoryTaskRepositoryTest {

    private lateinit var repository: InMemoryTaskRepository

    @Before
    fun setup() {
        repository = InMemoryTaskRepository()
    }

    @Test
    fun testAddDisciplineGeneratesIdAndReturns() = runTest {
        val discipline = Discipline(0, "Test", "Teacher", 1, 0)
        val created = repository.addDiscipline(discipline)
        
        assertNotEquals(0L, created.id)
        val list = repository.observeDisciplines().first()
        assertEquals(1, list.size)
        assertEquals(created.id, list.first().id)
    }

    @Test
    fun testInitialDisciplinesAreAvailableImmediately() = runTest {
        val discipline = Discipline(100L, "Seeded", "Teacher", 1, 0)
        val project = ProjectTask(101L, "Project", "Desc")
        val nested = GenericTask(102L, "Nested", "Desc")
        project.addSubTask(nested)
        discipline.addWorkItem(project)

        val seededRepository = InMemoryTaskRepository(listOf(discipline))

        assertEquals(1, seededRepository.observeDisciplines().first().size)
        assertEquals(102L, seededRepository.observeWorkItem(102L).first()?.id)
    }

    @Test
    fun testUpdateDisciplinePreservesWorkItems() = runTest {
        val discipline = Discipline(1L, "Original", "T", 1, 0)
        repository.addDiscipline(discipline)
        repository.addRootWorkItem(1L, GenericTask(10L, "Task", "D"))
        
        val updated = Discipline(1L, "New Name", "New T", 2, 1)
        repository.updateDiscipline(updated)
        
        val restored = repository.observeDiscipline(1L).first()
        assertEquals("New Name", restored?.name)
        assertEquals(1, restored?.workItems?.size)
        assertEquals(10L, restored?.workItems?.first()?.id)
    }

    @Test
    fun testAddRootWorkItemReturnsWithId() = runTest {
        repository.addDiscipline(Discipline(1L, "D1", "T", 1, 0))
        val task = GenericTask(0, "Task", "Desc")
        
        val created = repository.addRootWorkItem(1L, task)
        
        assertNotEquals(0L, created.id)
        val discipline = repository.observeDiscipline(1L).first()
        assertEquals(1, discipline?.workItems?.size)
        assertEquals(created.id, discipline?.workItems?.first()?.id)
    }

    @Test
    fun testAddSubTaskToProject() = runTest {
        repository.addDiscipline(Discipline(1L, "D1", "T", 1, 0))
        val project = ProjectTask(10L, "Project", "Desc")
        repository.addRootWorkItem(1L, project)
        
        val subTask = GenericTask(0, "Sub", "Desc")
        val createdSub = repository.addSubTask(10L, subTask)
        
        assertNotEquals(0L, createdSub.id)
        val restoredProject = repository.observeWorkItem(10L).first() as ProjectTask
        assertEquals(1, restoredProject.subTasks.size)
        assertEquals(createdSub.id, restoredProject.subTasks.first().id)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testAddSubTaskToGenericFails() = runTest {
        repository.addDiscipline(Discipline(1L, "D1", "T", 1, 0))
        val generic = GenericTask(10L, "Generic", "Desc")
        repository.addRootWorkItem(1L, generic)
        
        val subTask = GenericTask(0, "Sub", "Desc")
        repository.addSubTask(10L, subTask)
    }

    @Test
    fun testRecursiveDeleteAndCleanup() = runTest {
        repository.addDiscipline(Discipline(1L, "D1", "T", 1, 0))
        val project = ProjectTask(10L, "Parent", "Desc")
        val child = ProjectTask(11L, "Child", "Desc")
        val grandChild = GenericTask(12L, "GrandChild", "Desc")
        
        repository.addRootWorkItem(1L, project)
        repository.addSubTask(10L, child)
        repository.addSubTask(11L, grandChild)
        
        // Delete middle node
        repository.deleteWorkItem(11L)
        
        // Verify child and grandchild are gone
        assertNull(repository.observeWorkItem(11L).first())
        assertNull(repository.observeWorkItem(12L).first())
        
        // Verify parent still exists but has no children
        val restoredParent = repository.observeWorkItem(10L).first() as ProjectTask
        assertTrue(restoredParent.subTasks.isEmpty())
    }

    @Test
    fun testProgressUpdatesPropagateToParent() = runTest {
        repository.addDiscipline(Discipline(1L, "D1", "T", 1, 0))
        val project = ProjectTask(10L, "Project", "Desc")
        val reading = ReadingTask(11L, "Reading", "Desc", readPages = 0, totalPages = 100)
        
        repository.addRootWorkItem(1L, project)
        repository.addSubTask(10L, reading)
        
        // Initially 0%
        assertEquals(0.0, (repository.observeWorkItem(10L).first() as ProjectTask).getProgress(), 0.01)
        
        // Update child
        reading.updatePages(75, 100)
        repository.updateWorkItem(reading)
        
        // Parent should be 75%
        val updatedProject = repository.observeWorkItem(10L).first() as ProjectTask
        assertEquals(0.75, updatedProject.getProgress(), 0.01)
    }

    @Test
    fun testUpdateWorkItemEmitsAfterInPlaceMutation() = runTest {
        repository.addDiscipline(Discipline(1L, "D1", "T", 1, 0))
        val reading = ReadingTask(0L, "Reading", "Desc", readPages = 0, totalPages = 100)
        val created = repository.addRootWorkItem(1L, reading)

        val emissions = mutableListOf<Double>()
        val job = launch {
            repository.observeWorkItem(created.id)
                .filterNotNull()
                .take(2)
                .collect { item ->
                    emissions.add(item.getProgress())
                }
        }
        
        yield() // Let collection start

        (created as ReadingTask).updatePages(50, 100)
        repository.updateWorkItem(created)

        job.join()

        assertEquals(2, emissions.size)
        assertEquals(0.0, emissions[0], 0.01)
        assertEquals(0.5, emissions[1], 0.01)
    }

    @Test
    fun testChecklistUpdatesProgress() = runTest {
        repository.addDiscipline(Discipline(1L, "D1", "T", 1, 0))
        val task = GenericTask(0, "G", "D")
        val created = repository.addRootWorkItem(1L, task) as GenericTask
        
        assertEquals(0.0, created.getProgress(), 0.01)
        
        created.addChecklistItem("Item 1")
        repository.updateWorkItem(created)
        assertEquals(0.0, created.getProgress(), 0.01) // 0/1
        
        created.setChecklistItemCompleted(0, true)
        repository.updateWorkItem(created)
        assertEquals(1.0, created.getProgress(), 0.01) // 1/1
    }

    @Test
    fun testToggleCompletion() = runTest {
        repository.addDiscipline(Discipline(1L, "D1", "T", 1, 0))
        val task = GenericTask(10L, "G", "D") // No checklist
        repository.addRootWorkItem(1L, task)
        
        assertEquals(WorkStatus.CREATED, task.status)
        
        task.complete()
        repository.updateWorkItem(task)
        assertEquals(WorkStatus.DONE, task.status)
        assertEquals(1.0, task.getProgress(), 0.01)
        
        repository.changeWorkItemStatus(10L, WorkStatus.IN_PROGRESS)
        val restored = repository.observeWorkItem(10L).first()!!
        assertEquals(WorkStatus.IN_PROGRESS, restored.status)
        assertEquals(0.0, restored.getProgress(), 0.01)
    }

    @Test
    fun testMetadataUpdateAtomicity() = runTest {
        val task = GenericTask(10L, "G", "D")
        task.addChecklistItem("Incomplete") // cannot be DONE
        
        try {
            task.updateMetadata("New", "D", WorkStatus.DONE, Priority.HIGH, null, 10)
            fail("Should throw exception")
        } catch (_: IllegalStateException) {
            // Success
        }
        
        // Fields should NOT be changed
        assertEquals("G", task.title)
        assertEquals(WorkStatus.CREATED, task.status)
    }

    @Test
    fun testRecursiveOverdueDetection() = runTest {
        val discipline = Discipline(1L, "D", "T", 1, 0)
        val project = ProjectTask(10L, "P", "D")
        val child = GenericTask(11L, "C", "D")
        child.deadline = LocalDateTime.now().minusDays(1) // Overdue
        
        project.addSubTask(child)
        discipline.addWorkItem(project)
        
        assertEquals(1, discipline.getOverdueItems().size)
        assertEquals(11L, discipline.getOverdueItems().first().id)
    }

    @Test
    fun testCreateTaskWithDoneStatusFailsValidation() = runTest {
        repository.addDiscipline(Discipline(1L, "D1", "T", 1, 0))
        val result = WorkItemEditResult(
            title = "Task", description = "D", type = WorkItemType.PROGRAMMING,
            status = WorkStatus.DONE, priority = Priority.NORMAL, deadline = null, estimatedMinutes = 10,
            requiredCommits = 5, commitsCount = 0 // Not ready
        )
        
        try {
            val item = WorkItemFactory.createFrom(result)
            repository.addRootWorkItem(1L, item)
            fail("Should throw IllegalStateException")
        } catch (_: IllegalStateException) {}
    }

    @Test
    fun testProgrammingTaskCompletionTargets() = runTest {
        val task = ProgrammingTask(0, "P", "D", requiredCommits = 10, requiredIssues = 5)
        assertFalse(task.canBeCompleted())
        
        task.commitsCount = 10
        task.issuesResolved = 5
        task.testsPassed = 1.0
        assertTrue(task.canBeCompleted())
    }

    @Test
    fun testProjectProgressExcludesCancelled() = runTest {
        val project = ProjectTask(1L, "P", "D")
        val task1 = GenericTask(2L, "T1", "D").apply { 
            status = WorkStatus.DONE 
        }
        val task2 = GenericTask(3L, "T2", "D").apply { 
            status = WorkStatus.CANCELLED
        }
        
        project.addSubTask(task1)
        project.addSubTask(task2)
        
        // Only task 1 is active and DONE -> 100%
        assertEquals(1.0, project.getProgress(), 0.01)
        assertTrue(project.getProgressSnapshot().explanation.contains("1 active"))
    }
}
