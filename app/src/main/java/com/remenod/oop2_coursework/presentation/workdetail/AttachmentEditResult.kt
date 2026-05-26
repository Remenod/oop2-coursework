package com.remenod.oop2_coursework.presentation.workdetail

import com.remenod.oop2_coursework.domain.model.AttachmentSubtype

data class AttachmentEditResult(
    val title: String,
    val subtype: AttachmentSubtype,
    val urlOrPath: String
)
