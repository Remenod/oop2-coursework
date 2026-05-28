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
    var repositorySnapshot: GitHubRepositorySnapshot = GitHubRepositorySnapshot(defaultBranch = branch),
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
    val activeIssuesCount: Int get() = repositorySnapshot.activeIssuesCount
    val openPullRequestsCount: Int get() = repositorySnapshot.openPullRequestsCount
    val importableCandidates: List<GitHubWorkCandidate>
        get() = repositorySnapshot.workCandidates.filter {
            it.state == GitHubWorkCandidateState.OPEN || it.state == GitHubWorkCandidateState.DRAFT
        }

    override fun open() {
        markOpened()
        println("Opening GitHub repo: ${repositoryInfo?.canonicalUrl ?: url}")
    }

    override fun getDisplayName(): String = fullName?.let { "GitHub: $it" } ?: "GitHub: $title"

    override fun getOpenMode(): AttachmentOpenMode = AttachmentOpenMode.BROWSER

    override fun sync() {
        repositorySnapshot = createStubSnapshot()
        println("GitHub sync placeholder for: ${repositoryInfo?.fullName ?: url}")
    }

    fun syncHint(): String {
        val repo = fullName ?: title
        val branchText = effectiveBranch?.let { " on branch $it" } ?: ""
        return "Future GitHub sync can update repository activity, open pull requests, active issues, and importable work items for $repo$branchText."
    }

    private fun createStubSnapshot(now: LocalDateTime = LocalDateTime.now()): GitHubRepositorySnapshot {
        val baseUrl = repositoryInfo?.canonicalUrl ?: url.trimEnd('/')
        val branchName = effectiveBranch ?: repositorySnapshot.defaultBranch ?: "main"
        return GitHubRepositorySnapshot(
            activeIssuesCount = 2,
            openPullRequestsCount = 1,
            defaultBranch = branchName,
            lastRepositoryActivityAt = now.minusHours(3),
            syncedAt = now,
            workCandidates = listOf(
                GitHubWorkCandidate(
                    type = GitHubWorkCandidateType.ISSUE,
                    number = 1,
                    title = "Clarify assignment requirements",
                    url = "$baseUrl/issues/1",
                    state = GitHubWorkCandidateState.OPEN,
                    updatedAt = now.minusDays(1)
                ),
                GitHubWorkCandidate(
                    type = GitHubWorkCandidateType.ISSUE,
                    number = 2,
                    title = "Prepare submission notes",
                    url = "$baseUrl/issues/2",
                    state = GitHubWorkCandidateState.OPEN,
                    updatedAt = now.minusHours(5)
                ),
                GitHubWorkCandidate(
                    type = GitHubWorkCandidateType.PULL_REQUEST,
                    number = 3,
                    title = "Review coursework changes",
                    url = "$baseUrl/pull/3",
                    state = GitHubWorkCandidateState.DRAFT,
                    updatedAt = now.minusHours(3)
                )
            )
        )
    }
}

data class GitHubRepositorySnapshot(
    val activeIssuesCount: Int = 0,
    val openPullRequestsCount: Int = 0,
    val defaultBranch: String? = null,
    val lastRepositoryActivityAt: LocalDateTime? = null,
    val syncedAt: LocalDateTime? = null,
    val workCandidates: List<GitHubWorkCandidate> = emptyList()
)

data class GitHubWorkCandidate(
    val type: GitHubWorkCandidateType,
    val number: Int,
    val title: String,
    val url: String,
    val state: GitHubWorkCandidateState,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

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
