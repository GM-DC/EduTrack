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
import org.owlcode.edutrack.domain.model.Course
import org.owlcode.edutrack.domain.repository.CourseRepository

class CourseViewModel(
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CourseUiState())
    val state: StateFlow<CourseUiState> = _state.asStateFlow()

    init { loadCourses() }

    // ── Carga ──────────────────────────────────────────────────────────────

    fun loadCourses() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = courseRepository.getCourses()) {
                is AppResult.Success -> _state.update {
                    it.copy(courses = result.data, isLoading = false, error = null)
                }
                is AppResult.Error -> _state.update {
                    it.copy(isLoading = false, error = result.error.message)
                }
            }
        }
    }

    // ── Formulario ─────────────────────────────────────────────────────────

    fun showCreateForm() =
        _state.update { it.copy(showForm = true, editingCourse = null) }

    fun showEditForm(course: Course) =
        _state.update { it.copy(showForm = true, editingCourse = course) }

    fun dismissForm() =
        _state.update { it.copy(showForm = false, editingCourse = null) }

    fun saveCourse(course: Course) {
        viewModelScope.launch {
            val isNew  = course.id.isBlank()
            val toSave = if (isNew) {
                course.copy(id = Clock.System.now().toEpochMilliseconds().toString())
            } else course

            val result = if (isNew) courseRepository.addCourse(toSave)
                         else       courseRepository.updateCourse(toSave)

            when (result) {
                is AppResult.Success -> {
                    loadCourses()
                    _state.update { it.copy(showForm = false, editingCourse = null) }
                }
                is AppResult.Error -> _state.update { it.copy(error = result.error.message) }
            }
        }
    }

    // ── Borrado ────────────────────────────────────────────────────────────

    fun requestDelete(id: String) =
        _state.update { it.copy(pendingDeleteId = id) }

    fun cancelDelete() =
        _state.update { it.copy(pendingDeleteId = null) }

    fun confirmDelete() {
        val id = _state.value.pendingDeleteId ?: return
        viewModelScope.launch {
            courseRepository.deleteCourse(id)
            loadCourses()
            _state.update { it.copy(pendingDeleteId = null) }
        }
    }
}

