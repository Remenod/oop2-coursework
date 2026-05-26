package com.remenod.oop2_coursework.presentation.workdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.remenod.oop2_coursework.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkDetailScreen(
    viewModel: WorkDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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

                if (item.checklist.isNotEmpty()) {
                    item {
                        Text(text = "Checklist", style = MaterialTheme.typography.titleMedium)
                    }
                    items(item.checklist) { checkItem ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = checkItem.isCompleted, onCheckedChange = {})
                            Text(text = checkItem.text)
                        }
                    }
                }

                if (item.subTasks.isNotEmpty()) {
                    item {
                        Text(text = "Sub-tasks", style = MaterialTheme.typography.titleMedium)
                    }
                    items(item.subTasks) { subTask ->
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
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
