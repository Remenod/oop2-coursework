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
        totalPages: Int?
    ) -> Unit
) {
    var title by rememberSaveable { mutableStateOf(initialTitle) }
    var description by rememberSaveable { mutableStateOf(initialDescription) }
    var selectedType by rememberSaveable { mutableStateOf(initialType) }
    var selectedPriority by rememberSaveable { mutableStateOf(initialPriority) }
    var totalPages by rememberSaveable { mutableStateOf(initialTotalPages.toString()) }

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
                    listOf(
                        WorkItemType.GENERIC,
                        WorkItemType.READING,
                        WorkItemType.PROJECT
                    ).forEach { type ->
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

            Text("Priority", style = MaterialTheme.typography.labelLarge)

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Priority.values().forEach { priority ->
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
                        onConfirm(
                            title.trim(),
                            description.trim(),
                            selectedType,
                            selectedPriority,
                            totalPages.toIntOrNull()
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
