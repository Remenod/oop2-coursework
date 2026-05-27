package com.remenod.oop2_coursework

import com.remenod.oop2_coursework.data.factory.DemoDataFactory
import com.remenod.oop2_coursework.data.repository.InMemoryTaskRepository
import com.remenod.oop2_coursework.domain.repository.TaskRepository
import com.remenod.oop2_coursework.domain.service.AnalyticsService

class AppContainer {
    val repository: TaskRepository = InMemoryTaskRepository(DemoDataFactory.createDemoDisciplines())
    
    val analyticsService = AnalyticsService()
}
