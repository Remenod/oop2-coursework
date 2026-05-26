package com.remenod.oop2_coursework.presentation.workdetail

import com.remenod.oop2_coursework.domain.model.*

object AttachmentFactory {
    fun createFrom(result: AttachmentEditResult): Attachment {
        val title = result.title.trim()
        val target = result.urlOrPath.trim()
        val notes = result.notes.trim()

        require(title.isNotBlank()) { "Attachment title cannot be blank" }
        require(target.isNotBlank()) { "Attachment target cannot be blank" }

        return when (result.subtype) {
            AttachmentSubtype.GITHUB -> {
                val info = GitHubUrlParser.parse(target, result.branch)
                requireNotNull(info) { "GitHub attachment must be a valid github.com repository URL" }
                GitHubRepositoryLink(
                    id = 0,
                    title = title,
                    url = info.canonicalUrl,
                    branch = result.branch?.trim()?.takeIf { it.isNotBlank() } ?: info.branch,
                    purpose = result.purpose,
                    notes = notes
                )
            }
            AttachmentSubtype.GOOGLE_CLASSROOM -> {
                require(isHttpUrl(target)) { "Classroom link must start with http:// or https://" }
                require(target.contains("classroom.google.com", ignoreCase = true)) {
                    "Google Classroom link should point to classroom.google.com"
                }
                GoogleClassroomLink(id = 0, title = title, url = target, purpose = result.purpose, notes = notes)
            }
            AttachmentSubtype.LOCAL_FILE -> {
                require(!isHttpUrl(target)) { "Local file attachment should use a local path, file:// URI, or content:// URI" }
                LocalFileResource(id = 0, title = title, path = target, purpose = result.purpose, notes = notes)
            }
            AttachmentSubtype.CLOUD_FILE -> {
                val provider = result.provider?.trim()?.takeIf { it.isNotBlank() } ?: detectCloudProvider(target)
                CloudFileResource(id = 0, title = title, path = target, cloudProvider = provider, purpose = result.purpose, notes = notes)
            }
            AttachmentSubtype.UNKNOWN -> {
                require(isHttpUrl(target)) { "Web link must start with http:// or https://" }
                GenericWebLink(id = 0, title = title, url = target, purpose = result.purpose, notes = notes)
            }
        }
    }

    fun validate(result: AttachmentEditResult): String? {
        return try {
            createFrom(result)
            null
        } catch (e: IllegalArgumentException) {
            e.message ?: "Invalid attachment"
        }
    }

    fun detectCloudProvider(target: String): String {
        val lower = target.lowercase()
        return when {
            "drive.google.com" in lower || "docs.google.com" in lower -> "Google Drive"
            "dropbox.com" in lower -> "Dropbox"
            "onedrive.live.com" in lower || "1drv.ms" in lower -> "OneDrive"
            "sharepoint.com" in lower -> "SharePoint"
            "icloud.com" in lower -> "iCloud"
            else -> "Cloud"
        }
    }

    private fun isHttpUrl(value: String): Boolean {
        return value.startsWith("http://") || value.startsWith("https://")
    }
}
