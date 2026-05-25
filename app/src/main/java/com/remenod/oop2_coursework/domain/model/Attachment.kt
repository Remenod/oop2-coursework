package com.remenod.oop2_coursework.domain.model

import java.time.LocalDateTime

abstract class Attachment(
    val id: String,
    val name: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    abstract fun open()
    abstract fun getDisplayName(): String
    abstract fun getOpenMode(): AttachmentOpenMode
}
