package com.remenod.oop2_coursework.domain.interfaces

interface ProgressTrackable {
    fun getProgress(): Double
}

interface Completable {
    fun canBeCompleted(): Boolean
    fun complete()
    fun validateCompletion(): Boolean
}

interface Syncable {
    fun sync()
}

interface Submittable {
    fun submit()
}
