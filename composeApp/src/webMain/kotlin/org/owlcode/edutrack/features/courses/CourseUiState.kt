package org.owlcode.edutrack.features.courses

import org.owlcode.edutrack.domain.model.Course

data class CourseUiState(
    val courses: List<Course> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showForm: Boolean = false,
    val editingCourse: Course? = null,     // null = modo creación
    val pendingDeleteId: String? = null    // id a borrar, muestra diálogo confirmación
)

