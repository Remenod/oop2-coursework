package com.remenod.oop2_coursework.data.persistence.local

import com.remenod.oop2_coursework.data.persistence.mapper.TaskPersistenceMapper
import com.remenod.oop2_coursework.data.persistence.model.PersistenceSnapshot
import com.remenod.oop2_coursework.domain.model.Discipline
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class LocalTaskStorage(
    private val file: File
) {
    fun load(): List<Discipline>? {
        if (!file.exists()) return null

        return runCatching {
            ObjectInputStream(file.inputStream().buffered()).use { input ->
                val snapshot = input.readObject() as PersistenceSnapshot
                TaskPersistenceMapper.restore(snapshot)
            }
        }.getOrNull()
    }

    fun save(disciplines: List<Discipline>) {
        file.parentFile?.mkdirs()
        val snapshot = TaskPersistenceMapper.decompose(disciplines)
        val tempFile = File("${file.absolutePath}.tmp")

        ObjectOutputStream(tempFile.outputStream().buffered()).use { output ->
            output.writeObject(snapshot)
        }

        if (file.exists()) {
            file.delete()
        }
        if (!tempFile.renameTo(file)) {
            tempFile.copyTo(file, overwrite = true)
            tempFile.delete()
        }
    }
}
