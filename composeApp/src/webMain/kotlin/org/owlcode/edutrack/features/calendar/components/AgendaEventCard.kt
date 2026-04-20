package org.owlcode.edutrack.features.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.owlcode.edutrack.domain.model.CalendarItem
import org.owlcode.edutrack.domain.model.EventType

@Composable
fun AgendaEventCard(
    item: CalendarItem,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val typeColor = item.courseColor.toItemColor(item.type)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier              = Modifier.padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Bloque de horario ──────────────────────────────────────────
            Column(
                modifier            = Modifier.width(52.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (item.isAllDay) {
                    Text(
                        text  = if (item.isDeadline) "📋" else "Todo el día",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    item.startTime?.let {
                        Text(text = it, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    }
                    item.endTime?.let {
                        Text(text = it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // ── Barra de color lateral ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(typeColor)
            )

            // ── Título + curso + estado ────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                // Nombre del curso (solo para Tarea y Examen)
                if (item.type != EventType.PERSONAL) {
                    item.courseName?.let {
                        Text(
                            text  = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = typeColor
                        )
                    }
                }
                Text(text = item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                item.status?.let {
                    Text(
                        text  = it.toStatusLabel(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Badge de tipo ──────────────────────────────────────────────
            Surface(shape = RoundedCornerShape(8.dp), color = typeColor.copy(alpha = 0.15f)) {
                Text(
                    text     = item.type.label(),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = typeColor
                )
            }

            // ── Acciones ──────────────────────────────────────────────────
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Text("✏", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Text("✕", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private fun EventType.label(): String = when (this) {
    EventType.CLASS    -> "Clase"
    EventType.EXAM     -> "Examen"
    EventType.TASK     -> "Tarea"
    EventType.PERSONAL -> "Personal"
}

private fun String.toStatusLabel(): String = when (this.uppercase()) {
    // TaskStatus
    "PENDING"     -> "Pendiente"
    "IN_PROGRESS" -> "En progreso"
    "SUBMITTED"   -> "Entregada"
    "OVERDUE"     -> "Vencida"
    // ExamStatus
    "TAKEN"       -> "Rendido"
    "RESCHEDULED" -> "Reprogramado"
    "CANCELLED"   -> "Cancelado"
    else          -> this
}

