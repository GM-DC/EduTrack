package org.owlcode.edutrack

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.owlcode.edutrack.core.cache.CacheManager
import org.owlcode.edutrack.features.app.di.appModule

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    MainScope().launch {
        // Antes de arrancar la app, detecta si hay un nuevo despliegue
        // y limpia IndexedDB + localStorage en caso afirmativo.
        CacheManager.clearIfOutdated()
        startApp()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun startApp() {
    startKoin {
        modules(appModule)
    }
    ComposeViewport {
        App()
    }
}