package org.owlcode.edutrack.core.network

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.owlcode.edutrack.core.config.AppConfig

fun createHttpClient(): HttpClient {
    // ✅ Log de diagnóstico — debe mostrar la URL de Railway, nunca localhost
    println("▶ [HttpClient] API_BASE_URL = ${AppConfig.API_BASE_URL}")
    println("▶ [HttpClient] APP_ENV      = ${AppConfig.APP_ENV}")

    return HttpClient {
        // URL base tomada del .env (inyectada en tiempo de compilación)
        defaultRequest {
            url(AppConfig.API_BASE_URL + "/")
        }

        // Evita que las peticiones queden colgadas indefinidamente
        // (crítico cuando Railway entra en "sleep" o el token expiró)
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis  = 15_000
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
}

