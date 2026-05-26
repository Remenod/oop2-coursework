package com.remenod.oop2_coursework.presentation.workdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkLogEditSheet(
    onDismiss: () -> Unit,
    onConfirm: (message: String, minutesSpent: Int) -> Unit
) {
    var message by rememberSaveable { mutableStateOf("") }
    var minutesSpent by rememberSaveable { mutableStateOf("0") }

    val isValid = message.isNotBlank() && (minutesSpent.toIntOrNull() ?: -1) >= 0

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
            Text("Add Work Log", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("What did you do?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            OutlinedTextField(
                value = minutesSpent,
                onValueChange = { minutesSpent = it.filter { c -> c.isDigit() } },
                label = { Text("Minutes spent") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                Button(
                    enabled = isValid,
                    onClick = {
                        onConfirm(message.trim(), minutesSpent.toIntOrNull() ?: 0)
                        onDismiss()
                    }
                ) { Text("Add Log") }
            }
        }
    }
}
