package com.remenod.oop2_coursework.data.persistence.mapper

import com.remenod.oop2_coursework.data.persistence.model.AttachmentRecord
import com.remenod.oop2_coursework.data.persistence.model.GitHubWorkCandidateRecord
import com.remenod.oop2_coursework.domain.model.*

object AttachmentPersistenceMapper {

    fun mapToRecord(workItemId: Long, domain: Attachment): AttachmentRecord {
        val type = if (domain is LinkAttachment) AttachmentType.LINK else AttachmentType.RESOURCE
        val subType = when (domain) {
            is GitHubRepositoryLink -> AttachmentSubtype.GITHUB
            is GoogleClassroomLink -> AttachmentSubtype.GOOGLE_CLASSROOM
            is LocalFileResource -> AttachmentSubtype.LOCAL_FILE
            is CloudFileResource -> AttachmentSubtype.CLOUD_FILE
            is GenericWebLink -> AttachmentSubtype.UNKNOWN
            else -> AttachmentSubtype.UNKNOWN
        }

        return AttachmentRecord(
            id = domain.id,
            workItemId = workItemId,
            name = domain.title,
            type = type,
            subType = subType,
            urlOrPath = when (domain) {
                is LinkAttachment -> domain.url
                is ResourceAttachment -> domain.pathOrUrl
                else -> ""
            },
            provider = (domain as? CloudFileResource)?.cloudProvider,
            purpose = domain.purpose,
            notes = domain.notes,
            branch = (domain as? GitHubRepositoryLink)?.effectiveBranch,
            repositoryOwner = (domain as? GitHubRepositoryLink)?.owner,
            repositoryName = (domain as? GitHubRepositoryLink)?.repositoryName,
            defaultBranch = (domain as? GitHubRepositoryLink)?.repositorySnapshot?.defaultBranch,
            activeIssuesCount = (domain as? GitHubRepositoryLink)?.activeIssuesCount,
            openPullRequestsCount = (domain as? GitHubRepositoryLink)?.openPullRequestsCount,
            lastRepositoryActivityAt = (domain as? GitHubRepositoryLink)?.repositorySnapshot?.lastRepositoryActivityAt,
            syncedAt = (domain as? GitHubRepositoryLink)?.repositorySnapshot?.syncedAt,
            lastOpenedAt = domain.lastOpenedAt,
            createdAt = domain.createdAt
        )
    }

    fun mapGitHubCandidates(domain: Attachment): List<GitHubWorkCandidateRecord> {
        val github = domain as? GitHubRepositoryLink ?: return emptyList()
        return github.repositorySnapshot.workCandidates.mapIndexed { index, candidate ->
            GitHubWorkCandidateRecord(
                id = 0,
                attachmentId = github.id,
                type = candidate.type,
                number = candidate.number,
                title = candidate.title,
                url = candidate.url,
                state = candidate.state,
                createdAt = candidate.createdAt,
                updatedAt = candidate.updatedAt,
                sortOrder = index
            )
        }
    }

    fun restore(
        record: AttachmentRecord,
        githubCandidates: List<GitHubWorkCandidateRecord> = emptyList()
    ): Attachment {
        val attachment = when (record.subType) {
            AttachmentSubtype.GITHUB -> GitHubRepositoryLink(
                id = record.id,
                title = record.name,
                url = record.urlOrPath,
                branch = record.branch,
                repositorySnapshot = GitHubRepositorySnapshot(
                    activeIssuesCount = record.activeIssuesCount ?: 0,
                    openPullRequestsCount = record.openPullRequestsCount ?: 0,
                    defaultBranch = record.defaultBranch,
                    lastRepositoryActivityAt = record.lastRepositoryActivityAt,
                    syncedAt = record.syncedAt,
                    workCandidates = githubCandidates.sortedBy { it.sortOrder }.map {
                        GitHubWorkCandidate(
                            type = it.type,
                            number = it.number,
                            title = it.title,
                            url = it.url,
                            state = it.state,
                            createdAt = it.createdAt,
                            updatedAt = it.updatedAt
                        )
                    }
                ),
                createdAt = record.createdAt,
                purpose = record.purpose,
                notes = record.notes
            )
            AttachmentSubtype.GOOGLE_CLASSROOM -> GoogleClassroomLink(
                id = record.id,
                title = record.name,
                url = record.urlOrPath,
                createdAt = record.createdAt,
                purpose = record.purpose,
                notes = record.notes
            )
            AttachmentSubtype.LOCAL_FILE -> LocalFileResource(
                id = record.id,
                title = record.name,
                path = record.urlOrPath,
                createdAt = record.createdAt,
                purpose = record.purpose,
                notes = record.notes
            )
            AttachmentSubtype.CLOUD_FILE -> CloudFileResource(
                id = record.id,
                title = record.name,
                path = record.urlOrPath,
                cloudProvider = record.provider ?: "Unknown",
                createdAt = record.createdAt,
                purpose = record.purpose,
                notes = record.notes
            )
            AttachmentSubtype.UNKNOWN -> GenericWebLink(
                id = record.id,
                title = record.name,
                url = record.urlOrPath,
                createdAt = record.createdAt,
                purpose = record.purpose,
                notes = record.notes
            )
        }
        attachment.lastOpenedAt = record.lastOpenedAt
        return attachment
    }
}
