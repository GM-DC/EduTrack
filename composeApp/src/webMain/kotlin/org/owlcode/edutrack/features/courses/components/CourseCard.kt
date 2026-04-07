package org.owlcode.edutrack.features.courses.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.owlcode.edutrack.domain.model.Course

/** Convierte un String hex (`"#4A90D9"`) a `Color` de Compose. */
fun String.toComposeColor(): Color = runCatching {
    val hex   = removePrefix("#")
    val value = hex.toLong(16)
    val argb  = if (hex.length == 6) 0xFF000000L or value else value
    Color(argb.toInt())
}.getOrDefault(Color(0xFF4A90D9))

@Composable
fun CourseCard(
    course: Course,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val courseColor = course.color.toComposeColor()

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier              = Modifier.padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Barra de color ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(courseColor)
            )

            // ── Info del curso ──────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = course.name,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (course.teacher.isNotBlank()) {
                    Text(
                        text  = course.teacher,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (course.description.isNotBlank()) {
                    Text(
                        text     = course.description,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            // ── Acciones ────────────────────────────────────────────────
            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Text("✏", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Text("✕", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
