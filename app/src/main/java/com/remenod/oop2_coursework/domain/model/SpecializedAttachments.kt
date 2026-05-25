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
        // Mock opening local file
        println("Opening local file at: $path")
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
        // Mock opening cloud file
        println("Opening $cloudProvider file: $path")
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
        // Mock opening GitHub link
        println("Opening GitHub repo: $url")
    }

    override fun getDisplayName(): String = "GitHub: $name"

    override fun getOpenMode(): AttachmentOpenMode = AttachmentOpenMode.BROWSER

    override fun sync() {
        // Mock sync
        println("Syncing with GitHub API for: $url")
    }
}

class GoogleClassroomLink(
    id: Long,
    name: String,
    url: String
) : LinkAttachment(id, name, url), Syncable, Submittable {
    override fun open() {
        // Mock opening Classroom link
        println("Opening Google Classroom: $url")
    }

    override fun getDisplayName(): String = "Classroom: $name"

    override fun getOpenMode(): AttachmentOpenMode = AttachmentOpenMode.BROWSER

    override fun sync() {
        // Mock sync
        println("Syncing with Google Classroom API for: $url")
    }

    override fun submit() {
        // Mock submit
        println("Submitting work to Google Classroom: $url")
    }
}
