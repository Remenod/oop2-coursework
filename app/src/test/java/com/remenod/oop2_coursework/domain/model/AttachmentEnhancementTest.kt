package com.remenod.oop2_coursework.domain.model

import com.remenod.oop2_coursework.data.persistence.mapper.AttachmentPersistenceMapper
import com.remenod.oop2_coursework.presentation.workdetail.AttachmentEditResult
import com.remenod.oop2_coursework.presentation.workdetail.AttachmentFactory
import org.junit.Assert.*
import org.junit.Test

class AttachmentEnhancementTest {

    @Test
    fun testGitHubUrlParserExtractsRepositoryAndBranch() {
        val info = GitHubUrlParser.parse("https://github.com/owner/repo/tree/main/src")

        assertNotNull(info)
        assertEquals("owner", info!!.owner)
        assertEquals("repo", info.repository)
        assertEquals("main", info.branch)
        assertEquals("src", info.path)
        assertEquals("https://github.com/owner/repo", info.canonicalUrl)
    }

    @Test
    fun testGitHubFactoryCreatesSourceCodeAttachment() {
        val attachment = AttachmentFactory.createFrom(
            AttachmentEditResult(
                title = "Repo",
                subtype = AttachmentSubtype.GITHUB,
                urlOrPath = "git@github.com:vision/repo.git",
                purpose = AttachmentPurpose.SOURCE_CODE,
                branch = "develop",
                notes = "Main coursework repository"
            )
        ) as GitHubRepositoryLink

        assertEquals("vision/repo", attachment.fullName)
        assertEquals("develop", attachment.effectiveBranch)
        assertEquals(AttachmentPurpose.SOURCE_CODE, attachment.purpose)
        assertTrue(attachment.programmingTaskSyncHint().contains("commits"))
    }

    @Test
    fun testCloudProviderDetection() {
        assertEquals("Google Drive", AttachmentFactory.detectCloudProvider("https://drive.google.com/file/d/abc"))
        assertEquals("Dropbox", AttachmentFactory.detectCloudProvider("https://dropbox.com/s/abc"))
        assertEquals("Cloud", AttachmentFactory.detectCloudProvider("https://example.com/file"))
    }

    @Test
    fun testAttachmentPersistencePreservesPurposeNotesAndBranch() {
        val original = GitHubRepositoryLink(
            id = 42L,
            title = "Repo",
            url = "https://github.com/owner/repo",
            branch = "main",
            purpose = AttachmentPurpose.SOURCE_CODE,
            notes = "Sync later with ProgrammingTask"
        )

        val record = AttachmentPersistenceMapper.mapToRecord(100L, original)
        val restored = AttachmentPersistenceMapper.restore(record) as GitHubRepositoryLink

        assertEquals(AttachmentPurpose.SOURCE_CODE, restored.purpose)
        assertEquals("Sync later with ProgrammingTask", restored.notes)
        assertEquals("main", restored.effectiveBranch)
        assertEquals("owner/repo", restored.fullName)
    }

    @Test
    fun testInvalidGitHubAttachmentRejectedByFactory() {
        val result = AttachmentEditResult(
            title = "Not GitHub",
            subtype = AttachmentSubtype.GITHUB,
            urlOrPath = "https://example.com/repo",
            purpose = AttachmentPurpose.SOURCE_CODE
        )

        assertNotNull(AttachmentFactory.validate(result))
    }
}
