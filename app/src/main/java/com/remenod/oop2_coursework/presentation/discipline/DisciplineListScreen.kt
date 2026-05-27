package com.remenod.oop2_coursework.presentation.discipline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisciplineListScreen(
    viewModel: DisciplineListViewModel,
    onDisciplineClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingDiscipline by remember { mutableStateOf<DisciplineCardUiModel?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Disciplines") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = uiState.disciplines,
                    key = { it.id },
                    contentType = { "discipline" }
                ) { discipline ->
                    DisciplineCard(
                        discipline = discipline, 
                        onClick = { onDisciplineClick(discipline.id) },
                        onEdit = { editingDiscipline = discipline },
                        onDelete = { viewModel.deleteDiscipline(discipline.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        DisciplineEditDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, teacher, semester, color ->
                viewModel.addDiscipline(name, teacher, semester, color)
            }
        )
    }

    editingDiscipline?.let { discipline ->
        DisciplineEditDialog(
            initialName = discipline.name,
            initialTeacher = discipline.teacherName,
            initialSemester = 1, // Simplified
            initialColor = discipline.color,
            onDismiss = { editingDiscipline = null },
            onConfirm = { name, teacher, semester, color ->
                viewModel.updateDiscipline(discipline.id, name, teacher, semester, color)
            }
        )
    }
}

@Composable
fun DisciplineCard(
    discipline: DisciplineCardUiModel,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(Color(discipline.color))
            )
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = discipline.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onClick)
                    )
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
                Text(
                    text = discipline.teacherName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        LinearProgressIndicator(
                            progress = { discipline.progressPercent.toFloat() },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "${(discipline.progressPercent * 100).toInt()}% completed",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    if (discipline.overdueCount > 0) {
                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                            Text("${discipline.overdueCount} overdue")
                        }
                    }
                }
            }
        }
    }
}
