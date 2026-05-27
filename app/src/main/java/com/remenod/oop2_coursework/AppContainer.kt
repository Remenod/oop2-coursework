package com.remenod.oop2_coursework

import android.content.Context
import com.remenod.oop2_coursework.data.factory.DemoDataFactory
import com.remenod.oop2_coursework.data.persistence.local.LocalTaskStorage
import com.remenod.oop2_coursework.data.repository.FileBackedTaskRepository
import com.remenod.oop2_coursework.data.repository.InMemoryTaskRepository
import com.remenod.oop2_coursework.domain.repository.TaskRepository
import com.remenod.oop2_coursework.domain.service.AnalyticsService
import java.io.File

class AppContainer(context: Context) {
    private val storage = LocalTaskStorage(
        File(context.applicationContext.filesDir, "study_tasks.snapshot")
    )
    private val initialDisciplines = storage.load() ?: DemoDataFactory.createDemoDisciplines()
    private val memoryRepository = InMemoryTaskRepository(initialDisciplines)

    val repository: TaskRepository = FileBackedTaskRepository(memoryRepository, storage)
    
    val analyticsService = AnalyticsService()
}
