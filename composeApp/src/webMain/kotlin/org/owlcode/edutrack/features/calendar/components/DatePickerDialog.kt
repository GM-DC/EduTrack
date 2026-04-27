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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Diálogo de selección de fecha con preview dinámico "Lun, 28 Abr 2026".
 * Si [initial] es null, abre siempre en la fecha de hoy.
 * El botón "Aplicar" solo se habilita cuando la fecha es válida.
 */
@Composable
fun DatePickerDialog(
    initial: DateState? = null,
    onDismiss: () -> Unit,
    onConfirm: (DateState) -> Unit
) {
    val todayDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val resolved  = initial ?: DateState(
        day   = todayDate.day,
        month = todayDate.month.ordinal + 1,
        year  = todayDate.year
    )

    var draft     by remember { mutableStateOf(resolved) }
    var dayText   by remember { mutableStateOf(resolved.day.toString().padStart(2, '0')) }
    var monthText by remember { mutableStateOf(resolved.month.toString().padStart(2, '0')) }
    var yearText  by remember { mutableStateOf(resolved.year.toString()) }

    LaunchedEffect(dayText, monthText, yearText) {
        val d = dayText.toIntOrNull()   ?: return@LaunchedEffect
        val m = monthText.toIntOrNull() ?: return@LaunchedEffect
        val y = yearText.toIntOrNull()  ?: return@LaunchedEffect
        draft = draft.copy(day = d, month = m, year = y)
    }

    val isValid = draft.isValid()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecciona fecha") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Preview dinámico ─────────────────────────────────────────
                Surface(
                    shape    = RoundedCornerShape(12.dp),
                    color    = if (isValid) MaterialTheme.colorScheme.primaryContainer
                               else        MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text       = draft.toFriendlyString(),
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign  = TextAlign.Center,
                        color      = if (isValid) MaterialTheme.colorScheme.onPrimaryContainer
                                     else        MaterialTheme.colorScheme.onErrorContainer,
                        modifier   = Modifier.padding(vertical = 12.dp)
                    )
                }

                // ── Campos: Día / Mes / Año ──────────────────────────────────
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    // Día
                    OutlinedTextField(
                        value           = dayText,
                        onValueChange   = { dayText = it.filter(Char::isDigit).take(2) },
                        label           = { Text("Día") },
                        singleLine      = true,
                        isError         = dayText.toIntOrNull()?.let { it !in 1..draft.daysInMonth() } ?: true,
                        textStyle       = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier        = Modifier.weight(1f)
                    )

                    // Mes
                    OutlinedTextField(
                        value           = monthText,
                        onValueChange   = { monthText = it.filter(Char::isDigit).take(2) },
                        label           = { Text("Mes") },
                        singleLine      = true,
                        isError         = monthText.toIntOrNull()?.let { it !in 1..12 } ?: true,
                        textStyle       = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier        = Modifier.weight(1f)
                    )

                    // Año
                    OutlinedTextField(
                        value           = yearText,
                        onValueChange   = { yearText = it.filter(Char::isDigit).take(4) },
                        label           = { Text("Año") },
                        singleLine      = true,
                        isError         = yearText.toIntOrNull()?.let { it !in 1900..MAX_YEAR } ?: true,
                        textStyle       = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier        = Modifier.weight(1.5f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(draft) }, enabled = isValid) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
