package com.remenod.oop2_coursework.presentation.discipline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remenod.oop2_coursework.domain.model.Discipline
import com.remenod.oop2_coursework.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DisciplineListViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    val uiState: StateFlow<DisciplineListUiState> = repository.observeDisciplines()
        .map { disciplines -> disciplines.toUiState() }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = repository.getDisciplinesSnapshot().toUiState()
        )

    fun addDiscipline(name: String, teacher: String, semester: Int, color: Int) {
        viewModelScope.launch {
            repository.addDiscipline(Discipline(0, name, teacher, semester, color))
        }
    }

    fun updateDiscipline(id: Long, name: String, teacher: String, semester: Int, color: Int) {
        viewModelScope.launch {
            repository.updateDiscipline(Discipline(id, name, teacher, semester, color))
        }
    }

    fun deleteDiscipline(id: Long) {
        viewModelScope.launch {
            repository.deleteDiscipline(id)
        }
    }

    private fun List<Discipline>.toUiState(): DisciplineListUiState {
        return DisciplineListUiState(
            disciplines = map { it.toCardUiModel() }
        )
    }

    private fun Discipline.toCardUiModel(): DisciplineCardUiModel {
        return DisciplineCardUiModel(
            id = id,
            name = name,
            teacherName = teacherName,
            taskCount = workItems.size,
            progressPercent = getProgress(),
            overdueCount = getOverdueItems().size,
            color = color
        )
    }
}
