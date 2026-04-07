package org.owlcode.edutrack.features.courses

import org.owlcode.edutrack.domain.model.Clase
import org.owlcode.edutrack.domain.model.Course
import org.owlcode.edutrack.domain.model.Examen
import org.owlcode.edutrack.domain.model.Tarea

data class CourseDetailUiState(
    val course: Course? = null,
    val clases: List<Clase> = emptyList(),
    val tareas: List<Tarea> = emptyList(),
    val examenes: List<Examen> = emptyList(),
    val selectedTab: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    // Formularios
    val showClaseForm: Boolean = false,
    val editingClase: Clase? = null,
    val showTareaForm: Boolean = false,
    val editingTarea: Tarea? = null,
    val showExamenForm: Boolean = false,
    val editingExamen: Examen? = null,
    // Borrado
    val pendingDeleteId: String? = null,
    val pendingDeleteType: String? = null     // "CLASE" | "TAREA" | "EXAMEN"
)

