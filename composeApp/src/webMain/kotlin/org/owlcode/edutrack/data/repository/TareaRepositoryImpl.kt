package org.owlcode.edutrack.data.repository

import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.core.result.safeApiCall
import org.owlcode.edutrack.data.local.datasource.TareaLocalDataSource
import org.owlcode.edutrack.data.remote.dto.toDto
import org.owlcode.edutrack.domain.model.Tarea
import org.owlcode.edutrack.domain.repository.TareaRepository

class TareaRepositoryImpl(
    private val localDataSource: TareaLocalDataSource
) : TareaRepository {

    override suspend fun getAllTareas(): AppResult<List<Tarea>> = safeApiCall {
        localDataSource.getAll().map { it.toDomain() }
    }

    override suspend fun getTareasByCourse(courseId: String): AppResult<List<Tarea>> = safeApiCall {
        localDataSource.getAll().filter { it.courseId == courseId }.map { it.toDomain() }
    }

    override suspend fun getTarea(id: String): AppResult<Tarea?> = safeApiCall {
        localDataSource.get(id)?.toDomain()
    }

    override suspend fun addTarea(tarea: Tarea): AppResult<Tarea> = safeApiCall {
        localDataSource.save(tarea.toDto())
        tarea
    }

    override suspend fun updateTarea(tarea: Tarea): AppResult<Tarea> = safeApiCall {
        localDataSource.save(tarea.toDto())
        tarea
    }

    override suspend fun deleteTarea(id: String): AppResult<Unit> = safeApiCall {
        localDataSource.delete(id)
    }
}

