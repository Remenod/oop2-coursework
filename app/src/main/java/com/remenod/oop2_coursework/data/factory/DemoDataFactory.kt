package com.remenod.oop2_coursework.data.factory

import com.remenod.oop2_coursework.domain.model.*
import java.time.LocalDateTime

object DemoDataFactory {
    fun createDemoDisciplines(): List<Discipline> {
        val oop = Discipline(
            id = 1L,
            name = "Object-Oriented Programming",
            teacherName = "Oleksandr Petrenko",
            semester = 4,
            color = 0xFF4CAF50.toInt()
        )

        val coursework = ProjectTask(
            id = 101L,
            title = "Coursework",
            description = "Development of an organizer based on an OOP model"
        ).apply {
            estimatedMinutes = 600
            addSubTask(ProgrammingTask(
                id = 201L,
                title = "Implement domain model",
                description = "Create WorkItem and Attachment hierarchy",
                commitsCount = 3,
                issuesResolved = 1,
                testsPassed = 0.8,
                estimatedMinutes = 120
            ).apply {
                addChecklistItem(ChecklistItem("Create base WorkItem", true))
                addChecklistItem(ChecklistItem("Create specialized types", false))
            })
            
            addSubTask(ReadingTask(
                id = 202L,
                title = "Read patterns materials",
                description = "Learn Composite and Template Method",
                readPages = 15,
                totalPages = 30
            ).apply { estimatedMinutes = 60 })
            
            addSubTask(SeminarTask(
                id = 203L,
                title = "Prepare presentation",
                description = "Slides about project architecture",
                topicSelected = true,
                materialsCollected = true
            ).apply { estimatedMinutes = 90 })
        }

        val exam = ExamTask(
            id = 102L,
            title = "Exam Preparation",
            description = "Review of all course topics"
        ).apply {
            deadline = LocalDateTime.now().plusDays(7)
            topics.add(ExamTopic("Encapsulation", 90))
            topics.add(ExamTopic("Polymorphism", 85))
            topics.add(ExamTopic("Inheritance", 95))
            topics.add(ExamTopic("Design Patterns", 40))
        }

        oop.addWorkItem(coursework)
        oop.addWorkItem(exam)

        val math = Discipline(
            id = 2L,
            name = "Higher Mathematics",
            teacherName = "Mariia Sydorenko",
            semester = 4,
            color = 0xFF2196F3.toInt()
        )
        
        math.addWorkItem(GenericTask(
            id = 103L,
            title = "Homework #5",
            description = "Solve integrals"
        ).apply {
            addChecklistItem(ChecklistItem("Task 1", true))
            addChecklistItem(ChecklistItem("Task 2", true))
            addChecklistItem(ChecklistItem("Task 3", false))
        })

        return listOf(oop, math)
    }
}
