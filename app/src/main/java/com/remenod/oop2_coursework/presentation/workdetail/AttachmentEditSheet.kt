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
import com.remenod.oop2_coursework.domain.model.AttachmentPurpose
import com.remenod.oop2_coursework.domain.model.AttachmentSubtype
import com.remenod.oop2_coursework.domain.model.GitHubUrlParser
import com.remenod.oop2_coursework.domain.model.WorkItemType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AttachmentEditSheet(
    taskType: WorkItemType,
    onDismiss: () -> Unit,
    onConfirm: (AttachmentEditResult) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var target by rememberSaveable { mutableStateOf("") }
    var branch by rememberSaveable { mutableStateOf("") }
    var provider by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var selectedSubtype by rememberSaveable { mutableStateOf(defaultSubtypeFor(taskType)) }
    var selectedPurpose by rememberSaveable { mutableStateOf(defaultPurposeFor(taskType, selectedSubtype)) }

    val githubInfo = remember(target, branch, selectedSubtype) {
        if (selectedSubtype == AttachmentSubtype.GITHUB) GitHubUrlParser.parse(target, branch.takeIf { it.isNotBlank() }) else null
    }

    val effectiveProvider = remember(target, provider, selectedSubtype) {
        if (selectedSubtype == AttachmentSubtype.CLOUD_FILE) {
            provider.takeIf { it.isNotBlank() } ?: AttachmentFactory.detectCloudProvider(target)
        } else provider
    }

    val draft = AttachmentEditResult(
        title = title.trim(),
        subtype = selectedSubtype,
        urlOrPath = target.trim(),
        purpose = selectedPurpose,
        notes = notes.trim(),
        branch = branch.trim().takeIf { it.isNotBlank() },
        provider = effectiveProvider.takeIf { it.isNotBlank() }
    )

    val validationError = if (target.isBlank() || title.isBlank()) null else AttachmentFactory.validate(draft)
    val isValid = title.isNotBlank() && target.isNotBlank() && validationError == null

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

            if (taskType == WorkItemType.PROGRAMMING) {
                AssistChip(
                    onClick = {
                        selectedSubtype = AttachmentSubtype.GITHUB
                        selectedPurpose = AttachmentPurpose.SOURCE_CODE
                    },
                    label = { Text("Programming task: GitHub source code is recommended") }
                )
            }

            Text("Type", style = MaterialTheme.typography.labelLarge)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                attachmentTypeOptions().forEach { (type, label) ->
                    FilterChip(
                        selected = selectedSubtype == type,
                        onClick = {
                            selectedSubtype = type
                            selectedPurpose = defaultPurposeFor(taskType, type)
                            if (type != AttachmentSubtype.GITHUB) branch = ""
                            if (type != AttachmentSubtype.CLOUD_FILE) provider = ""
                        },
                        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }

            Text("Purpose", style = MaterialTheme.typography.labelLarge)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                purposeOptions(taskType).forEach { purpose ->
                    FilterChip(
                        selected = selectedPurpose == purpose,
                        onClick = { selectedPurpose = purpose },
                        label = { Text(purpose.name.replace('_', ' '), maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }

            OutlinedTextField(
                value = target,
                onValueChange = {
                    target = it
                    if (title.isBlank() && selectedSubtype == AttachmentSubtype.GITHUB) {
                        GitHubUrlParser.parse(it)?.fullName?.let { repo -> title = repo }
                    }
                    if (selectedSubtype == AttachmentSubtype.CLOUD_FILE && provider.isBlank()) {
                        provider = AttachmentFactory.detectCloudProvider(it)
                    }
                },
                label = { Text(targetLabel(selectedSubtype)) },
                modifier = Modifier.fillMaxWidth(),
                isError = validationError != null,
                supportingText = {
                    when {
                        validationError != null -> Text(validationError)
                        selectedSubtype == AttachmentSubtype.GITHUB && githubInfo != null -> Text("Repository: ${githubInfo.fullName}${githubInfo.branch?.let { " • branch $it" } ?: ""}")
                        selectedSubtype == AttachmentSubtype.CLOUD_FILE && target.isNotBlank() -> Text("Provider: $effectiveProvider")
                        else -> Text(targetHint(selectedSubtype))
                    }
                }
            )

            if (selectedSubtype == AttachmentSubtype.GITHUB) {
                OutlinedTextField(
                    value = branch,
                    onValueChange = { branch = it },
                    label = { Text("Branch, optional") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Reserved for future GitHub sync with ProgrammingTask commits/issues/tests") }
                )
            }

            if (selectedSubtype == AttachmentSubtype.CLOUD_FILE) {
                OutlinedTextField(
                    value = provider,
                    onValueChange = { provider = it },
                    label = { Text("Cloud provider") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / usage context") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                supportingText = { Text("Example: assignment source, dataset, final PDF, API docs, starter code") }
            )

            if (selectedSubtype == AttachmentSubtype.GITHUB && taskType == WorkItemType.PROGRAMMING) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Future synergy: this GitHub attachment can later sync commits, closed issues, tests, branch status and repository activity into ProgrammingTask.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
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
                        onConfirm(draft)
                        onDismiss()
                    }
                ) { Text("Add") }
            }
        }
    }
}

private fun defaultSubtypeFor(taskType: WorkItemType): AttachmentSubtype {
    return if (taskType == WorkItemType.PROGRAMMING) AttachmentSubtype.GITHUB else AttachmentSubtype.UNKNOWN
}

private fun defaultPurposeFor(taskType: WorkItemType, subtype: AttachmentSubtype): AttachmentPurpose {
    return when {
        subtype == AttachmentSubtype.GITHUB -> AttachmentPurpose.SOURCE_CODE
        subtype == AttachmentSubtype.GOOGLE_CLASSROOM -> AttachmentPurpose.ASSIGNMENT_BRIEF
        subtype == AttachmentSubtype.LOCAL_FILE -> AttachmentPurpose.LOCAL_RESOURCE
        subtype == AttachmentSubtype.CLOUD_FILE -> AttachmentPurpose.CLOUD_RESOURCE
        taskType == WorkItemType.EXAM -> AttachmentPurpose.REFERENCE
        taskType == WorkItemType.READING -> AttachmentPurpose.REFERENCE
        else -> AttachmentPurpose.REFERENCE
    }
}

private fun attachmentTypeOptions(): List<Pair<AttachmentSubtype, String>> = listOf(
    AttachmentSubtype.GITHUB to "GitHub",
    AttachmentSubtype.GOOGLE_CLASSROOM to "Classroom",
    AttachmentSubtype.UNKNOWN to "Web Link",
    AttachmentSubtype.LOCAL_FILE to "Local File",
    AttachmentSubtype.CLOUD_FILE to "Cloud File"
)

private fun purposeOptions(taskType: WorkItemType): List<AttachmentPurpose> {
    val base = mutableListOf(
        AttachmentPurpose.REFERENCE,
        AttachmentPurpose.ASSIGNMENT_BRIEF,
        AttachmentPurpose.SUBMISSION,
        AttachmentPurpose.RUBRIC,
        AttachmentPurpose.NOTES,
        AttachmentPurpose.OUTPUT_ARTIFACT
    )
    if (taskType == WorkItemType.PROGRAMMING) {
        base.add(0, AttachmentPurpose.SOURCE_CODE)
    }
    if (taskType == WorkItemType.EXAM || taskType == WorkItemType.PROJECT) {
        base.add(AttachmentPurpose.DATASET)
    }
    base.add(AttachmentPurpose.LOCAL_RESOURCE)
    base.add(AttachmentPurpose.CLOUD_RESOURCE)
    return base.distinct()
}

private fun targetLabel(subtype: AttachmentSubtype): String {
    return when (subtype) {
        AttachmentSubtype.GITHUB -> "GitHub repository URL"
        AttachmentSubtype.GOOGLE_CLASSROOM -> "Google Classroom URL"
        AttachmentSubtype.UNKNOWN -> "Web URL"
        AttachmentSubtype.LOCAL_FILE -> "Local path / file URI"
        AttachmentSubtype.CLOUD_FILE -> "Cloud URL / shared path"
    }
}

private fun targetHint(subtype: AttachmentSubtype): String {
    return when (subtype) {
        AttachmentSubtype.GITHUB -> "Example: https://github.com/owner/repo or git@github.com:owner/repo.git"
        AttachmentSubtype.GOOGLE_CLASSROOM -> "Expected: https://classroom.google.com/..."
        AttachmentSubtype.UNKNOWN -> "Expected: https://..."
        AttachmentSubtype.LOCAL_FILE -> "Example: /storage/emulated/0/Download/file.pdf or content://..."
        AttachmentSubtype.CLOUD_FILE -> "Google Drive, Dropbox, OneDrive, SharePoint or other shared resource"
    }
}
