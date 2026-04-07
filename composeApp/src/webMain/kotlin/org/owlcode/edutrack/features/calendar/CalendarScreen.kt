package org.owlcode.edutrack.features.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.owlcode.edutrack.features.calendar.components.CalendarModeTabs
import org.owlcode.edutrack.features.calendar.components.DeleteConfirmDialog
import org.owlcode.edutrack.features.calendar.components.PersonalFormDialog
import org.owlcode.edutrack.features.calendar.day.DayView
import org.owlcode.edutrack.features.calendar.month.MonthView
import org.owlcode.edutrack.features.calendar.week.WeekView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onLogout: () -> Unit,
    onNavigateToCourses: () -> Unit = {},
    viewModel: CalendarViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title   = { Text("Agenda") },
                actions = {
                    TextButton(onClick = onNavigateToCourses) { Text("Cursos") }
                    TextButton(onClick = { viewModel.logout(onLogout) }) { Text("Salir") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::showCreatePersonalForm) {
                Text("+", style = MaterialTheme.typography.headlineSmall)
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            CalendarModeTabs(selectedMode = state.viewMode, onModeSelected = viewModel::changeMode)
            FilterChipRow(activeFilter = state.activeFilter, onFilterSelected = viewModel::setFilter)
            when (state.viewMode) {
                CalendarViewMode.MONTH -> MonthView(state = state, viewModel = viewModel)
                CalendarViewMode.WEEK  -> WeekView(state = state, viewModel = viewModel)
                CalendarViewMode.DAY   -> DayView(state = state, viewModel = viewModel)
            }
        }

        if (state.showPersonalForm) {
            PersonalFormDialog(
                initialEvento = state.editingPersonal,
                defaultDate   = state.selectedDateStr,
                onDismiss     = viewModel::dismissPersonalForm,
                onSave        = viewModel::savePersonal
            )
        }

        if (state.pendingDeleteItem != null) {
            DeleteConfirmDialog(
                message   = "¿Eliminar este evento? Esta acción no se puede deshacer.",
                onConfirm = viewModel::confirmDelete,
                onDismiss = viewModel::cancelDelete
            )
        }
    }
}

@Composable
private fun FilterChipRow(
    activeFilter: CalendarFilter,
    onFilterSelected: (CalendarFilter) -> Unit
) {
    val filters = listOf(
        CalendarFilter.ALL      to "Todo",
        CalendarFilter.ACADEMIC to "Académico",
        CalendarFilter.PERSONAL to "Personal"
    )
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
        filters.forEachIndexed { idx, (filter, label) ->
            SegmentedButton(
                selected = activeFilter == filter,
                onClick  = { onFilterSelected(filter) },
                shape    = SegmentedButtonDefaults.itemShape(index = idx, count = filters.size)
            ) { Text(label, style = MaterialTheme.typography.labelSmall) }
        }
    }
}
