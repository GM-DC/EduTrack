package org.owlcode.edutrack.data.local.datasource

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.owlcode.edutrack.core.database.StorageDriver
import org.owlcode.edutrack.data.remote.dto.CourseDto

class CourseLocalDataSource(private val driver: StorageDriver) {

    private companion object {
        const val STORE = "courses"
    }

    suspend fun saveCourse(course: CourseDto) =
        driver.put(STORE, course.id, Json.encodeToString(course))

    suspend fun getCourse(id: String): CourseDto? =
        driver.get(STORE, id)?.let { Json.decodeFromString(it) }

    suspend fun getAllCourses(): List<CourseDto> =
        driver.getAll(STORE).mapNotNull {
            runCatching { Json.decodeFromString<CourseDto>(it) }.getOrNull()
        }

    suspend fun deleteCourse(id: String) = driver.delete(STORE, id)

    suspend fun saveAllCourses(courses: List<CourseDto>) {
        driver.clear(STORE)
        courses.forEach { saveCourse(it) }
    }
}

