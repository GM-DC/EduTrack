package org.owlcode.edutrack.data.repository

import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.core.result.safeApiCall
import org.owlcode.edutrack.data.local.datasource.AuthLocalDataSource
import org.owlcode.edutrack.data.local.datasource.CourseLocalDataSource
import org.owlcode.edutrack.data.local.mapper.toLocalDomain
import org.owlcode.edutrack.data.remote.api.CourseApiService
import org.owlcode.edutrack.data.remote.mapper.toDomain
import org.owlcode.edutrack.data.remote.mapper.toDto
import org.owlcode.edutrack.domain.model.Course
import org.owlcode.edutrack.domain.repository.CourseRepository

class CourseRepositoryImpl(
    private val remote: CourseApiService,
    private val local: CourseLocalDataSource,
    private val authLocal: AuthLocalDataSource
) : CourseRepository {

    // offline-first: devuelve datos locales inmediatamente
    override suspend fun getCourses(): AppResult<List<Course>> = safeApiCall {
        local.getAllCourses().map { it.toLocalDomain() }
    }

    override suspend fun getCourseById(id: String): AppResult<Course?> = safeApiCall {
        local.getCourse(id)?.toLocalDomain()
    }

    override suspend fun addCourse(course: Course): AppResult<Course> = safeApiCall {
        val token = authLocal.getToken() ?: error("No autenticado")
        val dto = remote.addCourse(token, course.toDto())
        local.saveCourse(dto)
        dto.toDomain()
    }

    override suspend fun updateCourse(course: Course): AppResult<Course> = safeApiCall {
        val token = authLocal.getToken() ?: error("No autenticado")
        val dto = remote.updateCourse(token, course.toDto())
        local.saveCourse(dto)
        dto.toDomain()
    }

    override suspend fun deleteCourse(id: String): AppResult<Unit> = safeApiCall {
        val token = authLocal.getToken() ?: error("No autenticado")
        remote.deleteCourse(token, id)
        local.deleteCourse(id)
    }
}

