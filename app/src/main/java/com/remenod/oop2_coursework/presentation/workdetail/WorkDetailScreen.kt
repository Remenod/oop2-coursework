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

                // Type-Specific Sections
                when (item.type) {
                    WorkItemType.READING -> item {
                        ReadingTaskSection(item, viewModel)
                    }
                    WorkItemType.PROGRAMMING -> item {
                        ProgrammingTaskSection(item, viewModel)
                    }
                    WorkItemType.EXAM -> {
                        item {
                            Text(text = "Topics", style = MaterialTheme.typography.titleMedium)
                        }
                        items(item.examTopics) { topic ->
                            ExamTopicItem(topic, onUpdate = { c -> viewModel.updateExamTopic(topic.index, c) }, onRemove = { viewModel.removeExamTopic(topic.index) })
                        }
                        item {
                            AddTopicSection(onAdd = { n, c -> viewModel.addExamTopic(n, c) })
                        }
                    }
                    WorkItemType.SEMINAR -> item {
                        SeminarTaskSection(item.seminarStages, viewModel)
                    }
                    WorkItemType.PROJECT -> {
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
                    WorkItemType.GENERIC -> {}
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
            onConfirm = { title, desc, type, priority, initialData ->
                viewModel.addSubTask(title, desc, type, priority, initialData)
            }
        )
    }
}

@Composable
fun ReadingTaskSection(item: WorkItemDetailUiModel, viewModel: WorkDetailViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Reading Progress", style = MaterialTheme.typography.titleSmall)
            Text("Pages: ${item.readPages} / ${item.totalPages}", style = MaterialTheme.typography.bodyMedium)
            Button(onClick = { showDialog = true }, modifier = Modifier.padding(top = 8.dp)) {
                Text("Update Pages")
            }
        }
    }

    if (showDialog) {
        var read by remember { mutableStateOf(item.readPages.toString()) }
        var total by remember { mutableStateOf(item.totalPages.toString()) }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Update Reading") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = read, onValueChange = { read = it.filter { c -> c.isDigit() } }, label = { Text("Read") })
                    OutlinedTextField(value = total, onValueChange = { total = it.filter { c -> c.isDigit() } }, label = { Text("Total") })
                }
            },
            confirmButton = {
                val r = read.toIntOrNull() ?: 0
                val t = total.toIntOrNull() ?: 0
                Button(enabled = t > 0 && r >= 0 && r <= t, onClick = { 
                    viewModel.updateReadingProgress(r, t)
                    showDialog = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun ProgrammingTaskSection(item: WorkItemDetailUiModel, viewModel: WorkDetailViewModel) {
    var commits by remember { mutableStateOf(item.commitsCount?.toString() ?: "0") }
    var issues by remember { mutableStateOf(item.issuesResolved?.toString() ?: "0") }
    var tests by remember { mutableFloatStateOf(item.testsPassed?.toFloat() ?: 0f) }

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Programming Stats", style = MaterialTheme.typography.titleSmall)
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = commits, onValueChange = { commits = it.filter { c -> c.isDigit() } }, label = { Text("Commits") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = issues, onValueChange = { issues = it.filter { c -> c.isDigit() } }, label = { Text("Issues") }, modifier = Modifier.weight(1f))
            }
            
            Text("Tests Passed: ${(tests * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
            Slider(value = tests, onValueChange = { tests = it })
            
            Button(onClick = { 
                viewModel.updateProgrammingStats(commits.toIntOrNull() ?: 0, issues.toIntOrNull() ?: 0, tests.toDouble())
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Update Stats")
            }
        }
    }
}

@Composable
fun ExamTopicItem(topic: ExamTopicUiModel, onUpdate: (Int) -> Unit, onRemove: () -> Unit) {
    var confidence by remember { mutableFloatStateOf(topic.confidence.toFloat()) }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = topic.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onRemove) { Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error) }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Slider(value = confidence, onValueChange = { confidence = it }, onValueChangeFinished = { onUpdate(confidence.toInt()) }, valueRange = 0f..100f, modifier = Modifier.weight(1f))
                Text(text = "${confidence.toInt()}%", modifier = Modifier.width(48.dp))
            }
        }
    }
}

@Composable
fun AddTopicSection(onAdd: (String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("New Topic") }, modifier = Modifier.weight(1f))
        Button(onClick = { 
            if (name.isNotBlank()) {
                onAdd(name, 50)
                name = ""
            }
        }) { Text("Add") }
    }
}

@Composable
fun SeminarTaskSection(stages: SeminarStagesUiModel?, viewModel: WorkDetailViewModel) {
    if (stages == null) return
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Preparation Stages", style = MaterialTheme.typography.titleSmall)
            SeminarStageRow("Topic Selected", stages.topicSelected) { viewModel.toggleSeminarStage(SeminarStageType.TOPIC_SELECTED) }
            SeminarStageRow("Materials Collected", stages.materialsCollected) { viewModel.toggleSeminarStage(SeminarStageType.MATERIALS_COLLECTED) }
            SeminarStageRow("Speech Prepared", stages.speechPrepared) { viewModel.toggleSeminarStage(SeminarStageType.SPEECH_PREPARED) }
            SeminarStageRow("Slides Prepared", stages.slidesPrepared) { viewModel.toggleSeminarStage(SeminarStageType.SLIDES_PREPARED) }
            SeminarStageRow("Rehearsal Done", stages.rehearsalDone) { viewModel.toggleSeminarStage(SeminarStageType.REHEARSAL_DONE) }
        }
    }
}

@Composable
fun SeminarStageRow(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onToggle() }) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        Text(text = label)
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
