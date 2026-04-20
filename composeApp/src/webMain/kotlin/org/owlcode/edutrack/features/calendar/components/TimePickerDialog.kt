package org.owlcode.edutrack.features.calendar.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Diálogo de selección de hora (MVP 0 — solo campos editables).
 * Hora: campo numérico 1–12.
 * Minutos: campo numérico 0–59.
 * AM/PM: botones de toggle.
 */
@Composable
fun TimePickerDialog(
    initial: TimeState = TimeState(),
    onDismiss: () -> Unit,
    onConfirm: (TimeState) -> Unit
) {
    var draft by remember { mutableStateOf(initial) }

    var hourText   by remember { mutableStateOf(initial.hour.toString().padStart(2, '0')) }
    var minuteText by remember { mutableStateOf(initial.minute.toString().padStart(2, '0')) }

    LaunchedEffect(hourText) {
        val parsed = hourText.toIntOrNull()
        if (parsed != null && parsed in 1..12) draft = draft.copy(hour = parsed)
    }
    LaunchedEffect(minuteText) {
        val parsed = minuteText.toIntOrNull()
        if (parsed != null && parsed in 0..59) draft = draft.copy(minute = parsed)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecciona hora") },
        text  = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Preview ───────────────────────────────────────────────────
                Surface(
                    shape    = RoundedCornerShape(12.dp),
                    color    = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text       = draft.toDisplayString(),
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign  = TextAlign.Center,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier   = Modifier.padding(vertical = 12.dp)
                    )
                }

                // ── Hora  :  Minuto ───────────────────────────────────────────
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value           = hourText,
                        onValueChange   = { hourText = it.filter { c -> c.isDigit() }.take(2) },
                        label           = { Text("Hora (1–12)") },
                        singleLine      = true,
                        textStyle       = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier        = Modifier.weight(1f)
                    )

                    Text(":", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value           = minuteText,
                        onValueChange   = { minuteText = it.filter { c -> c.isDigit() }.take(2) },
                        label           = { Text("Min (0–59)") },
                        singleLine      = true,
                        textStyle       = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier        = Modifier.weight(1f)
                    )
                }

                // ── Toggle AM / PM ────────────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(true to "AM", false to "PM").forEach { (amValue, label) ->
                        val selected = draft.isAm == amValue
                        Button(
                            onClick  = { draft = draft.copy(isAm = amValue) },
                            modifier = Modifier.weight(1f),
                            colors   = if (selected)
                                ButtonDefaults.buttonColors()
                            else
                                ButtonDefaults.outlinedButtonColors(),
                            elevation = if (selected) ButtonDefaults.buttonElevation() else null
                        ) {
                            Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(draft) }) { Text("Aplicar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
