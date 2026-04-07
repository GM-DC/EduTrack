package org.owlcode.edutrack.data.repository

import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.core.result.safeApiCall
import org.owlcode.edutrack.data.local.datasource.ExamenLocalDataSource
import org.owlcode.edutrack.data.remote.dto.toDto
import org.owlcode.edutrack.domain.model.Examen
import org.owlcode.edutrack.domain.repository.ExamenRepository

class ExamenRepositoryImpl(
    private val localDataSource: ExamenLocalDataSource
) : ExamenRepository {

    override suspend fun getAllExamenes(): AppResult<List<Examen>> = safeApiCall {
        localDataSource.getAll().map { it.toDomain() }
    }

    override suspend fun getExamenesByCourse(courseId: String): AppResult<List<Examen>> = safeApiCall {
        localDataSource.getAll().filter { it.courseId == courseId }.map { it.toDomain() }
    }

    override suspend fun getExamen(id: String): AppResult<Examen?> = safeApiCall {
        localDataSource.get(id)?.toDomain()
    }

    override suspend fun addExamen(examen: Examen): AppResult<Examen> = safeApiCall {
        localDataSource.save(examen.toDto())
        examen
    }

    override suspend fun updateExamen(examen: Examen): AppResult<Examen> = safeApiCall {
        localDataSource.save(examen.toDto())
        examen
    }

    override suspend fun deleteExamen(id: String): AppResult<Unit> = safeApiCall {
        localDataSource.delete(id)
    }
}

