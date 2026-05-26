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
            createdAt = domain.createdAt
        )
    }

    fun restore(record: AttachmentRecord): Attachment {
        return when (record.subType) {
            AttachmentSubtype.GITHUB -> GitHubRepositoryLink(record.id, record.name, record.urlOrPath)
            AttachmentSubtype.GOOGLE_CLASSROOM -> GoogleClassroomLink(record.id, record.name, record.urlOrPath)
            AttachmentSubtype.LOCAL_FILE -> LocalFileResource(record.id, record.name, record.urlOrPath)
            AttachmentSubtype.CLOUD_FILE -> CloudFileResource(record.id, record.name, record.urlOrPath, record.provider ?: "Unknown")
            AttachmentSubtype.UNKNOWN -> GenericWebLink(record.id, record.name, record.urlOrPath)
        }
    }
}
