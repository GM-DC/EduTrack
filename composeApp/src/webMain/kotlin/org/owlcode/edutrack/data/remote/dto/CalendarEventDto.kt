package org.owlcode.edutrack.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CalendarEventDto(
    val id: String,
    val title: String,
    val description: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val type: String = "CLASS",
    val date: String
)

