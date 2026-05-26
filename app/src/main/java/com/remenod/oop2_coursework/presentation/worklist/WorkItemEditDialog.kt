package com.remenod.oop2_coursework.presentation.worklist

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remenod.oop2_coursework.domain.model.*

@Composable
fun WorkItemEditDialog(
    initialTitle: String = "",
    initialDescription: String = "",
    initialType: WorkItemType = WorkItemType.GENERIC,
    initialPriority: Priority = Priority.NORMAL,
    allowTypeChange: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, type: WorkItemType, priority: Priority, readingPages: Int?) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var selectedType by remember { mutableStateOf(initialType) }
    var selectedPriority by remember { mutableStateOf(initialPriority) }
    var totalPages by remember { mutableStateOf("100") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialTitle.isEmpty()) "Add Task" else "Edit Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (allowTypeChange) {
                    Text("Type", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        WorkItemType.values().filter { 
                            it == WorkItemType.GENERIC || it == WorkItemType.PROJECT || it == WorkItemType.READING 
                        }.forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(type.name) }
                            )
                        }
                    }
                }

                if (selectedType == WorkItemType.READING) {
                    OutlinedTextField(
                        value = totalPages,
                        onValueChange = { totalPages = it },
                        label = { Text("Total Pages") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text("Priority", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Priority.values().forEach { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            label = { Text(priority.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(title, description, selectedType, selectedPriority, totalPages.toIntOrNull())
                    onDismiss()
                },
                enabled = title.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
