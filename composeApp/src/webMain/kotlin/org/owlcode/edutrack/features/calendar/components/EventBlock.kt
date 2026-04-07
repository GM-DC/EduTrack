package org.owlcode.edutrack.features.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.owlcode.edutrack.domain.model.CalendarItem
import org.owlcode.edutrack.domain.model.EventType
import org.owlcode.edutrack.features.calendar.utils.toMinutes

// HOUR_HEIGHT_DP está definido en TimeColumn.kt — no se redefine aquí

/** Convierte un color hex a Color Compose. Reutilizable en todo el módulo de calendario. */
fun String?.toItemColor(type: EventType): Color = when {
    this != null -> runCatching {
        val hex   = removePrefix("#")
        val value = hex.toLong(16)
        val argb  = if (hex.length == 6) 0xFF000000L or value else value
        Color(argb.toInt())
    }.getOrDefault(type.defaultColor())
    else -> type.defaultColor()
}

fun EventType.defaultColor(): Color = when (this) {
    EventType.CLASS    -> Color(0xFF4A90D9)
    EventType.EXAM     -> Color(0xFFE05252)
    EventType.TASK     -> Color(0xFFF5A623)
    EventType.PERSONAL -> Color(0xFF7B68EE)
}

/**
 * Bloque de evento posicionado absolutamente dentro de un [Box] cuya altura es
 * `(endHour - startHour + 1) * HOUR_HEIGHT_DP.dp`.
 * Solo para items con hora (isAllDay = false).
 */
@Composable
fun EventBlock(
    item: CalendarItem,
    startHour: Int = 7,
    modifier: Modifier = Modifier
) {
    val color = item.courseColor.toItemColor(item.type)

    val startMin = toMinutes(item.startTime ?: "${startHour}:00")
    val rawEnd   = toMinutes(item.endTime ?: "")
    val endMin   = if (rawEnd <= startMin) startMin + 30 else rawEnd

    val offsetDp = ((startMin - startHour * 60) / 60f * HOUR_HEIGHT_DP).dp
    val heightDp = ((endMin - startMin) / 60f * HOUR_HEIGHT_DP).dp.coerceAtLeast(28.dp)

    Box(
        modifier = modifier
            .offset(y = offsetDp)
            .height(heightDp)
            .fillMaxWidth()
            .padding(horizontal = 2.dp)
            .background(color.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
    ) {
        Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(4.dp))
            Column {
                Text(
                    text       = item.title,
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = color,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                if (item.startTime != null) {
                    Text(
                        text     = "${item.startTime}–${item.endTime ?: ""}",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = color.copy(alpha = 0.8f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}
