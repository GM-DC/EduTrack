package org.owlcode.edutrack.features.courses.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.owlcode.edutrack.domain.model.Examen
import org.owlcode.edutrack.domain.model.ExamStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamenFormDialog(
    initialExamen: Examen?,
    courseId: String,
    onDismiss: () -> Unit,
    onSave: (Examen) -> Unit
) {
    var titulo     by remember { mutableStateOf(initialExamen?.titulo     ?: "") }
    var tema       by remember { mutableStateOf(initialExamen?.tema       ?: "") }
    var fecha      by remember { mutableStateOf(initialExamen?.fecha      ?: "") }
    var horaInicio by remember { mutableStateOf(initialExamen?.horaInicio ?: "") }
    var horaFin    by remember { mutableStateOf(initialExamen?.horaFin    ?: "") }
    var estado     by remember { mutableStateOf(initialExamen?.estado     ?: ExamStatus.PENDING) }
    var estadoExpanded by remember { mutableStateOf(false) }

    var tituloError by remember { mutableStateOf(false) }
    var fechaError  by remember { mutableStateOf(false) }
    var horasError  by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialExamen == null) "Nuevo examen" else "Editar examen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = titulo, onValueChange = { titulo = it; tituloError = false },
                    label = { Text("Título *") }, isError = tituloError,
                    supportingText = if (tituloError) ({ Text("El título no puede estar vacío") }) else null,
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(value = tema, onValueChange = { tema = it }, label = { Text("Tema") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = fecha, onValueChange = { fecha = it; fechaError = false },
                    label = { Text("Fecha (YYYY-MM-DD) *") },
                    placeholder = { Text("2026-04-07") },
                    isError = fechaError,
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = horaInicio, onValueChange = { horaInicio = it; horasError = false },
                        label = { Text("Hora inicio") },
                        placeholder = { Text("08:00") },
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = horaFin, onValueChange = { horaFin = it; horasError = false },
                        label = { Text("Hora fin") },
                        placeholder = { Text("10:00") },
                        supportingText = if (horaFin.isBlank() && horaInicio.isNotBlank()) ({ Text("Vacío = +1 hora automática", style = MaterialTheme.typography.labelSmall) }) else null,
                        isError = horasError, singleLine = true, modifier = Modifier.weight(1f)
                    )
                }
                if (horasError) Text("Hora fin debe ser > hora inicio", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)

                // Estado (solo al editar)
                if (initialExamen != null) {
                    ExposedDropdownMenuBox(expanded = estadoExpanded, onExpandedChange = { estadoExpanded = it }) {
                        OutlinedTextField(value = estado.label(), onValueChange = {}, readOnly = true, label = { Text("Estado") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = estadoExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable))
                        ExposedDropdownMenu(expanded = estadoExpanded, onDismissRequest = { estadoExpanded = false }) {
                            ExamStatus.entries.forEach { s -> DropdownMenuItem(text = { Text(s.label()) }, onClick = { estado = s; estadoExpanded = false }) }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                tituloError = titulo.isBlank()
                fechaError  = fecha.isBlank() || runCatching { kotlinx.datetime.LocalDate.parse(fecha) }.isFailure
                horasError  = horaInicio.isNotBlank() && horaFin.isNotBlank() && horaFin <= horaInicio
                if (!tituloError && !fechaError && !horasError) {
                    onSave(Examen(
                        id         = initialExamen?.id ?: "",
                        courseId   = courseId,
                        titulo     = titulo.trim(),
                        tema       = tema.trim(),
                        fecha      = fecha.trim(),
                        horaInicio = horaInicio.trim().ifBlank { null },
                        horaFin    = horaFin.trim().ifBlank { null },
                        estado     = estado
                    ))
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

private fun ExamStatus.label() = when (this) {
    ExamStatus.PENDING      -> "Pendiente"
    ExamStatus.TAKEN        -> "Rendido"
    ExamStatus.RESCHEDULED  -> "Reprogramado"
    ExamStatus.CANCELLED    -> "Cancelado"
}

