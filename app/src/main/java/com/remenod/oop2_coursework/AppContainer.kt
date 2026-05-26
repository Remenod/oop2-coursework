package com.remenod.oop2_coursework

import com.remenod.oop2_coursework.data.factory.DemoDataFactory
import com.remenod.oop2_coursework.data.repository.InMemoryTaskRepository
import com.remenod.oop2_coursework.domain.repository.TaskRepository
import com.remenod.oop2_coursework.domain.service.AnalyticsService
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class AppContainer {
    val repository: TaskRepository = InMemoryTaskRepository().apply {
        MainScope().launch {
            DemoDataFactory.createDemoDisciplines().forEach {
                addDiscipline(it)
            }
        }
    }
    
    val analyticsService = AnalyticsService()
}
