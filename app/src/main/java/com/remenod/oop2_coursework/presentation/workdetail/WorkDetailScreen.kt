package com.remenod.oop2_coursework.presentation.workdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.remenod.oop2_coursework.domain.model.*
import com.remenod.oop2_coursework.presentation.worklist.WorkItemEditSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkDetailScreen(
    viewModel: WorkDetailViewModel,
    onBack: () -> Unit,
    onSubTaskClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddSubTaskDialog by remember { mutableStateOf(false) }
    var showReadingProgressDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { 
                        viewModel.deleteThisTask()
                        onBack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { padding ->
        val item = uiState.item
        if (uiState.isLoading || item == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (uiState.isLoading) CircularProgressIndicator()
                else Text(uiState.error ?: "Unknown error")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(text = item.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = item.description, style = MaterialTheme.typography.bodyLarge)
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            InfoRow("Status", item.status.name)
                            InfoRow("Priority", item.priority.name)
                            InfoRow("Deadline", item.deadline)
                            InfoRow("Type", item.typeName)
                        }
                    }
                }

                item {
                    Text(text = "Progress: ${(item.progressPercent * 100).toInt()}%", style = MaterialTheme.typography.titleMedium)
                    LinearProgressIndicator(
                        progress = { item.progressPercent.toFloat() },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(text = item.progressExplanation, style = MaterialTheme.typography.bodySmall)
                }

                if (item.type == WorkItemType.READING) {
                    item {
                        Button(onClick = { showReadingProgressDialog = true }) {
                            Text("Update Reading Progress")
                        }
                    }
                }

                if (item.subTasks.isNotEmpty() || item.type == WorkItemType.PROJECT) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Sub-tasks", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            IconButton(onClick = { showAddSubTaskDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Sub-task")
                            }
                        }
                    }
                    items(item.subTasks) { subTask ->
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth().clickable { 
                                onSubTaskClick(subTask.id)
                            }
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = subTask.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text(text = "${(subTask.progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                                }
                                if (subTask.isDone) {
                                    Text("✅")
                                }
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = { viewModel.completeTask() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = item.status != WorkStatus.DONE && item.canBeCompleted
                    ) {
                        Text(if (item.status == WorkStatus.DONE) "Completed" else "Mark as done")
                    }
                }
            }
        }
    }

    if (showEditDialog && uiState.item != null) {
        WorkItemEditSheet(
            initialTitle = uiState.item!!.title,
            initialDescription = uiState.item!!.description,
            initialPriority = uiState.item!!.priority,
            initialType = uiState.item!!.type,
            allowTypeChange = false,
            onDismiss = { showEditDialog = false },
            onConfirm = { title, desc, _, priority, _ ->
                viewModel.updateBasicInfo(title, desc, priority)
            }
        )
    }

    if (showAddSubTaskDialog) {
        WorkItemEditSheet(
            onDismiss = { showAddSubTaskDialog = false },
            onConfirm = { title, desc, type, priority, pages ->
                viewModel.addSubTask(title, desc, type, priority, pages)
            }
        )
    }

    if (showReadingProgressDialog && uiState.item != null) {
        val currentRead = uiState.item!!.readPages ?: 0
        val currentTotal = uiState.item!!.totalPages ?: 100
        
        var pagesRead by remember { mutableStateOf(currentRead.toString()) }
        var totalPages by remember { mutableStateOf(currentTotal.toString()) }
        
        AlertDialog(
            onDismissRequest = { showReadingProgressDialog = false },
            title = { Text("Update Progress") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pagesRead,
                        onValueChange = { pagesRead = it.filter { c -> c.isDigit() } },
                        label = { Text("Pages Read") }
                    )
                    OutlinedTextField(
                        value = totalPages,
                        onValueChange = { totalPages = it.filter { c -> c.isDigit() } },
                        label = { Text("Total Pages") }
                    )
                }
            },
            confirmButton = {
                val r = pagesRead.toIntOrNull() ?: 0
                val t = totalPages.toIntOrNull() ?: 0
                Button(
                    enabled = t > 0 && r >= 0 && r <= t,
                    onClick = { 
                        viewModel.updateReadingProgress(r, t)
                        showReadingProgressDialog = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showReadingProgressDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold)
        Text(text = value)
    }
}
