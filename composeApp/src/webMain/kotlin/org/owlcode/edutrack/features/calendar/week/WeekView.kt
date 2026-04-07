package org.owlcode.edutrack.features.calendar.week

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import org.owlcode.edutrack.domain.model.CalendarItem
import org.owlcode.edutrack.features.calendar.AgendaUiState
import org.owlcode.edutrack.features.calendar.CalendarViewMode
import org.owlcode.edutrack.features.calendar.CalendarViewModel
import org.owlcode.edutrack.features.calendar.components.EventBlock
import org.owlcode.edutrack.features.calendar.components.HOUR_HEIGHT_DP
import org.owlcode.edutrack.features.calendar.components.TimeColumn
import org.owlcode.edutrack.features.calendar.components.defaultColor
import org.owlcode.edutrack.features.calendar.components.toItemColor
import org.owlcode.edutrack.features.calendar.utils.getWeekDays

private const val START_HOUR  = 7
private const val END_HOUR    = 22
private const val TOTAL_HOURS = END_HOUR - START_HOUR + 1

private val MONTH_SHORT = listOf("", "ene","feb","mar","abr","may","jun","jul","ago","sep","oct","nov","dic")
private val DAY_SHORT   = listOf("Lu","Ma","Mi","Ju","Vi","Sa","Do")

@Composable
fun WeekView(state: AgendaUiState, viewModel: CalendarViewModel) {
    val today       = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    val weekStart   = remember(state.visibleWeekStart) { LocalDate.parse(state.visibleWeekStart) }
    val weekDays    = remember(weekStart) { getWeekDays(weekStart) }
    val scrollState = rememberScrollState()
    val density     = LocalDensity.current

    LaunchedEffect(Unit) {
        val now   = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val minOff = now.hour * 60 + now.minute
        val offsetPx = with(density) { ((minOff - START_HOUR * 60) / 60f * HOUR_HEIGHT_DP).dp.toPx().toInt() }.coerceAtLeast(0)
        scrollState.animateScrollTo(offsetPx)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        WeekHeader(weekDays = weekDays, onPrev = viewModel::prevPeriod, onNext = viewModel::nextPeriod)
        WeekDaysRow(
            weekDays        = weekDays,
            selectedDateStr = state.selectedDateStr,
            eventDates      = state.datesWithEvents,
            today           = today,
            onDaySelected   = { dateStr -> viewModel.selectDate(dateStr); viewModel.changeMode(CalendarViewMode.DAY) }
        )
        HorizontalDivider()

        // ── Zona superior: all-day / deadlines ────────────────────────
        val allDayByDay = weekDays.associateWith { day ->
            state.eventsByDate[day.toString()].orEmpty().filter { it.isAllDay || it.isDeadline }
        }
        val hasAllDay = allDayByDay.values.any { it.isNotEmpty() }
        if (hasAllDay) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 36.dp, max = 72.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Spacer(Modifier.width(52.dp))
                weekDays.forEach { day ->
                    val items = allDayByDay[day].orEmpty()
                    Column(
                        modifier = Modifier.weight(1f).padding(horizontal = 2.dp, vertical = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items.take(2).forEach { item ->
                            val color = item.courseColor.toItemColor(item.type)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = color.copy(alpha = 0.20f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text     = item.title,
                                    style    = MaterialTheme.typography.labelSmall,
                                    color    = color,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
            HorizontalDivider()
        }

        // ── Grid scrolleable (timeline) ───────────────────────────────
        Row(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
            TimeColumn(startHour = START_HOUR, endHour = END_HOUR)
            weekDays.forEach { day ->
                val dateStr   = day.toString()
                val timedItems = state.eventsByDate[dateStr].orEmpty().filter { !it.isAllDay && !it.isDeadline }
                val isToday    = day == today
                val isSelected = dateStr == state.selectedDateStr

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height((TOTAL_HOURS * HOUR_HEIGHT_DP).dp)
                        .background(when {
                            isToday    -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.10f)
                            isSelected -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.12f)
                            else       -> MaterialTheme.colorScheme.surface
                        })
                ) {
                    repeat(TOTAL_HOURS) { idx ->
                        HorizontalDivider(
                            modifier = Modifier.offset(y = (idx * HOUR_HEIGHT_DP).dp),
                            color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                    timedItems.forEach { item -> EventBlock(item = item, startHour = START_HOUR) }
                    if (isToday) {
                        val nowMin = remember {
                            val n = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                            n.hour * 60 + n.minute
                        }
                        Box(
                            modifier = Modifier
                                .offset(y = ((nowMin - START_HOUR * 60) / 60f * HOUR_HEIGHT_DP).dp)
                                .fillMaxWidth().height(2.dp)
                                .background(MaterialTheme.colorScheme.error)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekHeader(weekDays: List<LocalDate>, onPrev: () -> Unit, onNext: () -> Unit) {
    val first    = weekDays.first(); val last = weekDays.last()
    val firstIdx = first.month.ordinal + 1; val lastIdx = last.month.ordinal + 1
    val rangeText = if (firstIdx == lastIdx) "${first.day}–${last.day} de ${MONTH_SHORT[firstIdx]} ${first.year}"
                    else "${first.day} ${MONTH_SHORT[firstIdx]} – ${last.day} ${MONTH_SHORT[lastIdx]} ${last.year}"
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onPrev) { Text("‹", style = MaterialTheme.typography.headlineSmall) }
        Text(rangeText, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        TextButton(onClick = onNext) { Text("›", style = MaterialTheme.typography.headlineSmall) }
    }
}

@Composable
private fun WeekDaysRow(
    weekDays: List<LocalDate>,
    selectedDateStr: String,
    eventDates: Set<String>,
    today: LocalDate,
    onDaySelected: (String) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.width(52.dp))
        weekDays.forEachIndexed { idx, day ->
            val dateStr    = day.toString()
            val isSelected = dateStr == selectedDateStr
            val isToday    = day == today
            val hasEvents  = dateStr in eventDates
            Column(
                modifier = Modifier.weight(1f).clickable { onDaySelected(dateStr) }.padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(DAY_SHORT[idx], style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                    val bgColor = when { isSelected -> MaterialTheme.colorScheme.primary; isToday -> MaterialTheme.colorScheme.primaryContainer; else -> null }
                    bgColor?.let { Box(Modifier.size(28.dp).background(it, CircleShape)) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text       = day.day.toString(),
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                            color      = when { isSelected -> MaterialTheme.colorScheme.onPrimary; isToday -> MaterialTheme.colorScheme.primary; else -> MaterialTheme.colorScheme.onSurface },
                            textAlign  = TextAlign.Center
                        )
                        if (hasEvents) {
                            Box(Modifier.size(4.dp).background(
                                if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary, CircleShape))
                        }
                    }
                }
            }
        }
    }
}
