package com.remenod.oop2_coursework.data.repository

import com.remenod.oop2_coursework.data.persistence.local.LocalTaskStorage
import com.remenod.oop2_coursework.domain.model.Discipline
import com.remenod.oop2_coursework.domain.model.GenericTask
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class FileBackedTaskRepositoryTest {

    @Test
    fun testMutationIsSavedToLocalStorage() = runTest {
        val file = tempStorageFile()
        val storage = LocalTaskStorage(file)
        val repository = FileBackedTaskRepository(
            delegate = InMemoryTaskRepository(
                initialDisciplines = listOf(Discipline(1L, "OOP", "Teacher", 4, 0))
            ),
            storage = storage
        )

        val created = repository.addRootWorkItem(
            disciplineId = 1L,
            item = GenericTask(0L, "Saved task", "Stored after mutation")
        )

        assertNotEquals(0L, created.id)

        val loadedDisciplines = storage.load()
        assertNotNull(loadedDisciplines)
        val loadedTask = loadedDisciplines!!.first().workItems.first()
        assertEquals(created.id, loadedTask.id)
        assertEquals("Saved task", loadedTask.title)

        val restartedRepository = InMemoryTaskRepository(loadedDisciplines)
        assertEquals("Saved task", restartedRepository.observeWorkItem(created.id).first()?.title)
    }

    private fun tempStorageFile(): File {
        val dir = createTempDirectory(prefix = "oop2-file-backed-repo").toFile()
        return File(dir, "tasks.snapshot")
    }
}
