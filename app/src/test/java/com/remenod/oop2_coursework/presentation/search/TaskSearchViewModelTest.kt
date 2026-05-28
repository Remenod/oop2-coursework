package com.remenod.oop2_coursework.presentation.search

import com.remenod.oop2_coursework.data.repository.InMemoryTaskRepository
import com.remenod.oop2_coursework.domain.model.AttachmentPurpose
import com.remenod.oop2_coursework.domain.model.Discipline
import com.remenod.oop2_coursework.domain.model.GenericTask
import com.remenod.oop2_coursework.domain.model.GitHubRepositoryLink
import com.remenod.oop2_coursework.domain.model.ProjectTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskSearchViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testGlobalSearchFiltersNestedTaskByAttachmentPurpose() = runTest {
        val repository = InMemoryTaskRepository()
        val discipline = Discipline(1L, "OOP", "Teacher", 4, 0)
        val project = ProjectTask(10L, "Coursework", "D")
        val repositoryTask = GenericTask(11L, "Repository task", "D").apply {
            addAttachment(
                GitHubRepositoryLink(
                    id = 100L,
                    title = "Repo",
                    url = "https://github.com/example/coursework",
                    purpose = AttachmentPurpose.SOURCE_CODE
                )
            )
        }

        project.addSubTask(repositoryTask)
        discipline.addWorkItem(project)
        repository.addDiscipline(discipline)

        val viewModel = TaskSearchViewModel(repository)
        viewModel.setAttachmentPurposeFilter(AttachmentPurpose.SOURCE_CODE)

        val state = viewModel.uiState
            .filter { !it.isLoading && it.attachmentPurposeFilter == AttachmentPurpose.SOURCE_CODE }
            .first()

        assertEquals(2, state.totalItems)
        assertEquals(1, state.items.size)
        assertEquals(11L, state.items.first().id)
        assertEquals("OOP", state.items.first().disciplineName)
    }
}
