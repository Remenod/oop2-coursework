package com.remenod.oop2_coursework.domain.model

import com.remenod.oop2_coursework.domain.interfaces.Syncable
import com.remenod.oop2_coursework.domain.interfaces.Submittable
import java.time.LocalDateTime

abstract class LinkAttachment(
    id: Long,
    title: String,
    var url: String,
    createdAt: LocalDateTime = LocalDateTime.now()
) : Attachment(id, title, createdAt)

abstract class ResourceAttachment(
    id: Long,
    title: String,
    var pathOrUrl: String,
    createdAt: LocalDateTime = LocalDateTime.now()
) : Attachment(id, title, createdAt)

class LocalFileResource(
    id: Long,
    title: String,
    path: String
) : ResourceAttachment(id, title, path) {
    override fun open() {
        // Mock opening local file
        println("Opening local file at: $pathOrUrl")
    }

    override fun getDisplayName(): String = title

    override fun getOpenMode(): AttachmentOpenMode = AttachmentOpenMode.LOCAL
}

class CloudFileResource(
    id: Long,
    title: String,
    path: String,
    val cloudProvider: String
) : ResourceAttachment(id, title, path) {
    override fun open() {
        // Mock opening cloud file
        println("Opening $cloudProvider file: $pathOrUrl")
    }

    override fun getDisplayName(): String = "[Cloud] $title"

    override fun getOpenMode(): AttachmentOpenMode = AttachmentOpenMode.CLOUD
}

class GitHubRepositoryLink(
    id: Long,
    title: String,
    url: String
) : LinkAttachment(id, title, url), Syncable {
    override fun open() {
        // Mock opening GitHub link
        println("Opening GitHub repo: $url")
    }

    override fun getDisplayName(): String = "GitHub: $title"

    override fun getOpenMode(): AttachmentOpenMode = AttachmentOpenMode.BROWSER

    override fun sync() {
        // Mock sync
        println("Syncing with GitHub API for: $url")
    }
}

class GoogleClassroomLink(
    id: Long,
    title: String,
    url: String
) : LinkAttachment(id, title, url), Syncable, Submittable {
    override fun open() {
        // Mock opening Classroom link
        println("Opening Google Classroom: $url")
    }

    override fun getDisplayName(): String = "Classroom: $title"

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
