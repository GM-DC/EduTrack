package org.owlcode.edutrack.data.repository

import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.core.result.safeApiCall
import org.owlcode.edutrack.data.local.datasource.ClaseLocalDataSource
import org.owlcode.edutrack.data.remote.dto.toDto
import org.owlcode.edutrack.domain.model.Clase
import org.owlcode.edutrack.domain.repository.ClaseRepository

class ClaseRepositoryImpl(
    private val localDataSource: ClaseLocalDataSource
) : ClaseRepository {

    override suspend fun getAllClases(): AppResult<List<Clase>> = safeApiCall {
        localDataSource.getAll().map { it.toDomain() }
    }

    override suspend fun getClasesByCourse(courseId: String): AppResult<List<Clase>> = safeApiCall {
        localDataSource.getAll().filter { it.courseId == courseId }.map { it.toDomain() }
    }

    override suspend fun getClase(id: String): AppResult<Clase?> = safeApiCall {
        localDataSource.get(id)?.toDomain()
    }

    override suspend fun addClase(clase: Clase): AppResult<Clase> = safeApiCall {
        localDataSource.save(clase.toDto())
        clase
    }

    override suspend fun updateClase(clase: Clase): AppResult<Clase> = safeApiCall {
        localDataSource.save(clase.toDto())
        clase
    }

    override suspend fun deleteClase(id: String): AppResult<Unit> = safeApiCall {
        localDataSource.delete(id)
    }
}

