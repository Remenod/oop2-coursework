package com.remenod.oop2_coursework.data.repository

import com.remenod.oop2_coursework.data.persistence.local.LocalTaskStorage
import com.remenod.oop2_coursework.domain.model.Discipline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class LocalTaskRepository(
    private val storage: LocalTaskStorage,
    initialDisciplines: List<Discipline>
) : InMemoryTaskRepository(initialDisciplines) {
    private val saveMutex = Mutex()

    override suspend fun onRepositoryChanged(disciplines: List<Discipline>) {
        withContext(Dispatchers.IO) {
            saveMutex.withLock {
                storage.save(disciplines)
            }
        }
    }

    companion object {
        fun create(storage: LocalTaskStorage): LocalTaskRepository {
            return LocalTaskRepository(
                storage = storage,
                initialDisciplines = storage.load().orEmpty()
            )
        }
    }
}
