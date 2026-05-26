package com.remenod.oop2_coursework.presentation.workdetail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AttachmentSection(
    attachments: List<AttachmentUiModel>,
    onAdd: () -> Unit,
    onRemove: (Long) -> Unit,
    onOpen: (AttachmentUiModel) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Attachments", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add attachment")
            }
        }

        if (attachments.isEmpty()) {
            Text("No attachments", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        } else {
            attachments.forEach { attachment ->
                AttachmentItem(attachment, onRemove = { onRemove(attachment.id) }, onOpen = { onOpen(attachment) })
            }
        }
    }
}

@Composable
fun AttachmentItem(
    attachment: AttachmentUiModel,
    onRemove: () -> Unit,
    onOpen: () -> Unit
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (attachment.typeLabel == "Link") Icons.Default.OpenInBrowser else Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(attachment.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text("${attachment.subtypeLabel} • ${attachment.createdAtText}", style = MaterialTheme.typography.labelSmall)
                Text(attachment.target, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, maxLines = 1)
            }

            Row {
                IconButton(onClick = onOpen) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open")
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
