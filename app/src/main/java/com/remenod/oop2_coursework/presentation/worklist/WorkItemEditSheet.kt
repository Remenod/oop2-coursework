package com.remenod.oop2_coursework.presentation.worklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.remenod.oop2_coursework.domain.model.*
import com.remenod.oop2_coursework.presentation.common.DateTimeUiFormatter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkItemEditSheet(
    initialTitle: String = "",
    initialDescription: String = "",
    initialType: WorkItemType = WorkItemType.GENERIC,
    initialStatus: WorkStatus = WorkStatus.CREATED,
    initialPriority: Priority = Priority.NORMAL,
    initialDeadline: LocalDateTime? = null,
    initialEstimatedMinutes: Int = 0,
    initialTotalPages: Int = 100,
    initialCommits: Int = 0,
    initialRequiredCommits: Int = 5,
    initialIssues: Int = 0,
    initialRequiredIssues: Int = 2,
    initialTests: Double = 0.0,
    allowTypeChange: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (WorkItemEditResult) -> Unit
) {
    var title by rememberSaveable { mutableStateOf(initialTitle) }
    var description by rememberSaveable { mutableStateOf(initialDescription) }
    var selectedType by rememberSaveable { mutableStateOf(initialType) }
    var selectedStatus by rememberSaveable { mutableStateOf(initialStatus) }
    var selectedPriority by rememberSaveable { mutableStateOf(initialPriority) }

    LaunchedEffect(allowDoneStatus) {
        if (!allowDoneStatus && selectedStatus == WorkStatus.DONE) {
            selectedStatus = WorkStatus.CREATED
        }
    }
    
    var deadlineInput by rememberSaveable { 
        mutableStateOf(DateTimeUiFormatter.formatInput(initialDeadline)) 
    }
    
    var showDatePicker by remember { mutableStateOf(false) }

    var estimatedMinutes by rememberSaveable { 
        mutableStateOf(initialEstimatedMinutes.toString()) 
    }

    var totalPages by rememberSaveable { mutableStateOf(initialTotalPages.toString()) }
    var commits by rememberSaveable { mutableStateOf(initialCommits.toString()) }
    var reqCommits by rememberSaveable { mutableStateOf(initialRequiredCommits.toString()) }
    var issues by rememberSaveable { mutableStateOf(initialIssues.toString()) }
    var reqIssues by rememberSaveable { mutableStateOf(initialRequiredIssues.toString()) }
    var tests by rememberSaveable { mutableFloatStateOf(initialTests.toFloat()) }

    val deadlineValid = DateTimeUiFormatter.isValidInput(deadlineInput)
    val parsedDeadline = DateTimeUiFormatter.parseInput(deadlineInput)
    val estimatedValid = (estimatedMinutes.toIntOrNull() ?: -1) >= 0

    val isValid = title.isNotBlank() && deadlineValid && estimatedValid &&
            (selectedType != WorkItemType.READING || (totalPages.toIntOrNull() ?: 0) > 0)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (initialTitle.isBlank()) "Add Task" else "Edit Task",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            if (allowTypeChange) {
                Text("Type", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WorkItemType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(text = type.name, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        )
                    }
                }
            }

            Text("Status", style = MaterialTheme.typography.labelLarge)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val visibleStatuses = if (allowDoneStatus) {
                    WorkStatus.entries
                } else {
                    WorkStatus.entries.filter { it != WorkStatus.DONE }
                }
                visibleStatuses.forEach { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = status },
                        label = { Text(text = status.name, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }

            Text("Priority", style = MaterialTheme.typography.labelLarge)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Priority.entries.forEach { priority ->
                    FilterChip(
                        selected = selectedPriority == priority,
                        onClick = { selectedPriority = priority },
                        label = { Text(text = priority.name, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = estimatedMinutes,
                    onValueChange = { estimatedMinutes = it.filter { c -> c.isDigit() } },
                    label = { Text("Estimate (min)") },
                    modifier = Modifier.weight(1f),
                    isError = !estimatedValid
                )
                
                OutlinedTextField(
                    value = deadlineInput,
                    onValueChange = { deadlineInput = it },
                    label = { Text("Deadline") },
                    modifier = Modifier.weight(2f),
                    isError = !deadlineValid,
                    placeholder = { Text("yyyy-MM-dd HH:mm") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Select date")
                        }
                    }
                )
            }

            // Quick Deadline Buttons
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val now = LocalDateTime.now()
                DeadlineButton("None") { deadlineInput = "" }
                DeadlineButton("Today") { 
                    deadlineInput = DateTimeUiFormatter.formatInput(now.withHour(23).withMinute(59)) 
                }
                DeadlineButton("Tomorrow") { 
                    deadlineInput = DateTimeUiFormatter.formatInput(now.plusDays(1).withHour(23).withMinute(59)) 
                }
                DeadlineButton("+7 Days") { 
                    deadlineInput = DateTimeUiFormatter.formatInput(now.plusDays(7).withHour(23).withMinute(59)) 
                }
            }

            // Type-specific minimal fields
            if (selectedType == WorkItemType.READING) {
                OutlinedTextField(
                    value = totalPages,
                    onValueChange = { totalPages = it.filter { c -> c.isDigit() } },
                    label = { Text("Total Pages") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            if (selectedType == WorkItemType.PROGRAMMING) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Targets", style = MaterialTheme.typography.labelLarge)
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
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                Button(
                    enabled = isValid,
                    onClick = {
                        val result = WorkItemEditResult(
                            title = title.trim(),
                            description = description.trim(),
                            type = selectedType,
                            status = selectedStatus,
                            priority = selectedPriority,
                            deadline = parsedDeadline,
                            estimatedMinutes = estimatedMinutes.toIntOrNull() ?: 0,
                            totalPages = totalPages.toIntOrNull(),
                            commitsCount = commits.toIntOrNull(),
                            requiredCommits = reqCommits.toIntOrNull(),
                            issuesResolved = issues.toIntOrNull(),
                            requiredIssues = reqIssues.toIntOrNull(),
                            testsPassed = tests.toDouble()
                        )
                        onConfirm(result)
                        onDismiss()
                    }
                ) { Text("Save") }
            }
        }
    }

    if (showDatePicker) {
        val initialMillis = parsedDeadline?.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
                        deadlineInput = DateTimeUiFormatter.formatInput(date.withHour(23).withMinute(59))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun DeadlineButton(label: String, onClick: () -> Unit) {
    AssistChip(onClick = onClick, label = { Text(label) })
}
