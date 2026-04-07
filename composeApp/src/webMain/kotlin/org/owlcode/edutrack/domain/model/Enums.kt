package org.owlcode.edutrack.domain.model

enum class RecurrenceType { NONE, DAILY, WEEKLY, MONTHLY }

enum class RecurrenceEndType { NEVER, UNTIL_DATE, AFTER_OCCURRENCES }

enum class ClassMode { PRESENTIAL, LIVE, SELF_PACED }

enum class TaskPriority { LOW, MEDIUM, HIGH }

enum class TaskStatus { PENDING, IN_PROGRESS, SUBMITTED, OVERDUE }

enum class ExamStatus { PENDING, TAKEN, RESCHEDULED, CANCELLED }

