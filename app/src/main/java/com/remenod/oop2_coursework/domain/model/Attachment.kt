package com.remenod.oop2_coursework.domain.model

import java.time.LocalDateTime

abstract class Attachment(
    val id: Long,
    var title: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var purpose: AttachmentPurpose = AttachmentPurpose.REFERENCE,
    var notes: String = "",
    var lastOpenedAt: LocalDateTime? = null
) {
    abstract fun open()
    abstract fun getDisplayName(): String
    abstract fun getOpenMode(): AttachmentOpenMode

    fun markOpened(now: LocalDateTime = LocalDateTime.now()) {
        lastOpenedAt = now
    }
}
