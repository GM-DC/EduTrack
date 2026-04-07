package org.owlcode.edutrack.features.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.domain.model.Clase
import org.owlcode.edutrack.domain.model.Examen
import org.owlcode.edutrack.domain.model.Tarea
import org.owlcode.edutrack.domain.repository.ClaseRepository
import org.owlcode.edutrack.domain.repository.CourseRepository
import org.owlcode.edutrack.domain.repository.ExamenRepository
import org.owlcode.edutrack.domain.repository.TareaRepository

class CourseDetailViewModel(
    private val courseId: String,
    private val courseRepository: CourseRepository,
    private val claseRepository: ClaseRepository,
    private val tareaRepository: TareaRepository,
    private val examenRepository: ExamenRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CourseDetailUiState())
    val state: StateFlow<CourseDetailUiState> = _state.asStateFlow()

    init { loadAll() }

    // ── Carga ───────────────────────────────────────────────────────────────

    fun loadAll() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val course = when (val r = courseRepository.getCourseById(courseId)) {
                is AppResult.Success -> r.data
                is AppResult.Error   -> null
            }
            val clases = when (val r = claseRepository.getClasesByCourse(courseId)) {
                is AppResult.Success -> r.data
                is AppResult.Error   -> emptyList()
            }
            val tareas = when (val r = tareaRepository.getTareasByCourse(courseId)) {
                is AppResult.Success -> r.data
                is AppResult.Error   -> emptyList()
            }
            val examenes = when (val r = examenRepository.getExamenesByCourse(courseId)) {
                is AppResult.Success -> r.data
                is AppResult.Error   -> emptyList()
            }
            _state.update { it.copy(course = course, clases = clases, tareas = tareas, examenes = examenes, isLoading = false) }
        }
    }

    fun selectTab(idx: Int) = _state.update { it.copy(selectedTab = idx) }

    // ── Clase CRUD ──────────────────────────────────────────────────────────

    fun showCreateClaseForm() = _state.update { it.copy(showClaseForm = true, editingClase = null) }
    fun showEditClaseForm(clase: Clase) = _state.update { it.copy(showClaseForm = true, editingClase = clase) }
    fun dismissClaseForm() = _state.update { it.copy(showClaseForm = false, editingClase = null) }

    fun saveClase(clase: Clase) {
        viewModelScope.launch {
            val isNew  = clase.id.isBlank()
            val toSave = if (isNew) clase.copy(id = Clock.System.now().toEpochMilliseconds().toString(), courseId = courseId) else clase
            val result = if (isNew) claseRepository.addClase(toSave) else claseRepository.updateClase(toSave)
            when (result) {
                is AppResult.Success -> { loadAll(); dismissClaseForm() }
                is AppResult.Error   -> _state.update { it.copy(error = result.error.message) }
            }
        }
    }

    // ── Tarea CRUD ──────────────────────────────────────────────────────────

    fun showCreateTareaForm() = _state.update { it.copy(showTareaForm = true, editingTarea = null) }
    fun showEditTareaForm(tarea: Tarea) = _state.update { it.copy(showTareaForm = true, editingTarea = tarea) }
    fun dismissTareaForm() = _state.update { it.copy(showTareaForm = false, editingTarea = null) }

    fun saveTarea(tarea: Tarea) {
        viewModelScope.launch {
            val isNew  = tarea.id.isBlank()
            val toSave = if (isNew) tarea.copy(id = Clock.System.now().toEpochMilliseconds().toString(), courseId = courseId) else tarea
            val result = if (isNew) tareaRepository.addTarea(toSave) else tareaRepository.updateTarea(toSave)
            when (result) {
                is AppResult.Success -> { loadAll(); dismissTareaForm() }
                is AppResult.Error   -> _state.update { it.copy(error = result.error.message) }
            }
        }
    }

    // ── Examen CRUD ─────────────────────────────────────────────────────────

    fun showCreateExamenForm() = _state.update { it.copy(showExamenForm = true, editingExamen = null) }
    fun showEditExamenForm(examen: Examen) = _state.update { it.copy(showExamenForm = true, editingExamen = examen) }
    fun dismissExamenForm() = _state.update { it.copy(showExamenForm = false, editingExamen = null) }

    fun saveExamen(examen: Examen) {
        viewModelScope.launch {
            val isNew  = examen.id.isBlank()
            val toSave = if (isNew) examen.copy(id = Clock.System.now().toEpochMilliseconds().toString(), courseId = courseId) else examen
            val result = if (isNew) examenRepository.addExamen(toSave) else examenRepository.updateExamen(toSave)
            when (result) {
                is AppResult.Success -> { loadAll(); dismissExamenForm() }
                is AppResult.Error   -> _state.update { it.copy(error = result.error.message) }
            }
        }
    }

    // ── Borrado ─────────────────────────────────────────────────────────────

    fun requestDelete(id: String, type: String) = _state.update { it.copy(pendingDeleteId = id, pendingDeleteType = type) }
    fun cancelDelete() = _state.update { it.copy(pendingDeleteId = null, pendingDeleteType = null) }

    fun confirmDelete() {
        val id   = _state.value.pendingDeleteId   ?: return
        val type = _state.value.pendingDeleteType ?: return
        viewModelScope.launch {
            when (type) {
                "CLASE"  -> claseRepository.deleteClase(id)
                "TAREA"  -> tareaRepository.deleteTarea(id)
                "EXAMEN" -> examenRepository.deleteExamen(id)
            }
            loadAll()
            cancelDelete()
        }
    }
}



