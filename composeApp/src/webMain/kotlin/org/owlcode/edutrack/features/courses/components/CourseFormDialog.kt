package org.owlcode.edutrack.features.courses.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.owlcode.edutrack.domain.model.Course

private val COLOR_PALETTE = listOf(
    "#4A90D9", "#E05252", "#7B68EE", "#F5A623",
    "#50C878", "#FF69B4", "#20B2AA", "#FF8C00"
)

@Composable
fun CourseFormDialog(
    initialCourse: Course?,
    onDismiss: () -> Unit,
    onSave: (Course) -> Unit
) {
    var name        by remember { mutableStateOf(initialCourse?.name                ?: "") }
    var teacher     by remember { mutableStateOf(initialCourse?.teacher             ?: "") }
    var description by remember { mutableStateOf(initialCourse?.description         ?: "") }
    var location    by remember { mutableStateOf(initialCourse?.locationOrPlatform  ?: "") }
    var credits     by remember { mutableStateOf(initialCourse?.credits?.toString() ?: "") }
    var color       by remember { mutableStateOf(initialCourse?.color               ?: COLOR_PALETTE.first()) }

    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialCourse == null) "Nuevo curso" else "Editar curso")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                // Nombre
                OutlinedTextField(
                    value          = name,
                    onValueChange  = { name = it; nameError = false },
                    label          = { Text("Nombre *") },
                    isError        = nameError,
                    supportingText = if (nameError) ({ Text("El nombre no puede estar vacío") }) else null,
                    singleLine     = true,
                    modifier       = Modifier.fillMaxWidth()
                )

                // Profesor
                OutlinedTextField(
                    value         = teacher,
                    onValueChange = { teacher = it },
                    label         = { Text("Profesor") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )

                // Descripción
                OutlinedTextField(
                    value         = description,
                    onValueChange = { description = it },
                    label         = { Text("Descripción") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )

                // Aula / Plataforma
                OutlinedTextField(
                    value         = location,
                    onValueChange = { location = it },
                    label         = { Text("Aula o plataforma") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )

                // Créditos
                OutlinedTextField(
                    value         = credits,
                    onValueChange = { if (it.all(Char::isDigit)) credits = it },
                    label         = { Text("Créditos (opcional)") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )

                // Selector de color
                Text(
                    text  = "Color",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    COLOR_PALETTE.forEach { hex ->
                        val selected = hex == color
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(hex.toComposeColor())
                                .then(
                                    if (selected) Modifier.border(3.dp, Color.White, CircleShape)
                                    else Modifier
                                )
                                .clickable { color = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                nameError = name.isBlank()
                if (!nameError) {
                    onSave(
                        Course(
                            id                 = initialCourse?.id ?: "",
                            name               = name.trim(),
                            teacher            = teacher.trim(),
                            description        = description.trim(),
                            color              = color,
                            locationOrPlatform = location.trim(),
                            credits            = credits.toIntOrNull()
                        )
                    )
                }
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

