package com.remenod.oop2_coursework.presentation.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.remenod.oop2_coursework.presentation.worklist.PriorityBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onDisciplineClick: (Long) -> Unit,
    onTaskClick: (Long) -> Unit,
    onViewDisciplines: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Academic Dashboard") },
                actions = {
                    IconButton(onClick = onViewDisciplines) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Disciplines")
                    }
                }
            )
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
                // Summary Statistics
                item {
                    Text("Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard(
                            label = "Overdue",
                            value = uiState.overdueTasks.toString(),
                            icon = Icons.Default.Warning,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Due Today",
                            value = uiState.dueTodayTasks.toString(),
                            icon = Icons.Default.Today,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard(
                            label = "Active",
                            value = uiState.activeTasks.toString(),
                            icon = Icons.Default.Pending,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Done",
                            value = uiState.doneTasks.toString(),
                            icon = Icons.Default.CheckCircle,
                            color = Color(0xFF1E8E3E),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Progress Summary
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Overall Progress", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { uiState.averageProgress.toFloat() },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${(uiState.averageProgress * 100).toInt()}% average", style = MaterialTheme.typography.bodySmall)
                                Text("${uiState.totalLoggedTimeText} spent", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // At Risk Tasks
                if (uiState.atRiskTasks.isNotEmpty()) {
                    item {
                        Text("At Risk / Urgent", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                    }
                    items(uiState.atRiskTasks) { task ->
                        DashboardTaskItem(task, onClick = { onTaskClick(task.id) })
                    }
                }

                // High Priority Tasks
                if (uiState.highPriorityTasks.isNotEmpty()) {
                    item {
                        Text("High Priority", style = MaterialTheme.typography.titleMedium)
                    }
                    items(uiState.highPriorityTasks) { task ->
                        DashboardTaskItem(task, onClick = { onTaskClick(task.id) })
                    }
                }

                // Disciplines Summary
                item {
                    Text("Disciplines", style = MaterialTheme.typography.titleMedium)
                }
                items(uiState.disciplineSummaries) { discipline ->
                    DisciplineSummaryCard(discipline, onClick = { onDisciplineClick(discipline.id) })
                }
                
                item {
                    DashboardActionButton(
                        onClick = onViewDisciplines,
                        modifier = Modifier.fillMaxWidth(),
                        variant = ButtonVariant.OUTLINED
                    ) {
                        Text("Manage All Disciplines")
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun DashboardTaskItem(
    task: DashboardTaskUiModel,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PriorityBadge(task.priority)
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(task.disciplineName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text(task.timeLeftText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
            Text("${(task.progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun DisciplineSummaryCard(
    discipline: DisciplineDashboardUiModel,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(discipline.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text("${discipline.activeTaskCount} active", style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { discipline.progress.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = Color(discipline.color)
            )
        }
    }
}

@Composable
fun DashboardActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.FILLED,
    content: @Composable RowScope.() -> Unit
) {
    when (variant) {
        ButtonVariant.FILLED -> Button(onClick = onClick, modifier = modifier, content = content)
        ButtonVariant.OUTLINED -> OutlinedButton(onClick = onClick, modifier = modifier, content = content)
        ButtonVariant.TEXT -> TextButton(onClick = onClick, modifier = modifier, content = content)
    }
}

enum class ButtonVariant {
    FILLED, OUTLINED, TEXT
}
