package com.remenod.oop2_coursework.data.repository

import com.remenod.oop2_coursework.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class AttachmentAndLogTest {

    private lateinit var repository: InMemoryTaskRepository

    @Before
    fun setup() {
        repository = InMemoryTaskRepository()
    }

    @Test
    fun testAddAndRemoveAttachment() = runTest {
        repository.addDiscipline(Discipline(1L, "D1", "T", 1, 0))
        val task = GenericTask(10L, "Task", "D")
        repository.addRootWorkItem(1L, task)

        val attachment = GenericWebLink(0, "Link", "https://google.com")
        val added = repository.addAttachment(10L, attachment)

        assertNotEquals(0L, added.id)
        val restored = repository.observeWorkItem(10L).first()!!
        assertEquals(1, restored.attachments.size)
        assertEquals("Link", restored.attachments.first().title)

        repository.removeAttachment(10L, added.id)
        val restoredEmpty = repository.observeWorkItem(10L).first()!!
        assertTrue(restoredEmpty.attachments.isEmpty())
    }

    @Test
    fun testAddAndRemoveWorkLog() = runTest {
        repository.addDiscipline(Discipline(1L, "D1", "T", 1, 0))
        val task = GenericTask(10L, "Task", "D")
        repository.addRootWorkItem(1L, task)

        val log = WorkLogEntry(0, "Manual log", minutesSpent = 30)
        val added = repository.addWorkLogEntry(10L, log)

        assertNotEquals(0L, added.id)
        val restored = repository.observeWorkItem(10L).first()!!
        assertEquals(1, restored.logs.size)
        assertEquals("Manual log", restored.logs.first().message)
        assertEquals(30, restored.logs.first().minutesSpent)

        repository.removeWorkLogEntry(10L, added.id)
        val restoredEmpty = repository.observeWorkItem(10L).first()!!
        assertTrue(restoredEmpty.logs.isEmpty())
    }

    @Test
    fun testAttachmentUpdatesUpdatedAt() = runTest {
        repository.addDiscipline(Discipline(1L, "D1", "T", 1, 0))
        val task = GenericTask(10L, "Task", "D")
        repository.addRootWorkItem(1L, task)
        
        val initialUpdate = task.updatedAt
        Thread.sleep(10) // Ensure time passes

        repository.addAttachment(10L, GenericWebLink(0, "L", "H"))
        val restored = repository.observeWorkItem(10L).first()!!
        assertTrue(restored.updatedAt.isAfter(initialUpdate))
    }
}
