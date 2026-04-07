package org.owlcode.edutrack.features.courses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.owlcode.edutrack.features.courses.components.CourseCard
import org.owlcode.edutrack.features.courses.components.CourseFormDialog
import org.owlcode.edutrack.features.calendar.components.DeleteConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToCourseDetail: (String) -> Unit = {},
    viewModel: CourseViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title   = { Text("Cursos") },
                actions = {
                    TextButton(onClick = onNavigateToCalendar) { Text("Agenda") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::showCreateForm) {
                Text("+", style = MaterialTheme.typography.headlineSmall)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                state.error != null -> Column(
                    modifier            = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = viewModel::loadCourses) { Text("Reintentar") }
                }

                state.courses.isEmpty() -> Text(
                    text     = "No hay cursos. ¡Crea el primero!",
                    modifier = Modifier.align(Alignment.Center),
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )

                else -> LazyColumn(
                    modifier       = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(state.courses, key = { it.id }) { course ->
                        CourseCard(
                            course   = course,
                            onClick  = { onNavigateToCourseDetail(course.id) },
                            onEdit   = { viewModel.showEditForm(course) },
                            onDelete = { viewModel.requestDelete(course.id) }
                        )
                    }
                }
            }
        }

        // ── Diálogo crear / editar curso ──────────────────────────────
        if (state.showForm) {
            CourseFormDialog(
                initialCourse = state.editingCourse,
                onDismiss     = viewModel::dismissForm,
                onSave        = viewModel::saveCourse
            )
        }

        // ── Diálogo confirmar borrado ─────────────────────────────────
        if (state.pendingDeleteId != null) {
            DeleteConfirmDialog(
                message   = "¿Eliminar este curso? Esta acción no se puede deshacer.",
                onConfirm = viewModel::confirmDelete,
                onDismiss = viewModel::cancelDelete
            )
        }
    }
}

