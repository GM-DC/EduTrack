package org.owlcode.edutrack.domain.repository

import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.domain.model.Tarea

interface TareaRepository {
    suspend fun getAllTareas(): AppResult<List<Tarea>>
    suspend fun getTareasByCourse(courseId: String): AppResult<List<Tarea>>
    suspend fun getTarea(id: String): AppResult<Tarea?>
    suspend fun addTarea(tarea: Tarea): AppResult<Tarea>
    suspend fun updateTarea(tarea: Tarea): AppResult<Tarea>
    suspend fun deleteTarea(id: String): AppResult<Unit>
}

