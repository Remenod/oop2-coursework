package com.remenod.oop2_coursework.presentation.workdetail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun AttachmentSection(
    attachments: List<AttachmentUiModel>,
    onAdd: () -> Unit,
    onRemove: (Long) -> Unit,
    onOpen: (AttachmentUiModel) -> Unit,
    onSync: (Long) -> Unit,
    onSubmit: (Long) -> Unit,
    onImportCandidates: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Attachments", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Links, resources, source code, assignment briefs and deliverables",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add attachment")
            }
        }

        if (attachments.isEmpty()) {
            Text("No attachments", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        } else {
            attachments.forEach { attachment ->
                AttachmentItem(
                    attachment = attachment,
                    onRemove = { onRemove(attachment.id) },
                    onOpen = { onOpen(attachment) },
                    onSync = { onSync(attachment.id) },
                    onSubmit = { onSubmit(attachment.id) },
                    onImportCandidates = { onImportCandidates(attachment.id) }
                )
            }
        }
    }
}

@Composable
fun AttachmentItem(
    attachment: AttachmentUiModel,
    onRemove: () -> Unit,
    onOpen: () -> Unit,
    onSync: () -> Unit,
    onSubmit: () -> Unit,
    onImportCandidates: () -> Unit
) {
    val icon = when (attachment.subtypeLabel) {
        "GitHub" -> Icons.Default.Code
        "Classroom" -> Icons.Default.School
        "Local File" -> Icons.Default.Description
        "Cloud File" -> Icons.Default.Cloud
        else -> Icons.Default.Language
    }

    val iconColor = when (attachment.subtypeLabel) {
        "GitHub" -> Color(0xFF6E5494)
        "Classroom" -> Color(0xFF1E8E3E)
        "Local File" -> Color(0xFFF4B400)
        "Cloud File" -> Color(0xFF4285F4)
        else -> MaterialTheme.colorScheme.primary
    }

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(attachment.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "${attachment.subtypeLabel} • ${attachment.purposeLabel} • ${attachment.createdAtText}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (attachment.repositoryFullName != null) {
                        Text(
                            text = "Repository: ${attachment.repositoryFullName}${attachment.branchLabel?.let { " • $it" } ?: ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }

            if (attachment.activeIssuesCount != null || attachment.openPullRequestsCount != null) {
                Text(
                    text = "Issues: ${attachment.activeIssuesCount ?: 0} • PRs: ${attachment.openPullRequestsCount ?: 0}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            attachment.syncedAtText?.let {
                Text(
                    text = "Synced: $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            attachment.lastRepositoryActivityText?.let {
                Text(
                    text = "Last repository activity: $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                attachment.target,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (attachment.providerLabel != null) {
                Text("Provider: ${attachment.providerLabel}", style = MaterialTheme.typography.labelSmall)
            }

            if (attachment.notes.isNotBlank()) {
                Text(attachment.notes, style = MaterialTheme.typography.bodySmall)
            }

            if (attachment.syncHint != null) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = attachment.syncHint,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Text("Last opened: ${attachment.lastOpenedText}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onOpen) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Open")
                }
                if (attachment.canSync) {
                    TextButton(onClick = onSync) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Sync")
                    }
                }
                if (attachment.canSubmit) {
                    TextButton(onClick = onSubmit) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Submit")
                    }
                }
                if (attachment.canImportCandidates) {
                    TextButton(onClick = onImportCandidates) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Import (${attachment.importableCandidateCount})")
                    }
                }
            }
        }
    }
}
