package org.owlcode.edutrack.features.app

import kotlinx.serialization.Serializable

sealed class Route(val path: String) {
    data object Login    : Route("login")
    data object Calendar : Route("calendar")
    data object Courses  : Route("courses")
}

/** Ruta type-safe para CourseDetail (Navigation 2.9+ KMP). */
@Serializable
data class CourseDetailRoute(val courseId: String)
