package org.owlcode.edutrack.data.repository

import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.core.result.safeApiCall
import org.owlcode.edutrack.data.local.datasource.AuthLocalDataSource
import org.owlcode.edutrack.data.local.datasource.ClaseLocalDataSource
import org.owlcode.edutrack.data.remote.api.ClaseApiService
import org.owlcode.edutrack.data.remote.dto.toDto
import org.owlcode.edutrack.domain.model.Clase
import org.owlcode.edutrack.domain.repository.ClaseRepository

class ClaseRepositoryImpl(
    private val remote: ClaseApiService,
    private val local: ClaseLocalDataSource,
    private val authLocal: AuthLocalDataSource
) : ClaseRepository {

    override suspend fun getAllClases(): AppResult<List<Clase>> = safeApiCall {
        local.getAll().map { it.toDomain() }
    }

    override suspend fun getClasesByCourse(courseId: String): AppResult<List<Clase>> = safeApiCall {
        val token = authLocal.getToken() ?: error("No autenticado")
        val dtos = remote.getClasesByCourse(token, courseId.toLong())
        dtos.forEach { local.save(it) }
        dtos.map { it.toDomain() }
    }

    override suspend fun getClase(id: String): AppResult<Clase?> = safeApiCall {
        local.get(id)?.toDomain()
    }

    override suspend fun addClase(clase: Clase): AppResult<Clase> = safeApiCall {
        val token = authLocal.getToken() ?: error("No autenticado")
        val dto = remote.addClase(token, clase.toDto())
        local.save(dto)
        dto.toDomain()
    }

    override suspend fun updateClase(clase: Clase): AppResult<Clase> = safeApiCall {
        val token = authLocal.getToken() ?: error("No autenticado")
        val dto = remote.updateClase(token, clase.toDto())
        local.save(dto)
        dto.toDomain()
    }

    override suspend fun deleteClase(id: String): AppResult<Unit> = safeApiCall {
        val token = authLocal.getToken() ?: error("No autenticado")
        remote.deleteClase(token, id.toLong())
        local.delete(id)
    }
}
