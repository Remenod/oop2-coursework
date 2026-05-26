package com.remenod.oop2_coursework.data.persistence.mapper

import com.remenod.oop2_coursework.data.persistence.model.DisciplineRecord
import com.remenod.oop2_coursework.domain.model.Discipline
import com.remenod.oop2_coursework.domain.model.WorkItem

object DisciplinePersistenceMapper {

    fun mapToRecord(domain: Discipline): DisciplineRecord {
        return DisciplineRecord(
            id = domain.id,
            name = domain.name,
            teacherName = domain.teacherName,
            semester = domain.semester,
            color = domain.color
        )
    }

    fun restore(record: DisciplineRecord, workItems: List<WorkItem>): Discipline {
        return Discipline(
            id = record.id,
            name = record.name,
            teacherName = record.teacherName,
            semester = record.semester,
            color = record.color
        ).apply {
            workItems.forEach { addWorkItem(it) }
        }
    }
}
