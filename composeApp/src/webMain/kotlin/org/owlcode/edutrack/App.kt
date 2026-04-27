package org.owlcode.edutrack

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.koin.compose.viewmodel.koinViewModel
import org.owlcode.edutrack.features.app.AppViewModel
import org.owlcode.edutrack.features.app.CourseDetailRoute
import org.owlcode.edutrack.features.app.Route
import org.owlcode.edutrack.features.calendar.CalendarScreen
import org.owlcode.edutrack.features.courses.CourseDetailScreen
import org.owlcode.edutrack.features.courses.CourseScreen
import org.owlcode.edutrack.features.login.LoginScreen

@Composable
fun App() {
    val navController = rememberNavController()
    val viewModel: AppViewModel = koinViewModel()
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val isInitialized   by viewModel.isInitialized.collectAsState()

    // Mostrar un indicador de carga mientras se comprueba la sesión.
    // Esto evita que el NavHost se cree con startDestination incorrecto
    // y luego se destruya/recree causando pantalla en blanco.
    if (!isInitialized) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Redirigir al login si la sesión expira en background (token rechazado por el servidor).
    // Esto cubre el caso de "abrí la página después de 40 min y el token ya no es válido".
    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated && navController.currentDestination?.route != Route.Login.path) {
            navController.navigate(Route.Login.path) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val startDestination = if (isAuthenticated) Route.Calendar.path else Route.Login.path

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Route.Login.path) {
            LoginScreen(onLoginSuccess = {
                viewModel.onLoggedIn()
                navController.navigate(Route.Calendar.path) { popUpTo(Route.Login.path) { inclusive = true } }
            })
        }
        composable(Route.Calendar.path) {
            CalendarScreen(
                onLogout = {
                    viewModel.onLoggedOut()
                    navController.navigate(Route.Login.path) { popUpTo(Route.Calendar.path) { inclusive = true } }
                },
                onNavigateToCourses = { navController.navigate(Route.Courses.path) }
            )
        }
        composable(Route.Courses.path) {
            CourseScreen(
                onNavigateToCalendar     = { navController.navigate(Route.Calendar.path) { popUpTo(Route.Courses.path) { inclusive = true } } },
                onNavigateToCourseDetail = { courseId -> navController.navigate(CourseDetailRoute(courseId)) }
            )
        }
        composable<CourseDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<CourseDetailRoute>()
            CourseDetailScreen(courseId = route.courseId, onBack = { navController.popBackStack() })
        }
    }
}
