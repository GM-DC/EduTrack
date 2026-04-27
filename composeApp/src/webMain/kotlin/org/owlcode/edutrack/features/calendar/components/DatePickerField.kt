package org.owlcode.edutrack.features.calendar.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * Campo de selección de fecha reutilizable.
 *
 * @param value           Fecha en formato ISO "YYYY-MM-DD" (vacío = sin fecha).
 * @param onDateSelected  Callback con la fecha seleccionada en formato "YYYY-MM-DD".
 * @param label           Etiqueta del campo.
 * @param isError         Estado de error.
 * @param modifier        Modificador externo.
 */
@Composable
fun DatePickerField(
    value: String,
    onDateSelected: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val displayValue = value.toDateState()?.toDisplayString() ?: value

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) showDialog = true
        }
    }

    OutlinedTextField(
        value             = displayValue,
        onValueChange     = {},
        readOnly          = true,
        label             = { Text(label) },
        placeholder       = { Text("DD / MM / YYYY") },
        isError           = isError,
        trailingIcon      = { Text("📅") },
        modifier          = modifier,
        interactionSource = interactionSource
    )

    if (showDialog) {
        DatePickerDialog(
            initial   = value.toDateState(),   // null → abre en hoy
            onDismiss = { showDialog = false },
            onConfirm = { state ->
                showDialog = false
                onDateSelected(state.toIsoString())
            }
        )
    }
}
