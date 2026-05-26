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

enum class AttachmentType {
    LINK,
    RESOURCE
}

enum class AttachmentSubtype {
    GITHUB,
    GOOGLE_CLASSROOM,
    LOCAL_FILE,
    CLOUD_FILE,
    UNKNOWN
}

enum class AttachmentPurpose {
    SOURCE_CODE,
    ASSIGNMENT_BRIEF,
    REFERENCE,
    SUBMISSION,
    DATASET,
    RUBRIC,
    NOTES,
    OUTPUT_ARTIFACT,
    LOCAL_RESOURCE,
    CLOUD_RESOURCE
}

enum class SeminarStageType {
    TOPIC_SELECTED,
    MATERIALS_COLLECTED,
    SPEECH_PREPARED,
    SLIDES_PREPARED,
    REHEARSAL_DONE
}
