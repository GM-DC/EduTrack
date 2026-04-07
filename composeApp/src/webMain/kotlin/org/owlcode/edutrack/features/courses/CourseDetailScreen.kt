package org.owlcode.edutrack.features.courses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.owlcode.edutrack.features.calendar.components.DeleteConfirmDialog
import org.owlcode.edutrack.features.courses.components.ClaseFormDialog
import org.owlcode.edutrack.features.courses.components.ExamenFormDialog
import org.owlcode.edutrack.features.courses.components.TareaFormDialog
import org.owlcode.edutrack.features.courses.components.toComposeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseId: String,
    onBack: () -> Unit,
    viewModel: CourseDetailViewModel = koinViewModel(parameters = { parametersOf(courseId) })
) {
    val state by viewModel.state.collectAsState()
    val tabs  = listOf("Clases", "Tareas", "Exámenes")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val courseName = state.course?.name ?: "Curso"
                    val courseColor = state.course?.color?.toComposeColor()
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        courseColor?.let { Box(Modifier.size(12.dp).then(Modifier), contentAlignment = Alignment.Center) { Surface(Modifier.size(12.dp), shape = MaterialTheme.shapes.small, color = it) {} } }
                        Text(courseName, fontWeight = FontWeight.SemiBold)
                    }
                },
                navigationIcon = { TextButton(onClick = onBack) { Text("‹") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                when (state.selectedTab) {
                    0 -> viewModel.showCreateClaseForm()
                    1 -> viewModel.showCreateTareaForm()
                    2 -> viewModel.showCreateExamenForm()
                }
            }) { Text("+", style = MaterialTheme.typography.headlineSmall) }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            PrimaryTabRow(selectedTabIndex = state.selectedTab) {
                tabs.forEachIndexed { idx, title ->
                    Tab(selected = state.selectedTab == idx, onClick = { viewModel.selectTab(idx) }, text = { Text(title) })
                }
            }

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                else -> when (state.selectedTab) {
                    0 -> LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (state.clases.isEmpty()) item { EmptyState("No hay clases. Pulsa + para agregar.") }
                        items(state.clases, key = { it.id }) { clase ->
                            ListCard(
                                title    = clase.titulo.ifBlank { "Clase" },
                                subtitle = "${clase.startDate}${if (clase.startTime != null) " · ${clase.startTime}" else ""}",
                                badge    = clase.modalidad.label(),
                                onEdit   = { viewModel.showEditClaseForm(clase) },
                                onDelete = { viewModel.requestDelete(clase.id, "CLASE") }
                            )
                        }
                    }
                    1 -> LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (state.tareas.isEmpty()) item { EmptyState("No hay tareas. Pulsa + para agregar.") }
                        items(state.tareas, key = { it.id }) { tarea ->
                            ListCard(
                                title    = tarea.titulo,
                                subtitle = "Entrega: ${tarea.dueDate}${if (tarea.dueTime != null) " · ${tarea.dueTime}" else ""}",
                                badge    = tarea.estado.label(),
                                onEdit   = { viewModel.showEditTareaForm(tarea) },
                                onDelete = { viewModel.requestDelete(tarea.id, "TAREA") }
                            )
                        }
                    }
                    2 -> LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (state.examenes.isEmpty()) item { EmptyState("No hay exámenes. Pulsa + para agregar.") }
                        items(state.examenes, key = { it.id }) { examen ->
                            ListCard(
                                title    = examen.titulo,
                                subtitle = "${examen.fecha}${if (examen.horaInicio != null) " · ${examen.horaInicio}" else ""}",
                                badge    = examen.estado.label(),
                                onEdit   = { viewModel.showEditExamenForm(examen) },
                                onDelete = { viewModel.requestDelete(examen.id, "EXAMEN") }
                            )
                        }
                    }
                }
            }
        }

        // ── Formularios ───────────────────────────────────────────────────
        state.course?.let { course ->
            if (state.showClaseForm) {
                ClaseFormDialog(initialClase = state.editingClase, courseId = course.id, onDismiss = viewModel::dismissClaseForm, onSave = viewModel::saveClase)
            }
            if (state.showTareaForm) {
                TareaFormDialog(initialTarea = state.editingTarea, courseId = course.id, onDismiss = viewModel::dismissTareaForm, onSave = viewModel::saveTarea)
            }
            if (state.showExamenForm) {
                ExamenFormDialog(initialExamen = state.editingExamen, courseId = course.id, onDismiss = viewModel::dismissExamenForm, onSave = viewModel::saveExamen)
            }
        }

        if (state.pendingDeleteId != null) {
            DeleteConfirmDialog(
                message   = "¿Eliminar este elemento? Esta acción no se puede deshacer.",
                onConfirm = viewModel::confirmDelete,
                onDismiss = viewModel::cancelDelete
            )
        }
    }
}

@Composable
private fun ListCard(title: String, subtitle: String, badge: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer) {
                Text(badge, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) { Text("✏", style = MaterialTheme.typography.labelMedium) }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Text("✕", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun EmptyState(msg: String) = Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
    Text(msg, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

private fun org.owlcode.edutrack.domain.model.ClassMode.label() = when (this) {
    org.owlcode.edutrack.domain.model.ClassMode.PRESENTIAL -> "Presencial"
    org.owlcode.edutrack.domain.model.ClassMode.LIVE       -> "En vivo"
    org.owlcode.edutrack.domain.model.ClassMode.SELF_PACED -> "A tu ritmo"
}
private fun org.owlcode.edutrack.domain.model.TaskStatus.label() = when (this) {
    org.owlcode.edutrack.domain.model.TaskStatus.PENDING     -> "Pendiente"
    org.owlcode.edutrack.domain.model.TaskStatus.IN_PROGRESS -> "En progreso"
    org.owlcode.edutrack.domain.model.TaskStatus.SUBMITTED   -> "Entregada"
    org.owlcode.edutrack.domain.model.TaskStatus.OVERDUE     -> "Vencida"
}
private fun org.owlcode.edutrack.domain.model.ExamStatus.label() = when (this) {
    org.owlcode.edutrack.domain.model.ExamStatus.PENDING     -> "Pendiente"
    org.owlcode.edutrack.domain.model.ExamStatus.TAKEN       -> "Rendido"
    org.owlcode.edutrack.domain.model.ExamStatus.RESCHEDULED -> "Reprogramado"
    org.owlcode.edutrack.domain.model.ExamStatus.CANCELLED   -> "Cancelado"
}


