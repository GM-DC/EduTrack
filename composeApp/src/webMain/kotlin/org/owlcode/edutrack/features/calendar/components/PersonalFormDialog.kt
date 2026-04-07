package org.owlcode.edutrack.features.calendar.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.owlcode.edutrack.domain.model.EventoPersonal

@Composable
fun PersonalFormDialog(
    initialEvento: EventoPersonal?,
    defaultDate: String,
    onDismiss: () -> Unit,
    onSave: (EventoPersonal) -> Unit
) {
    var titulo      by remember { mutableStateOf(initialEvento?.titulo      ?: "") }
    var descripcion by remember { mutableStateOf(initialEvento?.descripcion ?: "") }
    var fecha       by remember { mutableStateOf(initialEvento?.fecha       ?: defaultDate) }
    var horaInicio  by remember { mutableStateOf(initialEvento?.horaInicio  ?: "") }
    var horaFin     by remember { mutableStateOf(initialEvento?.horaFin     ?: "") }

    var tituloError    by remember { mutableStateOf(false) }
    var fechaError     by remember { mutableStateOf(false) }
    var horasError     by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialEvento == null) "Nuevo evento personal" else "Editar evento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value         = titulo,
                    onValueChange = { titulo = it; tituloError = false },
                    label         = { Text("Título *") },
                    isError       = tituloError,
                    supportingText = if (tituloError) ({ Text("El título no puede estar vacío") }) else null,
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value         = descripcion,
                    onValueChange = { descripcion = it },
                    label         = { Text("Descripción") },
                    modifier      = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value         = fecha,
                    onValueChange = { fecha = it; fechaError = false },
                    label         = { Text("Fecha (YYYY-MM-DD) *") },
                    placeholder   = { Text("2026-04-07") },
                    isError       = fechaError,
                    supportingText = if (fechaError) ({ Text("Fecha inválida") }) else null,
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value         = horaInicio,
                        onValueChange = { horaInicio = it; horasError = false },
                        label         = { Text("Hora inicio (HH:mm)") },
                        placeholder   = { Text("08:00") },
                        singleLine    = true,
                        modifier      = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value         = horaFin,
                        onValueChange = { horaFin = it; horasError = false },
                        label         = { Text("Hora fin (HH:mm)") },
                        placeholder   = { Text("10:00") },
                        isError       = horasError,
                        singleLine    = true,
                        modifier      = Modifier.weight(1f)
                    )
                }
                if (horasError) {
                    Text("La hora de fin debe ser posterior al inicio",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                tituloError = titulo.isBlank()
                fechaError  = runCatching { kotlinx.datetime.LocalDate.parse(fecha) }.isFailure
                horasError  = horaInicio.isNotBlank() && horaFin.isNotBlank() && horaFin <= horaInicio
                if (!tituloError && !fechaError && !horasError) {
                    onSave(EventoPersonal(
                        id          = initialEvento?.id ?: "",
                        titulo      = titulo.trim(),
                        descripcion = descripcion.trim(),
                        fecha       = fecha.trim(),
                        horaInicio  = horaInicio.trim().ifBlank { null },
                        horaFin     = horaFin.trim().ifBlank { null }
                    ))
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

