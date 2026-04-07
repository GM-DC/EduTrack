package org.owlcode.edutrack.features.calendar.day

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import org.owlcode.edutrack.features.calendar.AgendaUiState
import org.owlcode.edutrack.features.calendar.CalendarViewModel
import org.owlcode.edutrack.features.calendar.components.EventBlock
import org.owlcode.edutrack.features.calendar.components.HOUR_HEIGHT_DP
import org.owlcode.edutrack.features.calendar.components.TimeColumn
import org.owlcode.edutrack.features.calendar.components.toItemColor

private const val START_HOUR  = 7
private const val END_HOUR    = 22
private const val TOTAL_HOURS = END_HOUR - START_HOUR + 1

private val MONTH_ES = listOf("","enero","febrero","marzo","abril","mayo","junio","julio","agosto","septiembre","octubre","noviembre","diciembre")
private val DOW_ES   = listOf("lunes","martes","miércoles","jueves","viernes","sábado","domingo")

@Composable
fun DayView(state: AgendaUiState, viewModel: CalendarViewModel) {
    val today        = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    val selectedDate = remember(state.selectedDateStr) { LocalDate.parse(state.selectedDateStr) }
    val allItems     = state.eventsByDate[state.selectedDateStr].orEmpty()
    val allDayItems  = allItems.filter { it.isAllDay || it.isDeadline }
    val timedItems   = allItems.filter { !it.isAllDay && !it.isDeadline }
    val scrollState  = rememberScrollState()
    val density      = LocalDensity.current

    LaunchedEffect(state.selectedDateStr) {
        val now    = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val minOff = now.hour * 60 + now.minute
        val px     = with(density) { ((minOff - START_HOUR * 60) / 60f * HOUR_HEIGHT_DP).dp.toPx().toInt() }.coerceAtLeast(0)
        scrollState.animateScrollTo(px)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        DayHeader(date = selectedDate, today = today, onPrev = viewModel::prevPeriod, onNext = viewModel::nextPeriod)
        HorizontalDivider()

        // ── Zona superior: all-day / deadlines ────────────────────────
        if (allDayItems.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                allDayItems.forEach { item ->
                    val color = item.courseColor.toItemColor(item.type)
                    Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.18f), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(if (item.isDeadline) "📋" else "☀", style = MaterialTheme.typography.labelSmall)
                            Text(item.title, style = MaterialTheme.typography.labelMedium, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
            HorizontalDivider()
        }

        // ── Timeline scrolleable ──────────────────────────────────────
        Row(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
            TimeColumn(startHour = START_HOUR, endHour = END_HOUR)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height((TOTAL_HOURS * HOUR_HEIGHT_DP).dp)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                repeat(TOTAL_HOURS) { idx ->
                    HorizontalDivider(modifier = Modifier.offset(y = (idx * HOUR_HEIGHT_DP).dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                }
                if (selectedDate == today) {
                    val nowMin = remember {
                        val n = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                        n.hour * 60 + n.minute
                    }
                    Box(modifier = Modifier.offset(y = ((nowMin - START_HOUR * 60) / 60f * HOUR_HEIGHT_DP).dp).fillMaxWidth().height(2.dp).background(MaterialTheme.colorScheme.error))
                }
                timedItems.forEach { item -> EventBlock(item = item, startHour = START_HOUR) }
            }
        }
    }
}

@Composable
private fun DayHeader(date: LocalDate, today: LocalDate, onPrev: () -> Unit, onNext: () -> Unit) {
    val monthIdx = date.month.ordinal + 1
    val dowIdx   = date.dayOfWeek.ordinal
    val label    = when (date) {
        today -> "Hoy · ${date.day} de ${MONTH_ES[monthIdx]} ${date.year}"
        else  -> "${DOW_ES[dowIdx].replaceFirstChar { it.uppercaseChar() }}, ${date.day} de ${MONTH_ES[monthIdx]} ${date.year}"
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onPrev) { Text("‹", style = MaterialTheme.typography.headlineSmall) }
        Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        TextButton(onClick = onNext) { Text("›", style = MaterialTheme.typography.headlineSmall) }
    }
}
