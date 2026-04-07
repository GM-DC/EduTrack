package org.owlcode.edutrack.data.repository

import org.owlcode.edutrack.core.result.AppError
import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.domain.model.CalendarEvent
import org.owlcode.edutrack.domain.model.EventType
import org.owlcode.edutrack.domain.repository.CalendarRepository

class MockCalendarRepository : CalendarRepository {

    private val events = mutableListOf(
        // ── Hoy: 2026-04-05 ────────────────────────────────────────────────
        CalendarEvent("1",  "Matemáticas I",          "Sala B-302 · Prof. Martínez", "08:00", "10:00", EventType.CLASS,    "2026-04-05"),
        CalendarEvent("2",  "Examen de Física",        "Aula Magna · Prof. Ruiz",     "10:30", "12:30", EventType.EXAM,     "2026-04-05"),
        CalendarEvent("3",  "Programación Orientada",  "Lab de Cómputo 201",          "14:00", "16:00", EventType.CLASS,    "2026-04-05"),
        // ── Resto del mes ───────────────────────────────────────────────────
        CalendarEvent("4",  "Cálculo II",              "Sala A-101 · Prof. Sánchez",  "08:00", "10:00", EventType.CLASS,    "2026-04-07"),
        CalendarEvent("5",  "Tarea: Integrales",       "Entregar por plataforma",     "23:59", "23:59", EventType.TASK,     "2026-04-07"),
        CalendarEvent("6",  "Entrega Proyecto Final",  "Campus Virtual",              "23:59", "23:59", EventType.TASK,     "2026-04-10"),
        CalendarEvent("7",  "Reunión de Estudio",      "Biblioteca · Sala 3",         "16:00", "18:00", EventType.PERSONAL, "2026-04-10"),
        CalendarEvent("8",  "Física Experimental",     "Lab de Física · Piso 1",      "08:00", "10:00", EventType.CLASS,    "2026-04-13"),
        CalendarEvent("9",  "Examen de Programación",  "Lab de Cómputo 305",          "10:00", "12:00", EventType.EXAM,     "2026-04-15"),
        CalendarEvent("10", "Tarea: Algoritmos",       "Entregar por plataforma",     "23:59", "23:59", EventType.TASK,     "2026-04-18"),
        CalendarEvent("11", "Química General",         "Lab de Química · Piso 2",     "08:00", "10:00", EventType.CLASS,    "2026-04-21"),
        CalendarEvent("12", "Repaso Final",            "Sala de estudio virtual",     "19:00", "21:00", EventType.PERSONAL, "2026-04-25"),
        CalendarEvent("13", "Examen Final de Cálculo", "Aula Magna",                  "09:00", "12:00", EventType.EXAM,     "2026-04-28"),
    )

    override suspend fun getEvents(): AppResult<List<CalendarEvent>> =
        AppResult.Success(events.toList())

    override suspend fun getEventById(id: String): AppResult<CalendarEvent?> =
        AppResult.Success(events.find { it.id == id })

    override suspend fun addEvent(event: CalendarEvent): AppResult<CalendarEvent> {
        events.add(event)
        return AppResult.Success(event)
    }

    override suspend fun updateEvent(event: CalendarEvent): AppResult<CalendarEvent> {
        val idx = events.indexOfFirst { it.id == event.id }
        return if (idx >= 0) {
            events[idx] = event
            AppResult.Success(event)
        } else {
            AppResult.Error(AppError.Network("Evento no encontrado"))
        }
    }

    override suspend fun deleteEvent(id: String): AppResult<Unit> {
        events.removeAll { it.id == id }
        return AppResult.Success(Unit)
    }

    override suspend fun syncEvents(): AppResult<Unit> = AppResult.Success(Unit)
}
