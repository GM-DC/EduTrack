package org.owlcode.edutrack.core.cache

import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import org.owlcode.edutrack.core.config.AppConfig

/**
 * Implementación JS: borra IndexedDB completa + claves "edu_*" del localStorage
 * cuando la BUILD_VERSION cambia (nuevo despliegue).
 */
actual object CacheManager {

    private const val VERSION_KEY = "edu_deploy_version"
    private const val DB_NAME     = "edutrack_db"

    actual suspend fun clearIfOutdated() {
        val stored  = localStorage.getItem(VERSION_KEY)
        val current = AppConfig.BUILD_VERSION

        if (stored == current) {
            println("▶ [CacheManager] Versión vigente ($current). Caché OK.")
            return
        }

        println("▶ [CacheManager] Nueva versión detectada ($stored → $current). Limpiando caché...")
        deleteIndexedDb()
        clearLocalStorage()
        localStorage.setItem(VERSION_KEY, current)
        println("▶ [CacheManager] Caché limpiada. Lista para el nuevo despliegue.")
    }

    /** Elimina la base de datos IndexedDB completa (incluye todos los stores). */
    private suspend fun deleteIndexedDb(): Unit = suspendCoroutine { cont ->
        try {
            val req = window.asDynamic().indexedDB.deleteDatabase(DB_NAME)
            req.onsuccess = { _: dynamic -> cont.resume(Unit) }
            req.onerror   = { _: dynamic -> cont.resume(Unit) }
            req.onblocked = { _: dynamic ->
                // Otra pestaña tiene la BD abierta; se borrará al cerrarla.
                println("▶ [CacheManager] IndexedDB bloqueada. Se eliminará al cerrar la otra pestaña.")
                cont.resume(Unit)
            }
        } catch (e: Throwable) {
            println("▶ [CacheManager] Error eliminando IndexedDB: ${e.message}")
            cont.resume(Unit)
        }
    }

    /** Elimina las claves "edu_*" del localStorage (datos de la app, no configuración del navegador). */
    private fun clearLocalStorage() {
        val keysToRemove = (0 until localStorage.length)
            .mapNotNull { localStorage.key(it) }
            .filter { it.startsWith("edu_") && it != VERSION_KEY }
        keysToRemove.forEach { localStorage.removeItem(it) }
        println("▶ [CacheManager] localStorage limpiado (${keysToRemove.size} claves eliminadas).")
    }
}

