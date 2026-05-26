package com.remenod.oop2_coursework.presentation.worklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.remenod.oop2_coursework.domain.model.AttachmentPurpose
import com.remenod.oop2_coursework.domain.model.Priority
import com.remenod.oop2_coursework.domain.model.WorkStatus

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkListScreen(
    viewModel: WorkListViewModel,
    onWorkItemClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.disciplineName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.actionError != null) {
                    item {
                        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.small) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(text = uiState.actionError!!, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                                IconButton(onClick = { viewModel.clearActionError() }) { Text("X") }
                            }
                        }
                    }
                }
                
                item {
                    WorkListControlsPanel(
                        state = uiState,
                        onQueryChange = viewModel::updateQuery,
                        onTypeFilterChange = viewModel::setTypeFilter,
                        onStatusFilterChange = viewModel::setStatusFilter,
                        onPriorityFilterChange = viewModel::setPriorityFilter,
                        onAttachmentPurposeFilterChange = viewModel::setAttachmentPurposeFilter,
                        onOverdueOnlyChange = viewModel::setOverdueOnly,
                        onGithubOnlyChange = viewModel::setGithubOnly,
                        onWithLogsOnlyChange = viewModel::setWithLogsOnly,
                        onSortChange = viewModel::setSortOption,
                        onClearFilters = viewModel::clearFilters
                    )
                }

                if (uiState.items.isEmpty()) {
                    item {
                        EmptyWorkListState(
                            hasAnyItems = uiState.totalItems > 0,
                            onClearFilters = viewModel::clearFilters
                        )
                    }
                }

                items(uiState.items) { item ->
                    WorkItemCard(
                        item = item, 
                        onClick = { onWorkItemClick(item.id) },
                        onDelete = { viewModel.deleteTask(item.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        WorkItemEditSheet(
            onDismiss = { showAddDialog = false },
            onConfirm = { result ->
                viewModel.addTask(result)
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkListControlsPanel(
    state: WorkListUiState,
    onQueryChange: (String) -> Unit,
    onTypeFilterChange: (WorkListTypeFilter) -> Unit,
    onStatusFilterChange: (WorkStatus?) -> Unit,
    onPriorityFilterChange: (Priority?) -> Unit,
    onAttachmentPurposeFilterChange: (AttachmentPurpose?) -> Unit,
    onOverdueOnlyChange: (Boolean) -> Unit,
    onGithubOnlyChange: (Boolean) -> Unit,
    onWithLogsOnlyChange: (Boolean) -> Unit,
    onSortChange: (WorkListSortOption) -> Unit,
    onClearFilters: () -> Unit
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search tasks") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (state.query.isNotBlank()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.items.size}/${state.totalItems} shown",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SortMenuButton(
                        selected = state.sortOption,
                        onSortChange = onSortChange
                    )
                    TextButton(onClick = onClearFilters) {
                        Text("Reset")
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WorkListTypeFilter.entries.forEach { type ->
                    FilterChip(
                        selected = state.typeFilter == type,
                        onClick = { onTypeFilterChange(type) },
                        label = { ChipText(type.label) }
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.attachmentPurposeFilter == null,
                    onClick = { onAttachmentPurposeFilterChange(null) },
                    label = { ChipText("Any attachment") }
                )
                AttachmentPurpose.entries.forEach { purpose ->
                    FilterChip(
                        selected = state.attachmentPurposeFilter == purpose,
                        onClick = { onAttachmentPurposeFilterChange(purpose) },
                        label = { ChipText(purpose.name) }
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.statusFilter == null,
                    onClick = { onStatusFilterChange(null) },
                    label = { ChipText("Any status") }
                )
                WorkStatus.entries.forEach { status ->
                    FilterChip(
                        selected = state.statusFilter == status,
                        onClick = { onStatusFilterChange(status) },
                        label = { ChipText(status.name) }
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.priorityFilter == null,
                    onClick = { onPriorityFilterChange(null) },
                    label = { ChipText("Any priority") }
                )
                Priority.entries.forEach { priority ->
                    FilterChip(
                        selected = state.priorityFilter == priority,
                        onClick = { onPriorityFilterChange(priority) },
                        label = { ChipText(priority.name) }
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.overdueOnly,
                    onClick = { onOverdueOnlyChange(!state.overdueOnly) },
                    label = { ChipText("Overdue") },
                    leadingIcon = if (state.overdueOnly) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
                FilterChip(
                    selected = state.githubOnly,
                    onClick = { onGithubOnlyChange(!state.githubOnly) },
                    label = { ChipText("GitHub") },
                    leadingIcon = if (state.githubOnly) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
                FilterChip(
                    selected = state.withLogsOnly,
                    onClick = { onWithLogsOnlyChange(!state.withLogsOnly) },
                    label = { ChipText("With logs") },
                    leadingIcon = if (state.withLogsOnly) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }
        }
    }
}

@Composable
fun SortMenuButton(
    selected: WorkListSortOption,
    onSortChange: (WorkListSortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(selected.label)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            WorkListSortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSortChange(option)
                        expanded = false
                    },
                    leadingIcon = if (selected == option) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null
                )
            }
        }
    }
}

@Composable
fun EmptyWorkListState(
    hasAnyItems: Boolean,
    onClearFilters: () -> Unit
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (hasAnyItems) Icons.Default.FilterAltOff else Icons.AutoMirrored.Filled.Assignment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
            Text(
                text = if (hasAnyItems) "No tasks match the current filters" else "No tasks yet",
                style = MaterialTheme.typography.titleMedium
            )
            if (hasAnyItems) {
                OutlinedButton(onClick = onClearFilters) {
                    Text("Reset filters")
                }
            }
        }
    }
}

@Composable
fun WorkItemCard(
    item: WorkItemCardUiModel,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = item.typeLabel,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(text = item.status.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    PriorityBadge(item.priority)
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { item.progressPercent.toFloat() },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(item.progressPercent * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            
            Text(
                text = item.progressExplanation,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (item.hasGitHubAttachment || item.hasLogs) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (item.hasGitHubAttachment) {
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { ChipText("GitHub") },
                            leadingIcon = { Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }
                    if (item.hasLogs) {
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { ChipText("Logs") },
                            leadingIcon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column {
                Text(text = "Due: ${item.deadlineText}", style = MaterialTheme.typography.labelSmall)
                Text(
                    text = item.timeLeftText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    fontWeight = if (item.isOverdue) FontWeight.Bold else FontWeight.Normal
                )
                Text(text = "Estimate: ${item.estimatedTimeText}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun ChipText(text: String) {
    Text(text = text, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

@Composable
fun PriorityBadge(priority: Priority) {
    val color = when (priority) {
        Priority.LOW -> Color.Gray
        Priority.NORMAL -> Color.Blue
        Priority.HIGH -> Color(0xFFFFA500)
        Priority.CRITICAL -> Color.Red
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        contentColor = color,
        shape = MaterialTheme.shapes.extraSmall,
        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = color)
    ) {
        Text(
            text = priority.name,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
