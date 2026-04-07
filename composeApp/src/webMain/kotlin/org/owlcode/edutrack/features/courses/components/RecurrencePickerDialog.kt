package org.owlcode.edutrack.features.courses.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.owlcode.edutrack.domain.model.RecurrenceEndType
import org.owlcode.edutrack.domain.model.RecurrenceRule
import org.owlcode.edutrack.domain.model.RecurrenceType

private val DAY_LABELS = listOf("Lu", "Ma", "Mi", "Ju", "Vi", "Sa", "Do")
private val MONTH_SHORT = listOf("", "ene","feb","mar","abr","may","jun","jul","ago","sep","oct","nov","dic")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurrencePickerDialog(
    initialRule: RecurrenceRule?,
    selectedDays: Set<Int>,
    onDismiss: () -> Unit,
    onSave: (RecurrenceRule?, Set<Int>) -> Unit
) {
    var type       by remember { mutableStateOf(initialRule?.type     ?: RecurrenceType.NONE) }
    var interval   by remember { mutableStateOf(initialRule?.interval?.toString() ?: "1") }
    var days       by remember { mutableStateOf(selectedDays) }
    var endType    by remember { mutableStateOf(initialRule?.endType  ?: RecurrenceEndType.NEVER) }
    var untilDate  by remember { mutableStateOf(initialRule?.untilDate ?: "") }
    var occCount   by remember { mutableStateOf(initialRule?.occurrenceCount?.toString() ?: "1") }
    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Periodicidad") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // ── Tipo de recurrencia ──────────────────────────────────
                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                    OutlinedTextField(
                        value = type.label(), onValueChange = {}, readOnly = true,
                        label = { Text("Repetir") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        RecurrenceType.entries.forEach { t ->
                            DropdownMenuItem(text = { Text(t.label()) }, onClick = { type = t; typeExpanded = false })
                        }
                    }
                }

                // ── Intervalo ────────────────────────────────────────────
                if (type != RecurrenceType.NONE) {
                    OutlinedTextField(
                        value = interval, onValueChange = { if (it.all(Char::isDigit)) interval = it },
                        label = { Text("Cada (número)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ── Días de semana (solo WEEKLY) ──────────────────────────
                if (type == RecurrenceType.WEEKLY) {
                    Text("Días de la semana", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        DAY_LABELS.forEachIndexed { idx, label ->
                            val selected = idx in days
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                    .clickable { days = if (selected) days - idx else days + idx },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // ── Condición de fin ─────────────────────────────────────
                if (type != RecurrenceType.NONE) {
                    Text("Termina", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    RecurrenceEndType.entries.forEach { et ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { endType = et }) {
                            RadioButton(selected = endType == et, onClick = { endType = et })
                            Text(et.label(), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    if (endType == RecurrenceEndType.UNTIL_DATE) {
                        OutlinedTextField(
                            value = untilDate, onValueChange = { untilDate = it },
                            label = { Text("Hasta (YYYY-MM-DD)") },
                            placeholder = { Text("2026-04-07") },
                            singleLine = true, modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (endType == RecurrenceEndType.AFTER_OCCURRENCES) {
                        OutlinedTextField(value = occCount, onValueChange = { if (it.all(Char::isDigit)) occCount = it }, label = { Text("Número de repeticiones") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    }

                    // Resumen dinámico
                    val summary = buildSummary(type, interval.toIntOrNull() ?: 1, days, endType, untilDate, occCount.toIntOrNull() ?: 1)
                    if (summary.isNotBlank()) {
                        Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val rule = if (type == RecurrenceType.NONE) null else RecurrenceRule(
                    type            = type,
                    interval        = interval.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                    daysOfWeek      = if (type == RecurrenceType.WEEKLY) days else emptySet(),
                    endType         = endType,
                    untilDate       = if (endType == RecurrenceEndType.UNTIL_DATE) untilDate.ifBlank { null } else null,
                    occurrenceCount = if (endType == RecurrenceEndType.AFTER_OCCURRENCES) occCount.toIntOrNull()?.coerceAtLeast(1) else null
                )
                onSave(rule, if (type == RecurrenceType.WEEKLY) days else emptySet())
            }) { Text("Aplicar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

private fun RecurrenceType.label() = when (this) {
    RecurrenceType.NONE    -> "No repetir"
    RecurrenceType.DAILY   -> "Diario"
    RecurrenceType.WEEKLY  -> "Semanal"
    RecurrenceType.MONTHLY -> "Mensual"
}

private fun RecurrenceEndType.label() = when (this) {
    RecurrenceEndType.NEVER              -> "Nunca"
    RecurrenceEndType.UNTIL_DATE         -> "Hasta fecha"
    RecurrenceEndType.AFTER_OCCURRENCES  -> "Después de N veces"
}

private fun buildSummary(
    type: RecurrenceType,
    interval: Int,
    days: Set<Int>,
    endType: RecurrenceEndType,
    untilDate: String,
    occCount: Int
): String {
    if (type == RecurrenceType.NONE) return ""
    val dayNames   = listOf("lunes","martes","miércoles","jueves","viernes","sábado","domingo")
    val base = when (type) {
        RecurrenceType.DAILY   -> if (interval == 1) "Se repite cada día" else "Se repite cada $interval días"
        RecurrenceType.WEEKLY  -> {
            val dStr = days.sorted().joinToString(" y ") { dayNames[it] }
            if (interval == 1) "Se repite cada $dStr" else "Se repite cada $interval semanas: $dStr"
        }
        RecurrenceType.MONTHLY -> if (interval == 1) "Se repite cada mes" else "Se repite cada $interval meses"
        RecurrenceType.NONE    -> ""
    }
    val end = when (endType) {
        RecurrenceEndType.NEVER              -> ""
        RecurrenceEndType.UNTIL_DATE         -> if (untilDate.isNotBlank()) " hasta $untilDate" else ""
        RecurrenceEndType.AFTER_OCCURRENCES  -> " ($occCount veces)"
    }
    return base + end
}

