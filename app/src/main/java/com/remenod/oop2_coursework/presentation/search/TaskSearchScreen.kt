package com.remenod.oop2_coursework.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.remenod.oop2_coursework.domain.model.AttachmentPurpose
import com.remenod.oop2_coursework.domain.model.Priority
import com.remenod.oop2_coursework.domain.model.WorkStatus
import com.remenod.oop2_coursework.presentation.common.DebouncedSearchField
import com.remenod.oop2_coursework.presentation.worklist.PriorityBadge

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskSearchScreen(
    viewModel: TaskSearchViewModel,
    onTaskClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search tasks") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
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
                item {
                    TaskSearchControlsPanel(
                        state = state,
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

                if (state.items.isEmpty()) {
                    item {
                        EmptySearchState(
                            hasAnyItems = state.totalItems > 0,
                            onClearFilters = viewModel::clearFilters
                        )
                    }
                }

                items(state.items, key = { it.id }) { item ->
                    TaskSearchResultCard(
                        item = item,
                        onClick = { onTaskClick(item.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskSearchControlsPanel(
    state: TaskSearchUiState,
    onQueryChange: (String) -> Unit,
    onTypeFilterChange: (TaskSearchTypeFilter) -> Unit,
    onStatusFilterChange: (WorkStatus?) -> Unit,
    onPriorityFilterChange: (Priority?) -> Unit,
    onAttachmentPurposeFilterChange: (AttachmentPurpose?) -> Unit,
    onOverdueOnlyChange: (Boolean) -> Unit,
    onGithubOnlyChange: (Boolean) -> Unit,
    onWithLogsOnlyChange: (Boolean) -> Unit,
    onSortChange: (TaskSearchSortOption) -> Unit,
    onClearFilters: () -> Unit
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DebouncedSearchField(
                query = state.query,
                onQueryChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = "Search across all disciplines"
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
                    TaskSearchSortMenuButton(
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
                TaskSearchTypeFilter.entries.forEach { type ->
                    FilterChip(
                        selected = state.typeFilter == type,
                        onClick = { onTypeFilterChange(type) },
                        label = { SearchChipText(type.label) }
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
                    label = { SearchChipText("Any status") }
                )
                WorkStatus.entries.forEach { status ->
                    FilterChip(
                        selected = state.statusFilter == status,
                        onClick = { onStatusFilterChange(status) },
                        label = { SearchChipText(status.name) }
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
                    label = { SearchChipText("Any priority") }
                )
                Priority.entries.forEach { priority ->
                    FilterChip(
                        selected = state.priorityFilter == priority,
                        onClick = { onPriorityFilterChange(priority) },
                        label = { SearchChipText(priority.name) }
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
                    label = { SearchChipText("Any attachment") }
                )
                AttachmentPurpose.entries.forEach { purpose ->
                    FilterChip(
                        selected = state.attachmentPurposeFilter == purpose,
                        onClick = { onAttachmentPurposeFilterChange(purpose) },
                        label = { SearchChipText(purpose.name) }
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ToggleFilterChip(
                    selected = state.overdueOnly,
                    label = "Overdue",
                    onClick = { onOverdueOnlyChange(!state.overdueOnly) }
                )
                ToggleFilterChip(
                    selected = state.githubOnly,
                    label = "GitHub",
                    onClick = { onGithubOnlyChange(!state.githubOnly) }
                )
                ToggleFilterChip(
                    selected = state.withLogsOnly,
                    label = "With logs",
                    onClick = { onWithLogsOnlyChange(!state.withLogsOnly) }
                )
            }
        }
    }
}

@Composable
fun ToggleFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { SearchChipText(label) },
        leadingIcon = if (selected) {
            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
        } else null
    )
}

@Composable
fun TaskSearchSortMenuButton(
    selected: TaskSearchSortOption,
    onSortChange: (TaskSearchSortOption) -> Unit
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
            TaskSearchSortOption.entries.forEach { option ->
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
fun TaskSearchResultCard(
    item: TaskSearchItemUiModel,
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PriorityBadge(item.priority)
                Text(
                    text = item.typeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = item.status.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.disciplineName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { item.progressPercent.toFloat() },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text("${(item.progressPercent * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.timeLeftText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (item.hasGitHubAttachment) {
                        Icon(Icons.Default.Code, contentDescription = "GitHub attachment", modifier = Modifier.size(18.dp))
                    }
                    if (item.hasLogs) {
                        Icon(Icons.Default.History, contentDescription = "Has logs", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EmptySearchState(
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
                text = if (hasAnyItems) "No tasks match the current filters" else "No tasks available",
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
fun SearchChipText(text: String) {
    Text(text = text, maxLines = 1, overflow = TextOverflow.Ellipsis)
}
