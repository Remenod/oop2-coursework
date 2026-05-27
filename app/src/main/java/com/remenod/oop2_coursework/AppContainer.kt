package com.remenod.oop2_coursework

import com.remenod.oop2_coursework.data.factory.DemoDataFactory
import com.remenod.oop2_coursework.data.repository.InMemoryTaskRepository
import com.remenod.oop2_coursework.domain.model.CompositeWorkItem
import com.remenod.oop2_coursework.domain.repository.TaskRepository
import com.remenod.oop2_coursework.domain.service.AnalyticsService
import kotlinx.coroutines.flow.first

class AppContainer {
    val repository: TaskRepository = InMemoryTaskRepository(DemoDataFactory.createDemoDisciplines())
    
    val analyticsService = AnalyticsService()

    suspend fun warmUp() {
        val disciplines = repository.observeDisciplines().first()
        val allItems = analyticsService.getAllItems(disciplines)

        analyticsService.countByStatus(disciplines)
        analyticsService.countOverdue(disciplines)
        analyticsService.calculateAverageProgress(disciplines)
        analyticsService.getAtRiskItems(disciplines)

        disciplines.forEach { discipline ->
            repository.observeDiscipline(discipline.id).first()
        }
        allItems.forEach { item ->
            repository.observeWorkItem(item.id).first()
            item.getProgressSnapshot()
            item.attachments.forEach { it.getDisplayName() }
            if (item is CompositeWorkItem) {
                item.subTasks.forEach { it.getProgressSnapshot() }
            }
        }
    }
}
