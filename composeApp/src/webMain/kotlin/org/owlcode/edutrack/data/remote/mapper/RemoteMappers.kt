package org.owlcode.edutrack.data.remote.mapper

import org.owlcode.edutrack.data.remote.dto.CalendarEventDto
import org.owlcode.edutrack.data.remote.dto.CourseDto
import org.owlcode.edutrack.data.remote.dto.UserDto
import org.owlcode.edutrack.domain.model.*

fun UserDto.toDomain(): User = User(
    id    = id,
    name  = name,
    email = email,
    role  = runCatching { UserRole.valueOf(role.uppercase()) }.getOrDefault(UserRole.STUDENT)
)

fun CalendarEventDto.toDomain(): CalendarEvent = CalendarEvent(
    id          = id,
    title       = title,
    description = description,
    startTime   = startTime,
    endTime     = endTime,
    type        = runCatching { EventType.valueOf(type.uppercase()) }.getOrDefault(EventType.CLASS),
    date        = date
)

fun CalendarEvent.toDto(): CalendarEventDto = CalendarEventDto(
    id          = id,
    title       = title,
    description = description,
    startTime   = startTime,
    endTime     = endTime,
    type        = type.name,
    date        = date
)

fun CourseDto.toDomain(): Course = Course(
    id          = id,
    name        = name,
    teacher     = teacher,
    description = description,
    color       = color
)

fun Course.toDto(): CourseDto = CourseDto(
    id          = id,
    name        = name,
    teacher     = teacher,
    description = description,
    color       = color
)
