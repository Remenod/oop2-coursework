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
import androidx.compose.runtime.saveable.rememberSaveable
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
    var showAddAttachmentDialog by remember { mutableStateOf(false) }
    var showAddLogDialog by remember { mutableStateOf(false) }

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
                            InfoRow("Deadline", item.deadlineText)
                            InfoRow("Time Left", item.timeLeftText)
                            InfoRow("Estimate", item.estimatedTimeText)
                            InfoRow("Created", item.createdAtText)
                            InfoRow("Updated", item.updatedAtText)
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
                    AttachmentSection(
                        attachments = item.attachments,
                        onAdd = { showAddAttachmentDialog = true },
                        onRemove = viewModel::removeAttachment,
                        onOpen = { viewModel.openAttachment(it.id) }
                    )
                }

                item {
                    WorkLogSection(
                        logs = item.logs,
                        onAdd = { showAddLogDialog = true },
                        onRemove = viewModel::removeLogEntry
                    )
                }

                if (item.type != WorkItemType.PROJECT) {
                    item {
                        ChecklistSection(
                            items = item.checklist,
                            onAdd = viewModel::addChecklistItem,
                            onCheckedChange = viewModel::setChecklistItemCompleted,
                            onRemove = viewModel::removeChecklistItem
                        )
                    }
                }

                // Type-Specific Sections
                when (item.type) {
                    WorkItemType.READING -> item {
                        ReadingTaskSection(item, viewModel::updateReadingProgress)
                    }
                    WorkItemType.PROGRAMMING -> item {
                        ProgrammingTaskSection(item, viewModel::updateProgrammingStats)
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
                        SeminarTaskSection(item.seminarStages, viewModel::setSeminarStage)
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
                        onClick = { viewModel.toggleCompletion() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = item.status == WorkStatus.DONE || item.canBeCompleted
                    ) {
                        Text(if (item.status == WorkStatus.DONE) "Mark as not done" else "Mark as done")
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
            initialStatus = uiState.item!!.status,
            initialDeadline = uiState.item!!.deadline,
            initialEstimatedMinutes = uiState.item!!.estimatedMinutes,
            initialType = uiState.item!!.type,
            allowTypeChange = false,
            onDismiss = { showEditDialog = false },
            onConfirm = { result ->
                viewModel.updateMetadata(result)
            }
        )
    }

    if (showAddSubTaskDialog) {
        WorkItemEditSheet(
            onDismiss = { showAddSubTaskDialog = false },
            onConfirm = { result ->
                viewModel.addSubTask(result)
            }
        )
    }

    if (showAddAttachmentDialog) {
        AttachmentEditSheet(
            onDismiss = { showAddAttachmentDialog = false },
            onConfirm = viewModel::addAttachment
        )
    }

    if (showAddLogDialog) {
        WorkLogEditSheet(
            onDismiss = { showAddLogDialog = false },
            onConfirm = viewModel::addManualLog
        )
    }
}

@Composable
fun ChecklistSection(
    items: List<ChecklistUiModel>,
    onAdd: (String) -> Unit,
    onCheckedChange: (Int, Boolean) -> Unit,
    onRemove: (Int) -> Unit
) {
    var newItem by rememberSaveable { mutableStateOf("") }

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Checklist", style = MaterialTheme.typography.titleSmall)

            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.isCompleted,
                        onCheckedChange = { checked ->
                            onCheckedChange(item.index, checked)
                        }
                    )
                    Text(
                        text = item.text,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onRemove(item.index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove checklist item")
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newItem,
                    onValueChange = { newItem = it },
                    label = { Text("New checklist item") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    enabled = newItem.isNotBlank(),
                    onClick = {
                        onAdd(newItem)
                        newItem = ""
                    }
                ) {
                    Text("Add")
                }
            }
        }
    }
}

