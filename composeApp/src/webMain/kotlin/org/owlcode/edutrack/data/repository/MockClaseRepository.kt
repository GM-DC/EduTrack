package org.owlcode.edutrack.data.repository

import org.owlcode.edutrack.core.result.AppError
import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.domain.model.Clase
import org.owlcode.edutrack.domain.model.ClassMode
import org.owlcode.edutrack.domain.repository.ClaseRepository

class MockClaseRepository : ClaseRepository {

    private val clases = mutableListOf(
        // Curso c1 — Matemáticas I
        Clase("cl1", "c1", titulo = "Límites y Continuidad",  startDate = "2026-04-28", startTime = "08:00", endTime = "10:00", aula = "Sala B-302",       docente = "Prof. Martínez"),
        Clase("cl2", "c1", titulo = "Derivadas",               startDate = "2026-05-05", startTime = "08:00", endTime = "10:00", aula = "Sala B-302",       docente = "Prof. Martínez"),
        Clase("cl3", "c1", titulo = "Integrales",              startDate = "2026-05-12", startTime = "08:00", endTime = "10:00", aula = "Sala B-302",       docente = "Prof. Martínez"),
        // Curso c2 — Física I
        Clase("cl4", "c2", titulo = "Movimiento Rectilíneo",   startDate = "2026-04-29", startTime = "10:00", endTime = "12:00", aula = "Aula Magna",       docente = "Prof. Ruiz"),
        Clase("cl5", "c2", titulo = "Leyes de Newton",         startDate = "2026-05-06", startTime = "10:00", endTime = "12:00", aula = "Aula Magna",       docente = "Prof. Ruiz"),
        // Curso c3 — Programación
        Clase("cl6", "c3", titulo = "Búsqueda y Ordenamiento", startDate = "2026-04-30", startTime = "14:00", endTime = "16:00", aula = "Lab Cómputo 201",  docente = "Prof. López"),
        Clase("cl7", "c3", titulo = "Recursividad",            startDate = "2026-05-07", startTime = "14:00", endTime = "16:00", aula = "Lab Cómputo 201",  docente = "Prof. López",  modalidad = ClassMode.LIVE),
        // Curso c4 — Química General
        Clase("cl8", "c4", titulo = "Enlace Covalente",        startDate = "2026-05-02", startTime = "08:00", endTime = "10:00", aula = "Lab Química P2",   docente = "Prof. García"),
    )

    private var nextId = 9

    override suspend fun getAllClases(): AppResult<List<Clase>> =
        AppResult.Success(clases.toList())

    override suspend fun getClasesByCourse(courseId: String): AppResult<List<Clase>> =
        AppResult.Success(clases.filter { it.courseId == courseId })

    override suspend fun getClase(id: String): AppResult<Clase?> =
        AppResult.Success(clases.find { it.id == id })

    override suspend fun addClase(clase: Clase): AppResult<Clase> {
        val nueva = clase.copy(id = "cl${nextId++}")
        clases.add(nueva)
        return AppResult.Success(nueva)
    }

    override suspend fun updateClase(clase: Clase): AppResult<Clase> {
        val idx = clases.indexOfFirst { it.id == clase.id }
        return if (idx >= 0) {
            clases[idx] = clase
            AppResult.Success(clase)
        } else {
            AppResult.Error(AppError.Network("Clase no encontrada: ${clase.id}"))
        }
    }

    override suspend fun deleteClase(id: String): AppResult<Unit> {
        clases.removeAll { it.id == id }
        return AppResult.Success(Unit)
    }
}

