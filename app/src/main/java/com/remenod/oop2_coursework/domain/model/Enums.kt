package com.remenod.oop2_coursework.domain.model

enum class WorkStatus {
    CREATED,
    IN_PROGRESS,
    BLOCKED,
    DONE,
    CANCELLED
}

enum class Priority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}

enum class SubmissionStatus {
    NOT_SUBMITTED,
    DRAFT,
    SUBMITTED,
    LATE,
    GRADED
}

enum class AttachmentOpenMode {
    LOCAL,
    BROWSER,
    CLOUD,
    EXTERNAL_APP
}

enum class WorkItemType {
    GENERIC,
    PROGRAMMING,
    EXAM,
    SEMINAR,
    READING,
    PROJECT
}
