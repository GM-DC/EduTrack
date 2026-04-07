package org.owlcode.edutrack.features.calendar.month

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import org.owlcode.edutrack.features.calendar.AgendaUiState
import org.owlcode.edutrack.features.calendar.CalendarViewModel
import org.owlcode.edutrack.features.calendar.components.AgendaEventCard
import org.owlcode.edutrack.features.calendar.components.MonthCalendar

private val MONTH_ES = listOf(
    "", "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
)
private val DAY_ES = listOf(
    "", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo"
)

@Composable
fun MonthView(
    state: AgendaUiState,
    viewModel: CalendarViewModel
) {
    val today = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    val selectedLabel = remember(state.selectedDateStr) {
        runCatching {
            val d        = LocalDate.parse(state.selectedDateStr)
            val tomorrow = today.plus(1, DateTimeUnit.DAY)
            val monthIdx = d.month.ordinal + 1
            val dowIdx   = d.dayOfWeek.ordinal + 1
            when (d) {
                today    -> "Hoy, ${d.day} de ${MONTH_ES[monthIdx]}"
                tomorrow -> "Mañana, ${d.day} de ${MONTH_ES[monthIdx]}"
                else     -> "${DAY_ES[dowIdx].replaceFirstChar { it.uppercaseChar() }}, ${d.day} de ${MONTH_ES[monthIdx]}"
            }
        }.getOrDefault(state.selectedDateStr)
    }

    LazyColumn(
        modifier       = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ── Calendario mensual ─────────────────────────────────────────
        item {
            Spacer(Modifier.height(8.dp))
            MonthCalendar(
                year            = state.currentYear,
                month           = state.currentMonth,
                selectedDateStr = state.selectedDateStr,
                itemsByDate     = state.itemsByDateForDots,
                onDaySelected   = viewModel::selectDate,
                onPrevMonth     = viewModel::prevMonth,
                onNextMonth     = viewModel::nextMonth
            )
            Spacer(Modifier.height(8.dp))
        }

        // ── Cabecera del día seleccionado ──────────────────────────────
        item {
            Text(
                text       = selectedLabel,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
        }

        // ── Estados ────────────────────────────────────────────────────
        if (state.isLoading) {
            item {
                Box(
                    modifier         = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
        } else if (state.error != null) {
            item {
                Column(
                    modifier            = Modifier.fillMaxWidth().padding(top = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.error, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = viewModel::loadEvents) { Text("Reintentar") }
                }
            }
        } else if (state.itemsForSelectedDay.isEmpty()) {
            item {
                Box(
                    modifier         = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = "Sin eventos para este día",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(state.itemsForSelectedDay, key = { it.id }) { item ->
                AgendaEventCard(
                    item     = item,
                    onEdit   = {
                        if (item.type == org.owlcode.edutrack.domain.model.EventType.PERSONAL) {
                            // Reconstruir EventoPersonal mínimo para editar
                            viewModel.showEditPersonalForm(
                                org.owlcode.edutrack.domain.model.EventoPersonal(
                                    id          = item.sourceId,
                                    titulo      = item.title,
                                    fecha       = item.date,
                                    horaInicio  = item.startTime,
                                    horaFin     = item.endTime
                                )
                            )
                        }
                    },
                    onDelete = { viewModel.requestDelete(item) }
                )
            }
        }
    }
}




