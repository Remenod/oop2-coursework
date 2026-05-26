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
import androidx.compose.ui.text.style.TextOverflow
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
                title = { Text("Dashboard") },
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    DashboardSummaryHeader(uiState)
                }

                item {
                    DashboardStatGrid(uiState)
                }

                item {
                    ProgressOverviewCard(uiState)
                }

                if (uiState.atRiskTasks.isNotEmpty()) {
                    item {
                        DashboardSectionTitle(
                            title = "At risk",
                            icon = Icons.Default.Warning,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    items(uiState.atRiskTasks) { task ->
                        DashboardTaskItem(task, onClick = { onTaskClick(task.id) })
                    }
                }

                if (uiState.highPriorityTasks.isNotEmpty()) {
                    item {
                        DashboardSectionTitle(
                            title = "High priority",
                            icon = Icons.Default.PriorityHigh,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(uiState.highPriorityTasks) { task ->
                        DashboardTaskItem(task, onClick = { onTaskClick(task.id) })
                    }
                }

                item {
                    DashboardSectionTitle(
                        title = "Disciplines",
                        icon = Icons.Default.School,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                items(uiState.disciplineSummaries) { discipline ->
                    DisciplineSummaryCard(discipline, onClick = { onDisciplineClick(discipline.id) })
                }

                item {
                    OutlinedButton(
                        onClick = onViewDisciplines,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Manage disciplines")
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardSummaryHeader(state: DashboardUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Study workload",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${state.totalTasks} tasks across ${state.disciplineSummaries.size} disciplines",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DashboardStatGrid(state: DashboardUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DashboardStatCard(
                label = "Active",
                value = state.activeTasks.toString(),
                icon = Icons.Default.Pending,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            DashboardStatCard(
                label = "Done",
                value = state.doneTasks.toString(),
                icon = Icons.Default.CheckCircle,
                color = Color(0xFF1E8E3E),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DashboardStatCard(
                label = "Overdue",
                value = state.overdueTasks.toString(),
                icon = Icons.Default.Warning,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
            DashboardStatCard(
                label = "Today",
                value = state.dueTodayTasks.toString(),
                icon = Icons.Default.Today,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DashboardStatCard(
                label = "This week",
                value = state.dueThisWeekTasks.toString(),
                icon = Icons.Default.Event,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            DashboardStatCard(
                label = "Cancelled",
                value = state.cancelledTasks.toString(),
                icon = Icons.Default.Cancel,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun DashboardStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier.heightIn(min = 88.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = color.copy(alpha = 0.12f),
                contentColor = color
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ProgressOverviewCard(state: DashboardUiState) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Overall progress", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${(state.averageProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            LinearProgressIndicator(
                progress = { state.averageProgress.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WorkloadMetric(
                    label = "Estimated",
                    value = state.totalEstimatedTimeText,
                    modifier = Modifier.weight(1f)
                )
                WorkloadMetric(
                    label = "Logged",
                    value = state.totalLoggedTimeText,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun WorkloadMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun DashboardSectionTitle(
    title: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color)
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PriorityBadge(task.priority)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = task.disciplineName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${(task.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(task.timeLeftText, style = MaterialTheme.typography.labelSmall)
                Text(task.status.name, style = MaterialTheme.typography.labelSmall)
            }
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
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = discipline.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${discipline.activeTaskCount}/${discipline.totalTaskCount} active",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (discipline.overdueTaskCount > 0) {
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text("${discipline.overdueTaskCount} overdue") },
                        leadingIcon = {
                            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    )
                }
            }
            LinearProgressIndicator(
                progress = { discipline.progress.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = Color(discipline.color)
            )
        }
    }
}
