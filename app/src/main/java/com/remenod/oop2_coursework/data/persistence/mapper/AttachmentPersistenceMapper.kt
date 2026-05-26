package com.remenod.oop2_coursework.data.persistence.mapper

import com.remenod.oop2_coursework.data.persistence.model.AttachmentRecord
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
            lastOpenedAt = domain.lastOpenedAt,
            createdAt = domain.createdAt
        )
    }

    fun restore(record: AttachmentRecord): Attachment {
        val attachment = when (record.subType) {
            AttachmentSubtype.GITHUB -> GitHubRepositoryLink(
                id = record.id,
                title = record.name,
                url = record.urlOrPath,
                branch = record.branch,
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
