package org.owlcode.edutrack.core.di

import io.ktor.client.*
import org.koin.dsl.module
import org.owlcode.edutrack.core.database.StorageDriver
import org.owlcode.edutrack.core.database.createStorageDriver
import org.owlcode.edutrack.core.network.createHttpClient

val coreModule = module {
    single<HttpClient> { createHttpClient() }
    single<StorageDriver> { createStorageDriver() }
}

