@file:OptIn(ExperimentalWasmJsInterop::class)

package org.owlcode.edutrack.core.cache

import kotlin.js.ExperimentalWasmJsInterop

import org.owlcode.edutrack.core.config.AppConfig

// ── Interop con localStorage y IndexedDB via @JsFun (Kotlin/WASM) ─────────────

@JsFun("(key) => localStorage.getItem(key) ?? null")
private external fun lsGet(key: String): String?

@JsFun("(key, value) => localStorage.setItem(key, value)")
private external fun lsSet(key: String, value: String)

@JsFun("() => localStorage.length")
private external fun lsLength(): Int

@JsFun("(i) => localStorage.key(i) ?? null")
private external fun lsKey(i: Int): String?

@JsFun("(key) => localStorage.removeItem(key)")
private external fun lsRemove(key: String)

@JsFun("(name) => { try { indexedDB.deleteDatabase(name); } catch(e) {} }")
private external fun idbDelete(name: String)

/**
 * Implementación WASM-JS: borra las claves "edu_*" del localStorage e inicia
 * la eliminación de IndexedDB cuando la BUILD_VERSION cambia.
 */
actual object CacheManager {

    private const val VERSION_KEY = "edu_deploy_version"

    actual suspend fun clearIfOutdated() {
        val stored  = lsGet(VERSION_KEY)
        val current = AppConfig.BUILD_VERSION

        if (stored == current) {
            println("▶ [CacheManager/WASM] Versión vigente ($current). Caché OK.")
            return
        }

        println("▶ [CacheManager/WASM] Nueva versión detectada ($stored → $current). Limpiando caché...")

        // Iniciamos la eliminación de IndexedDB (fire-and-forget, async en el navegador)
        idbDelete("edutrack_db")

        // Borramos todas las claves edu_* del localStorage
        val keysToRemove = (0 until lsLength())
            .mapNotNull { lsKey(it) }
            .filter { it.startsWith("edu_") && it != VERSION_KEY }
        keysToRemove.forEach { lsRemove(it) }

        lsSet(VERSION_KEY, current)
        println("▶ [CacheManager/WASM] Caché limpiada (${keysToRemove.size} claves). Lista para el nuevo despliegue.")
    }
}



