package com.remenod.oop2_coursework.data.persistence.local

import com.remenod.oop2_coursework.domain.model.AttachmentPurpose
import com.remenod.oop2_coursework.domain.model.Discipline
import com.remenod.oop2_coursework.domain.model.GenericTask
import com.remenod.oop2_coursework.domain.model.GenericWebLink
import com.remenod.oop2_coursework.domain.model.Priority
import com.remenod.oop2_coursework.domain.model.WorkLogEntry
import com.remenod.oop2_coursework.domain.model.WorkStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDateTime
import kotlin.io.path.createTempDirectory

class LocalTaskStorageTest {

    @Test
    fun testSaveAndLoadDisciplinesSnapshot() {
        val file = tempStorageFile()
        val storage = LocalTaskStorage(file)
        val createdAt = LocalDateTime.of(2026, 5, 28, 10, 0)
        val updatedAt = LocalDateTime.of(2026, 5, 28, 12, 30)
        val deadline = LocalDateTime.of(2026, 5, 29, 9, 0)

        val task = GenericTask(
            id = 10L,
            title = "Persisted task",
            description = "Should survive restart",
            status = WorkStatus.IN_PROGRESS,
            priority = Priority.HIGH,
            deadline = deadline,
            createdAt = createdAt,
            updatedAt = updatedAt,
            estimatedMinutes = 90
        ).apply {
            addChecklistItem("Checkpoint")
            addAttachment(
                GenericWebLink(
                    id = 11L,
                    title = "Reference",
                    url = "https://example.com",
                    createdAt = createdAt,
                    purpose = AttachmentPurpose.REFERENCE,
                    notes = "Saved note"
                )
            )
            addLog(WorkLogEntry(12L, "Worked on task", createdAt, minutesSpent = 25))
            this.updatedAt = updatedAt
        }
        val discipline = Discipline(1L, "OOP", "Teacher", 4, 0xFF00AA00.toInt()).apply {
            addWorkItem(task)
        }

        storage.save(listOf(discipline))

        val loaded = storage.load()
        assertNotNull(loaded)
        assertEquals(1, loaded!!.size)

        val loadedTask = loaded.first().workItems.first() as GenericTask
        assertEquals("Persisted task", loadedTask.title)
        assertEquals(WorkStatus.IN_PROGRESS, loadedTask.status)
        assertEquals(Priority.HIGH, loadedTask.priority)
        assertEquals(deadline, loadedTask.deadline)
        assertEquals(createdAt, loadedTask.createdAt)
        assertEquals(updatedAt, loadedTask.updatedAt)
        assertEquals(90, loadedTask.estimatedMinutes)
        assertEquals(1, loadedTask.checklist.size)
        assertEquals(1, loadedTask.attachments.size)
        assertEquals(1, loadedTask.logs.size)
        assertEquals("Saved note", loadedTask.attachments.first().notes)
        assertEquals(25, loadedTask.logs.first().minutesSpent)
        assertTrue(file.exists())
    }

    private fun tempStorageFile(): File {
        val dir = createTempDirectory(prefix = "oop2-storage").toFile()
        return File(dir, "tasks.snapshot")
    }
}
