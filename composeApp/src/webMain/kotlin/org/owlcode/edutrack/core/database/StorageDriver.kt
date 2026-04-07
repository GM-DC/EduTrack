package org.owlcode.edutrack.core.database

interface StorageDriver {
    suspend fun put(storeName: String, key: String, value: String)
    suspend fun get(storeName: String, key: String): String?
    suspend fun delete(storeName: String, key: String)
    suspend fun getAll(storeName: String): List<String>
    suspend fun clear(storeName: String)
}

