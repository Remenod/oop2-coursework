package com.remenod.oop2_coursework.presentation.discipline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remenod.oop2_coursework.domain.model.Discipline
import com.remenod.oop2_coursework.domain.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DisciplineListViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    val uiState: StateFlow<DisciplineListUiState> = repository.observeDisciplines()
        .map { disciplines ->
            DisciplineListUiState(
                disciplines = disciplines.map { it.toCardUiModel() }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DisciplineListUiState(isLoading = true)
        )

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
