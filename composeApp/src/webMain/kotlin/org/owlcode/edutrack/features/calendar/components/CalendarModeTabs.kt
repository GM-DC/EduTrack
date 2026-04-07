package org.owlcode.edutrack.features.calendar.components

import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.owlcode.edutrack.features.calendar.CalendarViewMode

@Composable
fun CalendarModeTabs(
    selectedMode: CalendarViewMode,
    onModeSelected: (CalendarViewMode) -> Unit
) {
    val tabs = listOf(
        CalendarViewMode.MONTH to "Mes",
        CalendarViewMode.WEEK  to "Semana",
        CalendarViewMode.DAY   to "Día"
    )
    PrimaryTabRow(selectedTabIndex = selectedMode.ordinal) {
        tabs.forEach { (mode, label) ->
            Tab(
                selected = selectedMode == mode,
                onClick  = { onModeSelected(mode) },
                text     = { Text(label) }
            )
        }
    }
}



