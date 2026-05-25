package com.remenod.oop2_coursework.presentation.discipline

data class DisciplineCardUiModel(
    val id: Long,
    val name: String,
    val teacherName: String,
    val taskCount: Int,
    val progressPercent: Double,
    val overdueCount: Int,
    val color: Int
)

data class DisciplineListUiState(
    val isLoading: Boolean = false,
    val disciplines: List<DisciplineCardUiModel> = emptyList(),
    val error: String? = null
)
