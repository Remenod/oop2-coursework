package com.remenod.oop2_coursework.presentation.workdetail

import com.remenod.oop2_coursework.domain.model.AttachmentPurpose
import com.remenod.oop2_coursework.domain.model.AttachmentSubtype

data class AttachmentEditResult(
    val title: String,
    val subtype: AttachmentSubtype,
    val urlOrPath: String,
    val purpose: AttachmentPurpose,
    val notes: String = "",
    val branch: String? = null,
    val provider: String? = null
)
