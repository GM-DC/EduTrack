package org.owlcode.edutrack.features.courses.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.owlcode.edutrack.domain.model.Tarea
import org.owlcode.edutrack.domain.model.TaskPriority
import org.owlcode.edutrack.domain.model.TaskStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareaFormDialog(
    initialTarea: Tarea?,
    courseId: String,
    onDismiss: () -> Unit,
    onSave: (Tarea) -> Unit
) {
    var titulo      by remember { mutableStateOf(initialTarea?.titulo      ?: "") }
    var descripcion by remember { mutableStateOf(initialTarea?.descripcion ?: "") }
    var prioridad   by remember { mutableStateOf(initialTarea?.prioridad   ?: TaskPriority.MEDIUM) }
    var dueDate     by remember { mutableStateOf(initialTarea?.dueDate     ?: "") }
    var dueTime     by remember { mutableStateOf(initialTarea?.dueTime     ?: "") }
    var estado      by remember { mutableStateOf(initialTarea?.estado      ?: TaskStatus.PENDING) }
    var priorExpanded by remember { mutableStateOf(false) }
    var estadoExpanded by remember { mutableStateOf(false) }

    var tituloError   by remember { mutableStateOf(false) }
    var dueDateError  by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialTarea == null) "Nueva tarea" else "Editar tarea") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = titulo, onValueChange = { titulo = it; tituloError = false },
                    label = { Text("Título *") }, isError = tituloError,
                    supportingText = if (tituloError) ({ Text("El título no puede estar vacío") }) else null,
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())

                // Prioridad
                ExposedDropdownMenuBox(expanded = priorExpanded, onExpandedChange = { priorExpanded = it }) {
                    OutlinedTextField(value = prioridad.label(), onValueChange = {}, readOnly = true, label = { Text("Prioridad") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable))
                    ExposedDropdownMenu(expanded = priorExpanded, onDismissRequest = { priorExpanded = false }) {
                        TaskPriority.entries.forEach { p -> DropdownMenuItem(text = { Text(p.label()) }, onClick = { prioridad = p; priorExpanded = false }) }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dueDate, onValueChange = { dueDate = it; dueDateError = false },
                        label = { Text("Fecha límite (YYYY-MM-DD) *") },
                        placeholder = { Text("2026-04-07") },
                        isError = dueDateError,
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dueTime, onValueChange = { dueTime = it },
                        label = { Text("Hora (HH:mm)") },
                        placeholder = { Text("08:00") },
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                }
                if (dueDateError) Text("Fecha requerida", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)

                // Estado (solo al editar)
                if (initialTarea != null) {
                    ExposedDropdownMenuBox(expanded = estadoExpanded, onExpandedChange = { estadoExpanded = it }) {
                        OutlinedTextField(value = estado.label(), onValueChange = {}, readOnly = true, label = { Text("Estado") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = estadoExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable))
                        ExposedDropdownMenu(expanded = estadoExpanded, onDismissRequest = { estadoExpanded = false }) {
                            TaskStatus.entries.forEach { s -> DropdownMenuItem(text = { Text(s.label()) }, onClick = { estado = s; estadoExpanded = false }) }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                tituloError  = titulo.isBlank()
                dueDateError = dueDate.isBlank() || runCatching { kotlinx.datetime.LocalDate.parse(dueDate) }.isFailure
                if (!tituloError && !dueDateError) {
                    onSave(Tarea(
                        id          = initialTarea?.id ?: "",
                        courseId    = courseId,
                        titulo      = titulo.trim(),
                        descripcion = descripcion.trim(),
                        prioridad   = prioridad,
                        dueDate     = dueDate.trim(),
                        dueTime     = dueTime.trim().ifBlank { null },
                        estado      = estado
                    ))
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

private fun TaskPriority.label() = when (this) { TaskPriority.LOW -> "Baja"; TaskPriority.MEDIUM -> "Media"; TaskPriority.HIGH -> "Alta" }
private fun TaskStatus.label()   = when (this) { TaskStatus.PENDING -> "Pendiente"; TaskStatus.IN_PROGRESS -> "En progreso"; TaskStatus.SUBMITTED -> "Entregada"; TaskStatus.OVERDUE -> "Vencida" }

