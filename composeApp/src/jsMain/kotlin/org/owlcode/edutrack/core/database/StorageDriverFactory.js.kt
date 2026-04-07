package org.owlcode.edutrack.core.database

actual fun createStorageDriver(): StorageDriver = IndexedDbStorageDriver()

