package com.remenod.oop2_coursework.domain.model

import com.remenod.oop2_coursework.domain.interfaces.Syncable
import com.remenod.oop2_coursework.domain.interfaces.Submittable
import java.time.LocalDateTime

abstract class LinkAttachment(
    id: Long,
    title: String,
    var url: String,
    createdAt: LocalDateTime = LocalDateTime.now(),
    purpose: AttachmentPurpose = AttachmentPurpose.REFERENCE,
    notes: String = ""
) : Attachment(id, title, createdAt, purpose, notes)

abstract class ResourceAttachment(
    id: Long,
    title: String,
    var pathOrUrl: String,
    createdAt: LocalDateTime = LocalDateTime.now(),
    purpose: AttachmentPurpose = AttachmentPurpose.REFERENCE,
    notes: String = ""
) : Attachment(id, title, createdAt, purpose, notes)

class LocalFileResource(
    id: Long,
    title: String,
    path: String,
    createdAt: LocalDateTime = LocalDateTime.now(),
    purpose: AttachmentPurpose = AttachmentPurpose.LOCAL_RESOURCE,
    notes: String = ""
) : ResourceAttachment(id, title, path, createdAt = createdAt, purpose = purpose, notes = notes) {
    override fun open() {
        markOpened()
        println("Opening local file at: $pathOrUrl")
    }

    override fun getDisplayName(): String = title

    override fun getOpenMode(): AttachmentOpenMode = AttachmentOpenMode.LOCAL
}

class CloudFileResource(
    id: Long,
    title: String,
    path: String,
    val cloudProvider: String,
    createdAt: LocalDateTime = LocalDateTime.now(),
    purpose: AttachmentPurpose = AttachmentPurpose.CLOUD_RESOURCE,
    notes: String = ""
) : ResourceAttachment(id, title, path, createdAt = createdAt, purpose = purpose, notes = notes) {
    override fun open() {
        markOpened()
        println("Opening $cloudProvider file: $pathOrUrl")
    }

    override fun getDisplayName(): String = "[$cloudProvider] $title"

    override fun getOpenMode(): AttachmentOpenMode = AttachmentOpenMode.CLOUD
}

class GitHubRepositoryLink(
    id: Long,
    title: String,
    url: String,
    val branch: String? = null,
    createdAt: LocalDateTime = LocalDateTime.now(),
    purpose: AttachmentPurpose = AttachmentPurpose.SOURCE_CODE,
    notes: String = ""
) : LinkAttachment(id, title, GitHubUrlParser.parse(url, branch)?.canonicalUrl ?: url.trim(), createdAt = createdAt, purpose = purpose, notes = notes), Syncable {
    val repositoryInfo: GitHubRepositoryInfo? = GitHubUrlParser.parse(url, branch)
    val owner: String? get() = repositoryInfo?.owner
    val repositoryName: String? get() = repositoryInfo?.repository
    val fullName: String? get() = repositoryInfo?.fullName
    val effectiveBranch: String? get() = branch ?: repositoryInfo?.branch
    val cloneUrl: String? get() = repositoryInfo?.cloneUrl
    val issuesUrl: String? get() = repositoryInfo?.issuesUrl
    val pullRequestsUrl: String? get() = repositoryInfo?.pullRequestsUrl
    val commitsUrl: String? get() = repositoryInfo?.commitsUrl

    override fun open() {
        markOpened()
        println("Opening GitHub repo: ${repositoryInfo?.canonicalUrl ?: url}")
    }

    override fun getDisplayName(): String = fullName?.let { "GitHub: $it" } ?: "GitHub: $title"

    override fun getOpenMode(): AttachmentOpenMode = AttachmentOpenMode.BROWSER

    override fun sync() {
        // Reserved for a future GitHub API integration.
        // Intended synergy: update ProgrammingTask commits/issues/tests from repo data.
        println("GitHub sync placeholder for: ${repositoryInfo?.fullName ?: url}")
    }

    fun programmingTaskSyncHint(): String {
        val repo = fullName ?: title
        val branchText = effectiveBranch?.let { " on branch $it" } ?: ""
        return "Future GitHub sync can update commits/issues/tests for $repo$branchText."
    }
}

class GoogleClassroomLink(
    id: Long,
    title: String,
    url: String,
    createdAt: LocalDateTime = LocalDateTime.now(),
    purpose: AttachmentPurpose = AttachmentPurpose.ASSIGNMENT_BRIEF,
    notes: String = ""
) : LinkAttachment(id, title, url.trim(), createdAt = createdAt, purpose = purpose, notes = notes), Syncable, Submittable {
    override fun open() {
        markOpened()
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

class GenericWebLink(
    id: Long,
    title: String,
    url: String,
    createdAt: LocalDateTime = LocalDateTime.now(),
    purpose: AttachmentPurpose = AttachmentPurpose.REFERENCE,
    notes: String = ""
) : LinkAttachment(id, title, url.trim(), createdAt = createdAt, purpose = purpose, notes = notes) {
    override fun open() {
        markOpened()
        println("Opening web link: $url")
    }

    override fun getDisplayName(): String = title

    override fun getOpenMode(): AttachmentOpenMode = AttachmentOpenMode.BROWSER
}
