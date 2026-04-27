package org.owlcode.edutrack.data.repository

import org.owlcode.edutrack.core.result.AppError
import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.domain.model.Examen
import org.owlcode.edutrack.domain.model.ExamStatus
import org.owlcode.edutrack.domain.repository.ExamenRepository

class MockExamenRepository : ExamenRepository {

    private val examenes = mutableListOf(
        // Curso c1 — Matemáticas I
        Examen("e1", "c1", titulo = "Parcial 1 — Cálculo",    tema = "Límites y Continuidad",  fecha = "2026-05-10", horaInicio = "08:00", horaFin = "10:00"),
        Examen("e2", "c1", titulo = "Parcial 2 — Derivadas",  tema = "Derivadas e Integrales",  fecha = "2026-06-07", horaInicio = "08:00", horaFin = "10:00"),
        // Curso c2 — Física I
        Examen("e3", "c2", titulo = "Examen de Física",       tema = "Mov. Rectilíneo y Leyes", fecha = "2026-04-28", horaInicio = "10:30", horaFin = "12:30", estado = ExamStatus.TAKEN),
        Examen("e4", "c2", titulo = "Parcial II — Física",    tema = "Termodinámica",            fecha = "2026-06-02", horaInicio = "10:00"),
        // Curso c3 — Programación
        Examen("e5", "c3", titulo = "Examen Programación",    tema = "Algoritmos y Estructuras", fecha = "2026-05-15", horaInicio = "10:00", horaFin = "12:00"),
        // Curso c4 — Química General
        Examen("e6", "c4", titulo = "Parcial Química",        tema = "Enlace Químico",           fecha = "2026-05-12", horaInicio = "09:00", horaFin = "11:00"),
    )

    private var nextId = 7

    override suspend fun getAllExamenes(): AppResult<List<Examen>> =
        AppResult.Success(examenes.toList())

    override suspend fun getExamenesByCourse(courseId: String): AppResult<List<Examen>> =
        AppResult.Success(examenes.filter { it.courseId == courseId })

    override suspend fun getExamen(id: String): AppResult<Examen?> =
        AppResult.Success(examenes.find { it.id == id })

    override suspend fun addExamen(examen: Examen): AppResult<Examen> {
        val nuevo = examen.copy(id = "e${nextId++}")
        examenes.add(nuevo)
        return AppResult.Success(nuevo)
    }

    override suspend fun updateExamen(examen: Examen): AppResult<Examen> {
        val idx = examenes.indexOfFirst { it.id == examen.id }
        return if (idx >= 0) {
            examenes[idx] = examen
            AppResult.Success(examen)
        } else {
            AppResult.Error(AppError.Network("Examen no encontrado: ${examen.id}"))
        }
    }

    override suspend fun deleteExamen(id: String): AppResult<Unit> {
        examenes.removeAll { it.id == id }
        return AppResult.Success(Unit)
    }
}

