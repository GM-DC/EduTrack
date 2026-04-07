package org.owlcode.edutrack.domain.repository

import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.domain.model.Course

interface CourseRepository {
    suspend fun getCourses(): AppResult<List<Course>>
    suspend fun getCourseById(id: String): AppResult<Course?>
    suspend fun addCourse(course: Course): AppResult<Course>
    suspend fun updateCourse(course: Course): AppResult<Course>
    suspend fun deleteCourse(id: String): AppResult<Unit>
}

