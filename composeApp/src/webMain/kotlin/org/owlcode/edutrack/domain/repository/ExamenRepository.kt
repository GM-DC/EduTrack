package org.owlcode.edutrack.domain.repository

import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.domain.model.Examen

interface ExamenRepository {
    suspend fun getAllExamenes(): AppResult<List<Examen>>
    suspend fun getExamenesByCourse(courseId: String): AppResult<List<Examen>>
    suspend fun getExamen(id: String): AppResult<Examen?>
    suspend fun addExamen(examen: Examen): AppResult<Examen>
    suspend fun updateExamen(examen: Examen): AppResult<Examen>
    suspend fun deleteExamen(id: String): AppResult<Unit>
}

