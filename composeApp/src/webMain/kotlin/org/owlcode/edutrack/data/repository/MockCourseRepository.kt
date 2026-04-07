package org.owlcode.edutrack.data.repository

import org.owlcode.edutrack.core.result.AppError
import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.domain.model.Course
import org.owlcode.edutrack.domain.repository.CourseRepository

class MockCourseRepository : CourseRepository {

    private val courses = mutableListOf(
        Course("c1", "Matemáticas I",    "Prof. Martínez", "Cálculo diferencial e integral", "#4A90D9"),
        Course("c2", "Física I",          "Prof. Ruiz",     "Mecánica clásica y termodinámica", "#E05252"),
        Course("c3", "Programación",      "Prof. López",    "Algoritmos y estructuras de datos", "#7B68EE"),
        Course("c4", "Química General",   "Prof. García",   "Química inorgánica básica", "#F5A623"),
    )
    private var nextId = 5

    override suspend fun getCourses(): AppResult<List<Course>> =
        AppResult.Success(courses.toList())

    override suspend fun getCourseById(id: String): AppResult<Course?> =
        AppResult.Success(courses.find { it.id == id })

    override suspend fun addCourse(course: Course): AppResult<Course> {
        val new = course.copy(id = "c${nextId++}")
        courses.add(new)
        return AppResult.Success(new)
    }

    override suspend fun updateCourse(course: Course): AppResult<Course> {
        val idx = courses.indexOfFirst { it.id == course.id }
        return if (idx >= 0) {
            courses[idx] = course
            AppResult.Success(course)
        } else {
            AppResult.Error(AppError.Network("Curso no encontrado"))
        }
    }

    override suspend fun deleteCourse(id: String): AppResult<Unit> {
        courses.removeAll { it.id == id }
        return AppResult.Success(Unit)
    }
}

