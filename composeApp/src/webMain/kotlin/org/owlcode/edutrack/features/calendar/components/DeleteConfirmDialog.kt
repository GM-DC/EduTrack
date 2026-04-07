package org.owlcode.edutrack.features.calendar.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun DeleteConfirmDialog(
    message: String = "¿Eliminar este elemento? Esta acción no se puede deshacer.",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title            = { Text("Confirmar eliminación") },
        text             = { Text(message) },
        confirmButton    = {
            TextButton(onClick = onConfirm) { Text("Eliminar") }
        },
        dismissButton    = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

