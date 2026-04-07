package org.owlcode.edutrack.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CourseDto(
    val id: String,
    val name: String,
    val teacher: String = "",
    val description: String = "",
    val color: String = "#4A90D9"
)

