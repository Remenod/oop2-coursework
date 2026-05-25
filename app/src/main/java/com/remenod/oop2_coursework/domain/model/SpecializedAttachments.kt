package com.remenod.oop2_coursework.domain.model

import com.remenod.oop2_coursework.domain.interfaces.Syncable
import com.remenod.oop2_coursework.domain.interfaces.Submittable
import java.time.LocalDateTime

abstract class LinkAttachment(
    id: Long,
    name: String,
    val url: String,
    createdAt: LocalDateTime = LocalDateTime.now()
) : Attachment(id, name, createdAt)

abstract class ResourceAttachment(
    id: Long,
    name: String,
    val path: String,
    createdAt: LocalDateTime = LocalDateTime.now()
) : Attachment(id, name, createdAt)

class LocalFileResource(
    id: Long,
    name: String,
    path: String
) : ResourceAttachment(id, name, path) {
    override fun open() {
        // TODO: Implement opening local file using Android Intent
    }

    override fun getDisplayName(): String = name

    override fun getOpenMode(): AttachmentOpenMode = AttachmentOpenMode.LOCAL
}

class CloudFileResource(
    id: Long,
    name: String,
    path: String,
    val cloudProvider: String
) : ResourceAttachment(id, name, path) {
    override fun open() {
        // TODO: Implement opening/downloading cloud file
    }

    override fun getDisplayName(): String = "[Cloud] $name"

    override fun getOpenMode(): AttachmentOpenMode = AttachmentOpenMode.CLOUD
}

class GitHubRepositoryLink(
    id: Long,
    name: String,
    url: String
) : LinkAttachment(id, name, url), Syncable {
    override fun open() {
        // TODO: Open URL in browser or external app
    }

    override fun getDisplayName(): String = "GitHub: $name"

    override fun getOpenMode(): AttachmentOpenMode = AttachmentOpenMode.BROWSER

    override fun sync() {
        // TODO: Sync commits, issues, branches via GitHub API
    }
}

class GoogleClassroomLink(
    id: Long,
    name: String,
    url: String
) : LinkAttachment(id, name, url), Syncable, Submittable {
    override fun open() {
        // TODO: Open Classroom assignment
    }

    override fun getDisplayName(): String = "Classroom: $name"

    override fun getOpenMode(): AttachmentOpenMode = AttachmentOpenMode.BROWSER

    override fun sync() {
        // TODO: Sync deadline and status from Google Classroom API
    }

    override fun submit() {
        // TODO: Submit assignment via Google Classroom API
    }
}
