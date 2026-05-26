package com.remenod.oop2_coursework.data.repository

import com.remenod.oop2_coursework.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
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
    fun testAddDisciplineGeneratesId() = runTest {
        val discipline = Discipline(0, "Test", "Teacher", 1, 0)
        repository.addDiscipline(discipline)
        
        val list = repository.observeDisciplines().first()
        assertEquals(1, list.size)
        assertNotEquals(0L, list.first().id)
    }

    @Test
    fun testAddRootWorkItem() = runTest {
        repository.addDiscipline(Discipline(1L, "D1", "T", 1, 0))
        val task = GenericTask(0, "Task", "Desc")
        
        repository.addRootWorkItem(1L, task)
        
        val discipline = repository.observeDiscipline(1L).first()
        assertEquals(1, discipline?.workItems?.size)
        assertNotEquals(0L, discipline?.workItems?.first()?.id)
    }

    @Test
    fun testAddSubTask() = runTest {
        repository.addDiscipline(Discipline(1L, "D1", "T", 1, 0))
        val project = ProjectTask(10L, "Project", "Desc")
        repository.addRootWorkItem(1L, project)
        
        val subTask = GenericTask(0, "Sub", "Desc")
        repository.addSubTask(10L, subTask)
        
        val restoredProject = repository.observeWorkItem(10L).first() as ProjectTask
        assertEquals(1, restoredProject.subTasks.size)
        assertNotEquals(0L, restoredProject.subTasks.first().id)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testAddSubTaskToAtomicFails() = runTest {
        repository.addDiscipline(Discipline(1L, "D1", "T", 1, 0))
        val atomic = GenericTask(10L, "Atomic", "Desc")
        repository.addRootWorkItem(1L, atomic)
        
        val subTask = GenericTask(0, "Sub", "Desc")
        repository.addSubTask(10L, subTask)
    }

    @Test
    fun testRecursiveDelete() = runTest {
        repository.addDiscipline(Discipline(1L, "D1", "T", 1, 0))
        val project = ProjectTask(10L, "Project", "Desc")
        val subTask = GenericTask(11L, "Sub", "Desc")
        
        repository.addRootWorkItem(1L, project)
        repository.addSubTask(10L, subTask)
        
        // Verify both exist
        assertNotNull(repository.observeWorkItem(11L).first())
        
        // Delete parent
        repository.deleteWorkItem(10L)
        
        // Verify both gone
        assertNull(repository.observeWorkItem(10L).first())
        assertNull(repository.observeWorkItem(11L).first())
    }

    @Test
    fun testProgressPropagation() = runTest {
        repository.addDiscipline(Discipline(1L, "D1", "T", 1, 0))
        val project = ProjectTask(10L, "Project", "Desc")
        val reading = ReadingTask(11L, "Reading", "Desc", readPages = 0, totalPages = 100)
        
        repository.addRootWorkItem(1L, project)
        repository.addSubTask(10L, reading)
        
        assertEquals(0.0, (repository.observeWorkItem(10L).first() as ProjectTask).getProgress(), 0.01)
        
        // Update reading progress
        reading.readPages = 50
        repository.updateWorkItem(reading)
        
        val updatedProject = repository.observeWorkItem(10L).first() as ProjectTask
        assertEquals(0.5, updatedProject.getProgress(), 0.01)
    }
}
