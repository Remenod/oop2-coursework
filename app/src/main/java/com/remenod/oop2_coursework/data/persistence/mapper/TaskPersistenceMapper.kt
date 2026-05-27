package com.remenod.oop2_coursework.data.persistence.mapper

import com.remenod.oop2_coursework.data.persistence.model.PersistenceBundle
import com.remenod.oop2_coursework.data.persistence.model.PersistenceSnapshot
import com.remenod.oop2_coursework.domain.model.Discipline

object TaskPersistenceMapper {

    fun decompose(disciplines: List<Discipline>): PersistenceSnapshot {
        val bundles = disciplines.map { discipline ->
            WorkItemPersistenceMapper.decomposeToBundle(discipline.id, discipline.workItems)
        }

        return PersistenceSnapshot(
            disciplines = disciplines.map(DisciplinePersistenceMapper::mapToRecord),
            bundle = PersistenceBundle(
                workItems = bundles.flatMap { it.workItems },
                checklistItems = bundles.flatMap { it.checklistItems },
                examTopics = bundles.flatMap { it.examTopics },
                attachments = bundles.flatMap { it.attachments },
                logs = bundles.flatMap { it.logs }
            )
        )
    }

    fun restore(snapshot: PersistenceSnapshot): List<Discipline> {
        return snapshot.disciplines.map { disciplineRecord ->
            val disciplineBundle = snapshot.bundle.filterByDiscipline(disciplineRecord.id)
            val workItems = WorkItemPersistenceMapper.restoreHierarchy(disciplineBundle)
            DisciplinePersistenceMapper.restore(disciplineRecord, workItems)
        }
    }

    private fun PersistenceBundle.filterByDiscipline(disciplineId: Long): PersistenceBundle {
        val itemIds = workItems
            .filter { it.disciplineId == disciplineId }
            .map { it.id }
            .toSet()

        return PersistenceBundle(
            workItems = workItems.filter { it.id in itemIds },
            checklistItems = checklistItems.filter { it.workItemId in itemIds },
            examTopics = examTopics.filter { it.workItemId in itemIds },
            attachments = attachments.filter { it.workItemId in itemIds },
            logs = logs.filter { it.workItemId in itemIds }
        )
    }
}
