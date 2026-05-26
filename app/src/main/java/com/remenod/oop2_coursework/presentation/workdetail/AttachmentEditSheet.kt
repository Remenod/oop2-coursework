package com.remenod.oop2_coursework.presentation.workdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.remenod.oop2_coursework.domain.model.AttachmentSubtype

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AttachmentEditSheet(
    onDismiss: () -> Unit,
    onConfirm: (AttachmentEditResult) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var target by rememberSaveable { mutableStateOf("") }
    var selectedSubtype by rememberSaveable { mutableStateOf(AttachmentSubtype.GITHUB) }

    val isWeb = selectedSubtype == AttachmentSubtype.GITHUB || 
                selectedSubtype == AttachmentSubtype.GOOGLE_CLASSROOM || 
                selectedSubtype == AttachmentSubtype.UNKNOWN // Generic Web

    val isValid = title.isNotBlank() && target.isNotBlank() && 
            (!isWeb || (target.startsWith("http://") || target.startsWith("https://")))

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
            Text("Add Attachment", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Type", style = MaterialTheme.typography.labelLarge)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val types = listOf(
                    AttachmentSubtype.GITHUB to "GitHub",
                    AttachmentSubtype.GOOGLE_CLASSROOM to "Classroom",
                    AttachmentSubtype.UNKNOWN to "Web Link",
                    AttachmentSubtype.LOCAL_FILE to "Local File",
                    AttachmentSubtype.CLOUD_FILE to "Cloud File"
                )
                types.forEach { (type, label) ->
                    FilterChip(
                        selected = selectedSubtype == type,
                        onClick = { selectedSubtype = type },
                        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }

            OutlinedTextField(
                value = target,
                onValueChange = { target = it },
                label = { Text(if (isWeb) "URL (https://...)" else "Path / URI") },
                modifier = Modifier.fillMaxWidth(),
                isError = target.isNotBlank() && isWeb && !target.startsWith("http")
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
                        onConfirm(AttachmentEditResult(title.trim(), selectedSubtype, target.trim()))
                        onDismiss()
                    }
                ) { Text("Add") }
            }
        }
    }
}
