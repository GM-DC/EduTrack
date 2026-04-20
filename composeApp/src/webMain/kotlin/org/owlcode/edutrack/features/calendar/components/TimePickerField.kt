package org.owlcode.edutrack.features.calendar.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * Campo de selección de hora reutilizable (MVP 0).
 *
 * @param value          Hora en formato 24h "HH:mm" (vacío = sin hora).
 * @param onTimeSelected Callback con la hora seleccionada en formato 24h "HH:mm".
 * @param label          Etiqueta del campo.
 * @param isError        Estado de error.
 * @param modifier       Modificador externo.
 */
@Composable
fun TimePickerField(
    value: String,
    onTimeSelected: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val displayValue = value.toTimeState()?.toDisplayString() ?: value

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
        placeholder       = { Text("--:-- --") },
        isError           = isError,
        trailingIcon      = { Text("🕐") },
        modifier          = modifier,
        interactionSource = interactionSource
    )

    if (showDialog) {
        val initial = value.toTimeState() ?: TimeState()
        TimePickerDialog(
            initial   = initial,
            onDismiss = { showDialog = false },
            onConfirm = { state ->
                showDialog = false
                onTimeSelected(state.to24h())
            }
        )
    }
}

