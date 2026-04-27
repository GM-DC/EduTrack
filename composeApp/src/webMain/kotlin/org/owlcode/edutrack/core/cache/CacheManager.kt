package org.owlcode.edutrack.core.cache

/**
 * Gestiona la invalidación de caché entre despliegues.
 * Implementado con expect/actual según la plataforma (JS o WASM-JS).
 */
expect object CacheManager {
    /** Limpia IndexedDB / localStorage si el BUILD_VERSION cambió. */
    suspend fun clearIfOutdated()
}


