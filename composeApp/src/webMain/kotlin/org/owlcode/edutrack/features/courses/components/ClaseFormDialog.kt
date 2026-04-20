package org.owlcode.edutrack.features.courses.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.owlcode.edutrack.domain.model.*
import org.owlcode.edutrack.features.calendar.components.DatePickerField
import org.owlcode.edutrack.features.calendar.components.TimePickerField
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaseFormDialog(
    initialClase: Clase?,
    courseId: String,
    onDismiss: () -> Unit,
    onSave: (Clase) -> Unit
) {
    var titulo     by remember { mutableStateOf(initialClase?.titulo    ?: "") }
    var modalidad  by remember { mutableStateOf(initialClase?.modalidad ?: ClassMode.PRESENTIAL) }
    var aula       by remember { mutableStateOf(initialClase?.aula      ?: "") }
    var enlace     by remember { mutableStateOf(initialClase?.enlace    ?: "") }
    var notas      by remember { mutableStateOf(initialClase?.notas     ?: "") }
    var startDate  by remember { mutableStateOf(initialClase?.startDate ?: "") }
    var endDate    by remember { mutableStateOf(initialClase?.endDate   ?: "") }
    var startTime  by remember { mutableStateOf(initialClase?.startTime ?: "") }
    var endTime    by remember { mutableStateOf(initialClase?.endTime   ?: "") }
    var showRecurrence by remember { mutableStateOf(false) }
    var recurrenceRule by remember { mutableStateOf(initialClase?.recurrenceRule) }
    var daysOfWeek by remember { mutableStateOf(initialClase?.daysOfWeek ?: emptySet<Int>()) }
    var modeExpanded by remember { mutableStateOf(false) }

    var startDateError by remember { mutableStateOf(false) }
    var dateOrderError by remember { mutableStateOf(false) }
    var timeOrderError by remember { mutableStateOf(false) }
    var daysError      by remember { mutableStateOf(false) }

    if (showRecurrence) {
        RecurrencePickerDialog(
            initialRule    = recurrenceRule,
            selectedDays   = daysOfWeek,
            onDismiss      = { showRecurrence = false },
            onSave         = { rule: RecurrenceRule?, days: Set<Int> ->
                recurrenceRule = rule
                daysOfWeek = days
                showRecurrence = false
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialClase == null) "Nueva clase" else "Editar clase") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título (opcional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                // Modalidad
                ExposedDropdownMenuBox(expanded = modeExpanded, onExpandedChange = { modeExpanded = it }) {
                    OutlinedTextField(
                        value = modalidad.label(), onValueChange = {}, readOnly = true,
                        label = { Text("Modalidad") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = modeExpanded, onDismissRequest = { modeExpanded = false }) {
                        ClassMode.entries.forEach { mode ->
                            DropdownMenuItem(text = { Text(mode.label()) }, onClick = { modalidad = mode; modeExpanded = false })
                        }
                    }
                }

                if (modalidad == ClassMode.PRESENTIAL) {
                    OutlinedTextField(value = aula, onValueChange = { aula = it }, label = { Text("Aula") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                } else {
                    OutlinedTextField(value = enlace, onValueChange = { enlace = it }, label = { Text("Enlace") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }

                OutlinedTextField(value = notas, onValueChange = { notas = it }, label = { Text("Notas") }, modifier = Modifier.fillMaxWidth())

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DatePickerField(
                        value          = startDate,
                        onDateSelected = { startDate = it; startDateError = false; dateOrderError = false },
                        label          = "Inicio *",
                        isError        = startDateError || dateOrderError,
                        modifier       = Modifier.weight(1f)
                    )
                    DatePickerField(
                        value          = endDate,
                        onDateSelected = { endDate = it; dateOrderError = false },
                        label          = "Fin",
                        modifier       = Modifier.weight(1f)
                    )
                }
                if (dateOrderError) Text("La fecha de fin debe ser ≥ inicio", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)

                if (modalidad != ClassMode.SELF_PACED) {
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
                    if (timeOrderError) Text("Hora fin debe ser > hora inicio", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }

                if (daysError) Text("Selecciona al menos un día", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)

                OutlinedButton(onClick = { showRecurrence = true }, modifier = Modifier.fillMaxWidth()) {
                    val ruleLabel = recurrenceRule?.let { "Repetir: ${it.type.label()} (${daysOfWeek.toDayLabel()})" } ?: "Configurar repetición"
                    Text(ruleLabel)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                startDateError = startDate.isBlank() || runCatching { kotlinx.datetime.LocalDate.parse(startDate) }.isFailure
                dateOrderError = endDate.isNotBlank() && runCatching { kotlinx.datetime.LocalDate.parse(startDate) > kotlinx.datetime.LocalDate.parse(endDate) }.getOrDefault(false)
                timeOrderError = startTime.isNotBlank() && endTime.isNotBlank() && endTime <= startTime
                val isWeekly   = recurrenceRule?.type == RecurrenceType.WEEKLY
                daysError      = isWeekly && daysOfWeek.isEmpty()
                if (!startDateError && !dateOrderError && !timeOrderError && !daysError) {
                    onSave(Clase(
                        id             = initialClase?.id ?: "",
                        courseId       = courseId,
                        titulo         = titulo.trim(),
                        modalidad      = modalidad,
                        aula           = aula.trim(),
                        enlace         = enlace.trim(),
                        notas          = notas.trim(),
                        startDate      = startDate.trim(),
                        endDate        = endDate.trim().ifBlank { startDate.trim() },
                        startTime      = startTime.trim().ifBlank { null },
                        endTime        = endTime.trim().ifBlank { null },
                        recurrenceRule = recurrenceRule,
                        daysOfWeek     = daysOfWeek
                    ))
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

private fun ClassMode.label() = when (this) {
    ClassMode.PRESENTIAL -> "Presencial"
    ClassMode.LIVE       -> "En vivo"
    ClassMode.SELF_PACED -> "A tu ritmo"
}

private fun RecurrenceType.label() = when (this) {
    RecurrenceType.NONE    -> "No se repite"
    RecurrenceType.DAILY   -> "Diario"
    RecurrenceType.WEEKLY  -> "Semanal"
    RecurrenceType.MONTHLY -> "Mensual"
}

private fun Set<Int>.toDayLabel(): String {
    val names = listOf("Lu", "Ma", "Mi", "Ju", "Vi", "Sa", "Do")
    return if (isEmpty()) "ningún día" else sorted().joinToString { names[it] }
}



