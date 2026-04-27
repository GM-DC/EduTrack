package org.owlcode.edutrack.core.database

import kotlinx.browser.window
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class IndexedDbStorageDriver(
    private val dbName: String = "edutrack_db",
    // versión 2: añade los stores que faltaban (courses, clases, tareas, examenes, personales)
    private val dbVersion: Int = 2,
    private val storeNames: List<String> = listOf(
        "auth", "courses", "clases", "tareas", "examenes", "personales", "calendar"
    )
) : StorageDriver {

    private var database: dynamic = null

    private suspend fun db(): dynamic {
        if (database != null) return database
        return suspendCoroutine { cont ->
            val request = window.asDynamic().indexedDB.open(dbName, dbVersion)
            request.onupgradeneeded = { event: dynamic ->
                val db = event.target.result
                storeNames.forEach { name ->
                    if (!db.objectStoreNames.contains(name)) {
                        db.createObjectStore(name)
                    }
                }
            }
            request.onsuccess = { event: dynamic ->
                database = event.target.result
                cont.resume(database)
            }
            request.onerror = { _: dynamic ->
                cont.resumeWithException(Exception("No se pudo abrir IndexedDB: $dbName"))
            }
        }
    }

    override suspend fun put(storeName: String, key: String, value: String) {
        val db = db()
        suspendCoroutine<Unit> { cont ->
            val req = db.transaction(storeName, "readwrite").objectStore(storeName).put(value, key)
            req.onsuccess = { _: dynamic -> cont.resume(Unit) }
            req.onerror  = { _: dynamic -> cont.resumeWithException(Exception("put() falló: $key")) }
        }
    }

    override suspend fun get(storeName: String, key: String): String? {
        val db = db()
        return suspendCoroutine { cont ->
            val req = db.transaction(storeName, "readonly").objectStore(storeName).get(key)
            req.onsuccess = { event: dynamic -> cont.resume(event.target.result as? String) }
            req.onerror  = { _: dynamic -> cont.resumeWithException(Exception("get() falló: $key")) }
        }
    }

    override suspend fun delete(storeName: String, key: String) {
        val db = db()
        suspendCoroutine<Unit> { cont ->
            val req = db.transaction(storeName, "readwrite").objectStore(storeName).delete(key)
            req.onsuccess = { _: dynamic -> cont.resume(Unit) }
            req.onerror  = { _: dynamic -> cont.resumeWithException(Exception("delete() falló: $key")) }
        }
    }

    override suspend fun getAll(storeName: String): List<String> {
        val db = db()
        return suspendCoroutine { cont ->
            val req = db.transaction(storeName, "readonly").objectStore(storeName).getAll()
            req.onsuccess = { event: dynamic ->
                val result = event.target.result
                val list = (0 until (result.length as Int)).mapNotNull { result[it] as? String }
                cont.resume(list)
            }
            req.onerror = { _: dynamic -> cont.resumeWithException(Exception("getAll() falló: $storeName")) }
        }
    }

    override suspend fun clear(storeName: String) {
        val db = db()
        suspendCoroutine<Unit> { cont ->
            val req = db.transaction(storeName, "readwrite").objectStore(storeName).clear()
            req.onsuccess = { _: dynamic -> cont.resume(Unit) }
            req.onerror  = { _: dynamic -> cont.resumeWithException(Exception("clear() falló: $storeName")) }
        }
    }
}

