package com.remenod.oop2_coursework

import android.content.Context
import com.remenod.oop2_coursework.data.persistence.local.LocalTaskStorage
import com.remenod.oop2_coursework.data.repository.LocalTaskRepository
import com.remenod.oop2_coursework.domain.repository.TaskRepository
import com.remenod.oop2_coursework.domain.service.AnalyticsService
import java.io.File

class AppContainer(context: Context) {
    private val storage = LocalTaskStorage(
        File(context.applicationContext.filesDir, "study_tasks.snapshot")
    )

    val repository: TaskRepository = LocalTaskRepository.create(
        storage = storage
    )
    
    val analyticsService = AnalyticsService()
}