@Composable
fun ReadingTaskSection(
    item: WorkItemDetailUiModel,
    onUpdate: (readPages: Int, totalPages: Int) -> Unit
) {
    var read by rememberSaveable(item.id, item.readPages) {
        mutableStateOf((item.readPages ?: 0).toString())
    }
    var total by rememberSaveable(item.id, item.totalPages) {
        mutableStateOf((item.totalPages ?: 100).toString())
    }

    val readInt = read.toIntOrNull() ?: 0
    val totalInt = total.toIntOrNull() ?: 0
    val valid = totalInt > 0 && readInt in 0..totalInt

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Reading Progress", style = MaterialTheme.typography.titleSmall)
            Text("Pages: $readInt / $totalInt")

            if (totalInt > 0) {
                Slider(
                    value = readInt.coerceIn(0, totalInt).toFloat(),
                    onValueChange = { read = it.toInt().toString() },
                    valueRange = 0f..totalInt.toFloat()
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(-10, -1, 1, 10).forEach { delta ->
                    OutlinedButton(
                        onClick = {
                            if (totalInt > 0) {
                                read = (readInt + delta).coerceIn(0, totalInt).toString()
                            }
                        }
                    ) {
                        Text(if (delta > 0) "+$delta" else "$delta")
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = read,
                    onValueChange = { read = it.filter(Char::isDigit) },
                    label = { Text("Read") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = total,
                    onValueChange = { total = it.filter(Char::isDigit) },
                    label = { Text("Total") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                enabled = valid,
                onClick = { onUpdate(readInt, totalInt) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Reading Progress")
            }
        }
    }
}

@Composable
fun ProgrammingTaskSection(
    item: WorkItemDetailUiModel,
    onUpdate: (Int, Int, Int, Int, Double) -> Unit
) {
    var commits by rememberSaveable(item.id, item.commitsCount) { mutableStateOf((item.commitsCount ?: 0).toString()) }
    var reqCommits by rememberSaveable(item.id, item.requiredCommits) { mutableStateOf((item.requiredCommits ?: 5).toString()) }
    var issues by rememberSaveable(item.id, item.issuesResolved) { mutableStateOf((item.issuesResolved ?: 0).toString()) }
    var reqIssues by rememberSaveable(item.id, item.requiredIssues) { mutableStateOf((item.requiredIssues ?: 2).toString()) }
    var tests by rememberSaveable(item.id, item.testsPassed) { mutableStateOf(item.testsPassed?.toFloat() ?: 0f) }

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Programming Stats", style = MaterialTheme.typography.titleSmall)
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = commits, onValueChange = { commits = it.filter { c -> c.isDigit() } }, label = { Text("Commits") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = reqCommits, onValueChange = { reqCommits = it.filter { c -> c.isDigit() } }, label = { Text("Target") }, modifier = Modifier.weight(1f))
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = issues, onValueChange = { issues = it.filter { c -> c.isDigit() } }, label = { Text("Issues") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = reqIssues, onValueChange = { reqIssues = it.filter { c -> c.isDigit() } }, label = { Text("Target") }, modifier = Modifier.weight(1f))
            }
            
            Text("Tests Passed: ${(tests * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
            Slider(value = tests, onValueChange = { tests = it })
            
            Button(onClick = { 
                onUpdate(
                    commits.toIntOrNull() ?: 0, 
                    reqCommits.toIntOrNull() ?: 5,
                    issues.toIntOrNull() ?: 0,
                    reqIssues.toIntOrNull() ?: 2,
                    tests.toDouble()
                )
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Update Stats")
            }
        }
    }
}

@Composable
fun ExamTopicItem(topic: ExamTopicUiModel, onUpdate: (Int) -> Unit, onRemove: () -> Unit) {
    var confidence by rememberSaveable(topic.index, topic.confidence) { mutableStateOf(topic.confidence.toFloat()) }
    
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
    var name by rememberSaveable { mutableStateOf("") }
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
fun SeminarTaskSection(stages: SeminarStagesUiModel?, onToggle: (SeminarStageType, Boolean) -> Unit) {
    if (stages == null) return
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Preparation Stages", style = MaterialTheme.typography.titleSmall)
            SeminarStageRow("Topic Selected", stages.topicSelected) { onToggle(SeminarStageType.TOPIC_SELECTED, it) }
            SeminarStageRow("Materials Collected", stages.materialsCollected) { onToggle(SeminarStageType.MATERIALS_COLLECTED, it) }
            SeminarStageRow("Speech Prepared", stages.speechPrepared) { onToggle(SeminarStageType.SPEECH_PREPARED, it) }
            SeminarStageRow("Slides Prepared", stages.slidesPrepared) { onToggle(SeminarStageType.SLIDES_PREPARED, it) }
            SeminarStageRow("Rehearsal Done", stages.rehearsalDone) { onToggle(SeminarStageType.REHEARSAL_DONE, it) }
        }
    }
}

@Composable
fun SeminarStageRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
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
