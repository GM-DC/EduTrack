package org.owlcode.edutrack.data.repository

import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.core.result.safeApiCall
import org.owlcode.edutrack.data.local.datasource.AuthLocalDataSource
import org.owlcode.edutrack.data.local.datasource.TareaLocalDataSource
import org.owlcode.edutrack.data.remote.api.TareaApiService
import org.owlcode.edutrack.data.remote.dto.toDto
import org.owlcode.edutrack.domain.model.Tarea
import org.owlcode.edutrack.domain.repository.TareaRepository

class TareaRepositoryImpl(
    private val remote: TareaApiService,
    private val local: TareaLocalDataSource,
    private val authLocal: AuthLocalDataSource
) : TareaRepository {

    override suspend fun getAllTareas(): AppResult<List<Tarea>> = safeApiCall {
        local.getAll().map { it.toDomain() }
    }

    override suspend fun getTareasByCourse(courseId: String): AppResult<List<Tarea>> = safeApiCall {
        val token = authLocal.getToken() ?: error("No autenticado")
        val dtos = remote.getTareasByCourse(token, courseId.toLong())
        dtos.forEach { local.save(it) }
        dtos.map { it.toDomain() }
    }

    override suspend fun getTarea(id: String): AppResult<Tarea?> = safeApiCall {
        local.get(id)?.toDomain()
    }

    override suspend fun addTarea(tarea: Tarea): AppResult<Tarea> = safeApiCall {
        val token = authLocal.getToken() ?: error("No autenticado")
        val dto = remote.addTarea(token, tarea.toDto())
        local.save(dto)
        dto.toDomain()
    }

    override suspend fun updateTarea(tarea: Tarea): AppResult<Tarea> = safeApiCall {
        val token = authLocal.getToken() ?: error("No autenticado")
        val dto = remote.updateTarea(token, tarea.toDto())
        local.save(dto)
        dto.toDomain()
    }

    override suspend fun deleteTarea(id: String): AppResult<Unit> = safeApiCall {
        val token = authLocal.getToken() ?: error("No autenticado")
        remote.deleteTarea(token, id.toLong())
        local.delete(id)
    }
}
