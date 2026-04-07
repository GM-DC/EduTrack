package org.owlcode.edutrack.features.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import org.owlcode.edutrack.domain.model.CalendarItem
import org.owlcode.edutrack.domain.model.EventType

private val MONTH_NAMES = listOf(
    "Enero","Febrero","Marzo","Abril","Mayo","Junio",
    "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"
)
private val WEEK_DAYS = listOf("Lu", "Ma", "Mi", "Ju", "Vi", "Sa", "Do")

private fun startOffsetFor(year: Int, month: Int): Int =
    LocalDate(year, month, 1).dayOfWeek.ordinal  // 0=Lun, 6=Dom

private fun daysInMonth(year: Int, month: Int): Int {
    val nextFirst = if (month == 12) LocalDate(year + 1, 1, 1)
                    else LocalDate(year, month + 1, 1)
    return nextFirst.minus(1, DateTimeUnit.DAY).day
}

@Composable
fun MonthCalendar(
    year: Int,
    month: Int,
    selectedDateStr: String,
    itemsByDate: Map<String, List<CalendarItem>> = emptyMap(),
    onDaySelected: (String) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    val startOffset: Int = startOffsetFor(year, month)
    val totalDays: Int   = daysInMonth(year, month)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp)) {

            // ── Navegación de mes ──────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onPrevMonth) {
                    Text("‹", style = MaterialTheme.typography.headlineSmall)
                }
                Text(
                    text = "${MONTH_NAMES[month - 1]} $year",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onNextMonth) {
                    Text("›", style = MaterialTheme.typography.headlineSmall)
                }
            }

            // ── Cabecera de días de semana ─────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth()) {
                WEEK_DAYS.forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Grilla de días ─────────────────────────────────────────────
            val totalCells: Int = startOffset + totalDays
            val rows: Int       = (totalCells + 6) / 7

            repeat(rows) { row ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(7) { col ->
                        val idx     = row * 7 + col
                        val day: Int? = if (idx in startOffset until startOffset + totalDays)
                                            idx - startOffset + 1 else null
                        val dateStr: String? = day?.let {
                            "$year-${month.toString().padStart(2,'0')}-${it.toString().padStart(2,'0')}"
                        }

                        DayCell(
                            day        = day,
                            isToday    = day != null && LocalDate(year, month, day) == today,
                            isSelected = dateStr != null && dateStr == selectedDateStr,
                            dayItems   = if (dateStr != null) itemsByDate[dateStr].orEmpty() else emptyList(),
                            modifier   = Modifier.weight(1f),
                            onClick    = { dateStr?.let(onDaySelected) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int?,
    isToday: Boolean,
    isSelected: Boolean,
    dayItems: List<CalendarItem>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .then(if (day != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (day != null) {
            val bgColor = when {
                isSelected -> MaterialTheme.colorScheme.primary
                isToday    -> MaterialTheme.colorScheme.primaryContainer
                else       -> null
            }
            bgColor?.let { Box(Modifier.size(34.dp).background(it, CircleShape)) }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text       = day.toString(),
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                    color      = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        isToday    -> MaterialTheme.colorScheme.primary
                        else       -> MaterialTheme.colorScheme.onSurface
                    }
                )
                // Hasta 3 puntos diferenciados por tipo+color
                if (dayItems.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        dayItems.take(3).forEach { item ->
                            val dotColor = if (isSelected)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                item.courseColor.toItemColor(item.type)
                            val isDeadline = item.isDeadline
                            Box(
                                modifier = Modifier
                                    .size(if (item.type == EventType.EXAM) 5.dp else 4.dp)
                                    .background(
                                        color = dotColor,
                                        shape = if (isDeadline) androidx.compose.foundation.shape.RoundedCornerShape(1.dp) else CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
