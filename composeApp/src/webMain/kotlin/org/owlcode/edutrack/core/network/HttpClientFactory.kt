package org.owlcode.edutrack.core.network

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.owlcode.edutrack.core.config.AppConfig

fun createHttpClient(): HttpClient = HttpClient {
    // URL base tomada del .env (inyectada en tiempo de compilación)
    defaultRequest {
        url(AppConfig.API_BASE_URL + "/")
    }

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        })
    }

    install(Logging) {
        level = if (AppConfig.isDevelopment) LogLevel.BODY else LogLevel.INFO
    }
}

