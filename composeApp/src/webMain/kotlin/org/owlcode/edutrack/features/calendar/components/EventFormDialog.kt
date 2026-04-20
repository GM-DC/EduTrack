package org.owlcode.edutrack.features.calendar.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.owlcode.edutrack.domain.model.CalendarEvent
import org.owlcode.edutrack.domain.model.EventType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFormDialog(
    initialEvent: CalendarEvent?,        // null = crear, non-null = editar
    defaultDate: String,                 // fecha preseleccionada del calendario
    onDismiss: () -> Unit,
    onSave: (CalendarEvent) -> Unit
) {
    var title       by remember { mutableStateOf(initialEvent?.title       ?: "") }
    var description by remember { mutableStateOf(initialEvent?.description ?: "") }
    var date        by remember { mutableStateOf(initialEvent?.date        ?: defaultDate) }
    var startTime   by remember { mutableStateOf(initialEvent?.startTime   ?: "") }
    var endTime     by remember { mutableStateOf(initialEvent?.endTime     ?: "") }
    var eventType   by remember { mutableStateOf(initialEvent?.type        ?: EventType.CLASS) }
    var typeExpanded by remember { mutableStateOf(false) }

    // Errores inline
    var titleError     by remember { mutableStateOf(false) }
    var dateError      by remember { mutableStateOf(false) }
    var timeOrderError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialEvent == null) "Nuevo evento" else "Editar evento")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                // Título
                OutlinedTextField(
                    value         = title,
                    onValueChange = { title = it; titleError = false },
                    label         = { Text("Título *") },
                    isError       = titleError,
                    supportingText = if (titleError) ({ Text("El título no puede estar vacío") }) else null,
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )

                // Descripción
                OutlinedTextField(
                    value         = description,
                    onValueChange = { description = it },
                    label         = { Text("Descripción") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )

                // Fecha
                OutlinedTextField(
                    value         = date,
                    onValueChange = { date = it; dateError = false },
                    label         = { Text("Fecha (YYYY-MM-DD) *") },
                    placeholder   = { Text("2026-04-07") },
                    isError       = dateError,
                    supportingText = if (dateError) ({ Text("Formato inválido") }) else null,
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )

                // Horas
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimePickerField(
                        value          = startTime,
                        onTimeSelected = { startTime = it; timeOrderError = false },
                        label          = "Hora inicio",
                        modifier       = Modifier.weight(1f)
                    )
                    TimePickerField(
                        value          = endTime,
                        onTimeSelected = { endTime = it; timeOrderError = false },
                        label          = "Hora fin",
                        isError        = timeOrderError,
                        modifier       = Modifier.weight(1f)
                    )
                }
                if (timeOrderError) {
                    Text(
                        text  = "La hora de fin debe ser posterior al inicio",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Tipo de evento (dropdown)
                ExposedDropdownMenuBox(
                    expanded         = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value         = eventType.label(),
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text("Tipo") },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier      = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded         = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        EventType.entries.forEach { type ->
                            DropdownMenuItem(
                                text    = { Text(type.label()) },
                                onClick = { eventType = type; typeExpanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // Validaciones
                titleError     = title.isBlank()
                dateError      = runCatching { kotlinx.datetime.LocalDate.parse(date) }.isFailure
                timeOrderError = startTime.isNotBlank() && endTime.isNotBlank() && endTime <= startTime

                if (!titleError && !dateError && !timeOrderError) {
                    onSave(
                        CalendarEvent(
                            id          = initialEvent?.id ?: "",
                            title       = title.trim(),
                            description = description.trim(),
                            date        = date.trim(),
                            startTime   = startTime.trim(),
                            endTime     = endTime.trim(),
                            type        = eventType
                        )
                    )
                }
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// ── Extensiones privadas del diálogo ──────────────────────────────────────────
private fun EventType.label(): String = when (this) {
    EventType.CLASS    -> "Clase"
    EventType.EXAM     -> "Examen"
    EventType.TASK     -> "Tarea"
    EventType.PERSONAL -> "Personal"
}

