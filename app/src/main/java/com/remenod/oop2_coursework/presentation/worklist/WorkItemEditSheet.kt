package com.remenod.oop2_coursework.presentation.worklist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.remenod.oop2_coursework.domain.model.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkItemEditSheet(
    initialTitle: String = "",
    initialDescription: String = "",
    initialType: WorkItemType = WorkItemType.GENERIC,
    initialPriority: Priority = Priority.NORMAL,
    initialTotalPages: Int = 100,
    allowTypeChange: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        description: String,
        type: WorkItemType,
        priority: Priority,
        initialData: Map<String, Any> // Extension: more flexible initial data
    ) -> Unit
) {
    var title by rememberSaveable { mutableStateOf(initialTitle) }
    var description by rememberSaveable { mutableStateOf(initialDescription) }
    var selectedType by rememberSaveable { mutableStateOf(initialType) }
    var selectedPriority by rememberSaveable { mutableStateOf(initialPriority) }
    var totalPages by rememberSaveable { mutableStateOf(initialTotalPages.toString()) }
    
    // Programming specific initial data
    var commits by rememberSaveable { mutableStateOf("0") }
    var issues by rememberSaveable { mutableStateOf("0") }
    var tests by rememberSaveable { mutableStateOf("0") }

    val isValid = title.isNotBlank() &&
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
                            label = {
                                Text(
                                    text = type.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }

            if (selectedType == WorkItemType.READING) {
                OutlinedTextField(
                    value = totalPages,
                    onValueChange = { value ->
                        totalPages = value.filter { it.isDigit() }
                    },
                    label = { Text("Total pages") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            if (selectedType == WorkItemType.PROGRAMMING) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = commits,
                        onValueChange = { commits = it.filter { c -> c.isDigit() } },
                        label = { Text("Commits") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = issues,
                        onValueChange = { issues = it.filter { c -> c.isDigit() } },
                        label = { Text("Issues") },
                        modifier = Modifier.weight(1f)
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
                        label = {
                            Text(
                                text = priority.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    enabled = isValid,
                    onClick = {
                        val initialData = mutableMapOf<String, Any>()
                        if (selectedType == WorkItemType.READING) {
                            initialData["totalPages"] = totalPages.toIntOrNull() ?: 100
                        } else if (selectedType == WorkItemType.PROGRAMMING) {
                            initialData["commitsCount"] = commits.toIntOrNull() ?: 0
                            initialData["issuesResolved"] = issues.toIntOrNull() ?: 0
                            initialData["testsPassed"] = (tests.toDoubleOrNull() ?: 0.0) / 100.0
                        }

                        onConfirm(
                            title.trim(),
                            description.trim(),
                            selectedType,
                            selectedPriority,
                            initialData
                        )
                        onDismiss()
                    }
                ) {
                    Text("Save")
                }
            }
        }
    }
}
