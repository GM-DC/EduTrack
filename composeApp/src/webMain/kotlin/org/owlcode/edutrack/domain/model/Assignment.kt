package org.owlcode.edutrack.domain.model

data class Assignment(
    val id: String,
    val title: String,
    val description: String,
    val dueDate: String,    // ISO-8601
    val status: AssignmentStatus
)

enum class AssignmentStatus { PENDING, IN_PROGRESS, SUBMITTED, GRADED }

