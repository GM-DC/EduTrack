package org.owlcode.edutrack.data.repository

import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.core.result.safeApiCall
import org.owlcode.edutrack.data.local.datasource.AuthLocalDataSource
import org.owlcode.edutrack.data.local.datasource.ExamenLocalDataSource
import org.owlcode.edutrack.data.remote.api.ExamenApiService
import org.owlcode.edutrack.data.remote.dto.toDto
import org.owlcode.edutrack.domain.model.Examen
import org.owlcode.edutrack.domain.repository.ExamenRepository

class ExamenRepositoryImpl(
    private val remote: ExamenApiService,
    private val local: ExamenLocalDataSource,
    private val authLocal: AuthLocalDataSource
) : ExamenRepository {

    override suspend fun getAllExamenes(): AppResult<List<Examen>> = safeApiCall {
        local.getAll().map { it.toDomain() }
    }

    override suspend fun getExamenesByCourse(courseId: String): AppResult<List<Examen>> = safeApiCall {
        val token = authLocal.getToken() ?: error("No autenticado")
        val dtos = remote.getExamenesByCourse(token, courseId.toLong())
        dtos.forEach { local.save(it) }
        dtos.map { it.toDomain() }
    }

    override suspend fun getExamen(id: String): AppResult<Examen?> = safeApiCall {
        local.get(id)?.toDomain()
    }

    override suspend fun addExamen(examen: Examen): AppResult<Examen> = safeApiCall {
        val token = authLocal.getToken() ?: error("No autenticado")
        val dto = remote.addExamen(token, examen.toDto())
        local.save(dto)
        dto.toDomain()
    }

    override suspend fun updateExamen(examen: Examen): AppResult<Examen> = safeApiCall {
        val token = authLocal.getToken() ?: error("No autenticado")
        val dto = remote.updateExamen(token, examen.toDto())
        local.save(dto)
        dto.toDomain()
    }

    override suspend fun deleteExamen(id: String): AppResult<Unit> = safeApiCall {
        val token = authLocal.getToken() ?: error("No autenticado")
        remote.deleteExamen(token, id.toLong())
        local.delete(id)
    }
}
