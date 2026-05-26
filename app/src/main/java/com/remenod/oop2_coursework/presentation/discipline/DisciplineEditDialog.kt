package com.remenod.oop2_coursework.presentation.discipline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DisciplineEditDialog(
    initialName: String = "",
    initialTeacher: String = "",
    initialSemester: Int = 1,
    initialColor: Int = 0xFF4CAF50.toInt(),
    onDismiss: () -> Unit,
    onConfirm: (name: String, teacher: String, semester: Int, color: Int) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var teacher by remember { mutableStateOf(initialTeacher) }
    var semester by remember { mutableStateOf(initialSemester.toString()) }
    var selectedColor by remember { mutableStateOf(initialColor) }

    val colors = listOf(
        0xFF4CAF50.toInt(), 0xFF2196F3.toInt(), 0xFFF44336.toInt(),
        0xFFFFEB3B.toInt(), 0xFF9C27B0.toInt(), 0xFFFF9800.toInt()
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialName.isEmpty()) "Add Discipline" else "Edit Discipline") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = teacher,
                    onValueChange = { teacher = it },
                    label = { Text("Teacher") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = semester,
                    onValueChange = { semester = it },
                    label = { Text("Semester") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Select Color", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(colors) { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .clickable { selectedColor = color }
                                .let {
                                    if (selectedColor == color) it.background(Color.Black.copy(alpha = 0.2f)) else it
                                }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(name, teacher, semester.toIntOrNull() ?: 1, selectedColor)
                    onDismiss()
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
