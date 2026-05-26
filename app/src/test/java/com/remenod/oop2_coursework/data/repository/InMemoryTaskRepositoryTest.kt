package com.remenod.oop2_coursework.data.repository

import com.remenod.oop2_coursework.domain.model.*
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

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
        reading.readPages = 75
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

        (created as ReadingTask).readPages = 50
        repository.updateWorkItem(created)

        job.join()

        assertEquals(2, emissions.size)
        assertEquals(0.0, emissions[0], 0.01)
        assertEquals(0.5, emissions[1], 0.01)
    }

    @Test
    fun testProgrammingTaskProgressUpdates() = runTest {
        repository.addDiscipline(Discipline(1L, "D1", "T", 1, 0))
        val task = ProgrammingTask(0, "P", "D")
        val created = repository.addRootWorkItem(1L, task) as ProgrammingTask
        
        created.commitsCount = 5
        created.issuesResolved = 2
        created.testsPassed = 1.0
        repository.updateWorkItem(created)
        
        val restored = repository.observeWorkItem(created.id).first() as ProgrammingTask
        assertEquals(1.0, restored.getProgress(), 0.01)
    }

    @Test
    fun testExamTaskTopicManagement() = runTest {
        repository.addDiscipline(Discipline(1L, "D1", "T", 1, 0))
        val task = ExamTask(0, "E", "D")
        val created = repository.addRootWorkItem(1L, task) as ExamTask
        
        created.topics.add(ExamTopic("T1", 100))
        repository.updateWorkItem(created)
        
        val restored = repository.observeWorkItem(created.id).first() as ExamTask
        assertEquals(1.0, restored.getProgress(), 0.01)
        
        created.topics[0] = created.topics[0].copy(confidence = 0)
        repository.updateWorkItem(created)
        
        assertEquals(0.0, (repository.observeWorkItem(created.id).first() as ExamTask).getProgress(), 0.01)
    }

    @Test
    fun testSeminarTaskStageToggles() = runTest {
        repository.addDiscipline(Discipline(1L, "D1", "T", 1, 0))
        val task = SeminarTask(0, "S", "D")
        val created = repository.addRootWorkItem(1L, task) as SeminarTask
        
        created.topicSelected = true
        repository.updateWorkItem(created)
        
        // 1 of 5 stages
        assertEquals(0.2, (repository.observeWorkItem(created.id).first() as SeminarTask).getProgress(), 0.01)
    }

    @Test
    fun testSpecializedTaskPropagationToParent() = runTest {
        repository.addDiscipline(Discipline(1L, "D1", "T", 1, 0))
        val project = ProjectTask(0, "Root", "D")
        val createdProject = repository.addRootWorkItem(1L, project) as ProjectTask
        
        val seminar = SeminarTask(0, "S", "D")
        val createdSeminar = repository.addSubTask(createdProject.id, seminar) as SeminarTask
        
        assertEquals(0.0, (repository.observeWorkItem(createdProject.id).first() as ProjectTask).getProgress(), 0.01)
        
        createdSeminar.topicSelected = true
        createdSeminar.materialsCollected = true
        repository.updateWorkItem(createdSeminar)
        
        // 2 of 5 stages = 0.4. Project should be 0.4
        val restoredProject = repository.observeWorkItem(createdProject.id).first() as ProjectTask
        assertEquals(0.4, restoredProject.getProgress(), 0.01)
    }
}
