package org.owlcode.edutrack.data.local.mapper

import org.owlcode.edutrack.data.remote.dto.CalendarEventDto
import org.owlcode.edutrack.data.remote.dto.CourseDto
import org.owlcode.edutrack.data.remote.dto.UserDto
import org.owlcode.edutrack.data.remote.mapper.toDomain
import org.owlcode.edutrack.domain.model.CalendarEvent
import org.owlcode.edutrack.domain.model.Course
import org.owlcode.edutrack.domain.model.User

// El esquema local es idéntico al remoto: se reutilizan los DTOs y mappers existentes.
// Extender aquí si en el futuro los modelos de persistencia local divergen del API.

fun CalendarEventDto.toLocalDomain(): CalendarEvent = toDomain()
fun UserDto.toLocalDomain(): User = toDomain()
fun CourseDto.toLocalDomain(): Course = toDomain()

