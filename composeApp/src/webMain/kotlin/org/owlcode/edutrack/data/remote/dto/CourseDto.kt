package org.owlcode.edutrack.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CourseDto(
    val id: Long = 0L,
    val name: String,
    val teacher: String = "",
    val description: String = "",
    val color: String = "#4A90D9",
    val locationOrPlatform: String = "",
    val credits: Int? = null
)
