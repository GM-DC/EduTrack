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
 * Diálogo de selección de fecha con preview dinámico.
 * Muestra campos numéricos para Día, Mes y Año.
 * El botón "Aplicar" solo se habilita cuando la fecha es válida.
 */
@Composable
fun DatePickerDialog(
    initial: DateState = DateState(),
    onDismiss: () -> Unit,
    onConfirm: (DateState) -> Unit
) {
    var draft by remember { mutableStateOf(initial) }

    var dayText   by remember { mutableStateOf(initial.day.toString().padStart(2, '0')) }
    var monthText by remember { mutableStateOf(initial.month.toString().padStart(2, '0')) }
    var yearText  by remember { mutableStateOf(initial.year.toString()) }

    LaunchedEffect(dayText, monthText, yearText) {
        val d = dayText.toIntOrNull()   ?: return@LaunchedEffect
        val m = monthText.toIntOrNull() ?: return@LaunchedEffect
        val y = yearText.toIntOrNull()  ?: return@LaunchedEffect
        draft = DateState(day = d, month = m, year = y)
    }

    val isValid = draft.isValid()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecciona fecha") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Preview dinámico ─────────────────────────────────────────────
                Surface(
                    shape    = RoundedCornerShape(12.dp),
                    color    = if (isValid)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text       = if (isValid) draft.toDisplayString() else "Fecha inválida",
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign  = TextAlign.Center,
                        color      = if (isValid)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer,
                        modifier   = Modifier.padding(vertical = 12.dp)
                    )
                }

                // ── Campos: Día / Mes / Año ──────────────────────────────────────
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value           = dayText,
                        onValueChange   = { dayText = it.filter(Char::isDigit).take(2) },
                        label           = { Text("Día") },
                        singleLine      = true,
                        isError         = dayText.toIntOrNull()?.let { it !in 1..31 } ?: true,
                        textStyle       = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier        = Modifier.weight(1f)
                    )

                    Text("/", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

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

                    Text("/", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value           = yearText,
                        onValueChange   = { yearText = it.filter(Char::isDigit).take(4) },
                        label           = { Text("Año") },
                        singleLine      = true,
                        isError         = yearText.toIntOrNull()?.let { it < 1900 } ?: true,
                        textStyle       = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier        = Modifier.weight(2f)
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
