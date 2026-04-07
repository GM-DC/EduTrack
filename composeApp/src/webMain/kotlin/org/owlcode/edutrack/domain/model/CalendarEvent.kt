package org.owlcode.edutrack.domain.model

data class CalendarEvent(
    val id: String,
    val title: String,
    val description: String = "",
    val startTime: String = "",   // "HH:mm"
    val endTime: String = "",     // "HH:mm"
    val type: EventType,
    val date: String              // "YYYY-MM-DD"
)

enum class EventType { CLASS, EXAM, TASK, PERSONAL }
