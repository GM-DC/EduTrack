package org.owlcode.edutrack.domain.repository

import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.domain.model.Clase

interface ClaseRepository {
    suspend fun getAllClases(): AppResult<List<Clase>>
    suspend fun getClasesByCourse(courseId: String): AppResult<List<Clase>>
    suspend fun getClase(id: String): AppResult<Clase?>
    suspend fun addClase(clase: Clase): AppResult<Clase>
    suspend fun updateClase(clase: Clase): AppResult<Clase>
    suspend fun deleteClase(id: String): AppResult<Unit>
}

