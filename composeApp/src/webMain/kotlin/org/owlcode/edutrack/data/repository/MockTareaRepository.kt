package org.owlcode.edutrack.data.repository

import org.owlcode.edutrack.core.result.AppError
import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.domain.model.Tarea
import org.owlcode.edutrack.domain.model.TaskPriority
import org.owlcode.edutrack.domain.repository.TareaRepository

class MockTareaRepository : TareaRepository {

    private val tareas = mutableListOf(
        // Curso c1 — Matemáticas I
        Tarea("t1", "c1", titulo = "Ejercicios Cap. 3",      descripcion = "Resolver 20 ejercicios de límites",     prioridad = TaskPriority.HIGH,   dueDate = "2026-05-02"),
        Tarea("t2", "c1", titulo = "Tarea: Integrales",      descripcion = "Series de integración por partes",      prioridad = TaskPriority.MEDIUM, dueDate = "2026-05-07"),
        // Curso c2 — Física I
        Tarea("t3", "c2", titulo = "Informe de laboratorio", descripcion = "Redactar análisis de resultados",       prioridad = TaskPriority.HIGH,   dueDate = "2026-04-30"),
        Tarea("t4", "c2", titulo = "Problemas de dinámica",  descripcion = "Resolver cap. 4 del libro de texto",    prioridad = TaskPriority.MEDIUM, dueDate = "2026-05-08"),
        // Curso c3 — Programación
        Tarea("t5", "c3", titulo = "Proyecto Final",         descripcion = "Implementar sistema en Kotlin/Compose",  prioridad = TaskPriority.HIGH,   dueDate = "2026-05-10"),
        Tarea("t6", "c3", titulo = "Tarea: Algoritmos",      descripcion = "Entregar por plataforma antes del cierre", prioridad = TaskPriority.MEDIUM, dueDate = "2026-04-30", dueTime = "23:59"),
        // Curso c4 — Química General
        Tarea("t7", "c4", titulo = "Práctica de reacciones", descripcion = "Leer cap. 5 y hacer resumen",           prioridad = TaskPriority.LOW,    dueDate = "2026-05-05"),
    )

    private var nextId = 8

    override suspend fun getAllTareas(): AppResult<List<Tarea>> =
        AppResult.Success(tareas.toList())

    override suspend fun getTareasByCourse(courseId: String): AppResult<List<Tarea>> =
        AppResult.Success(tareas.filter { it.courseId == courseId })

    override suspend fun getTarea(id: String): AppResult<Tarea?> =
        AppResult.Success(tareas.find { it.id == id })

    override suspend fun addTarea(tarea: Tarea): AppResult<Tarea> {
        val nueva = tarea.copy(id = "t${nextId++}")
        tareas.add(nueva)
        return AppResult.Success(nueva)
    }

    override suspend fun updateTarea(tarea: Tarea): AppResult<Tarea> {
        val idx = tareas.indexOfFirst { it.id == tarea.id }
        return if (idx >= 0) {
            tareas[idx] = tarea
            AppResult.Success(tarea)
        } else {
            AppResult.Error(AppError.Network("Tarea no encontrada: ${tarea.id}"))
        }
    }

    override suspend fun deleteTarea(id: String): AppResult<Unit> {
        tareas.removeAll { it.id == id }
        return AppResult.Success(Unit)
    }
}


