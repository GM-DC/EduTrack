package org.owlcode.edutrack.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RecurrenceRule(
    val type: RecurrenceType = RecurrenceType.NONE,
    val interval: Int = 1,                    // repetir cada N unidades
    val daysOfWeek: Set<Int> = emptySet(),    // 0=Lu … 6=Do (para WEEKLY)
    val endType: RecurrenceEndType = RecurrenceEndType.NEVER,
    val untilDate: String? = null,            // "YYYY-MM-DD"
    val occurrenceCount: Int? = null          // para AFTER_OCCURRENCES
)

