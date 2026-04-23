import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import java.util.Properties

// ── Lectura del archivo .env ──────────────────────────────────────────────────
val envFile = rootProject.file(".env")
val envProps = Properties().apply {
    if (envFile.exists()) envFile.inputStream().use { load(it) }
}
fun env(key: String, default: String = ""): String =
    (envProps.getProperty(key) ?: System.getenv(key) ?: default)
        .trim()
        .removePrefix("#.*".toRegex().find("")?.value ?: "")  // ignora comentarios inline
// ─────────────────────────────────────────────────────────────────────────────

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    js {
        browser()
        binaries.executable()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    
    sourceSets {
        // ── Fuentes generadas (AppConfig) ─────────────────────────────────────
        val generatedDir = layout.buildDirectory.dir("generated/kotlin/commonMain").get().asFile
        commonMain {
            kotlin.srcDir(generatedDir)
        }
        // ─────────────────────────────────────────────────────────────────────
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        val jsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }
    }
}

// ── Tarea: genera AppConfig.kt con valores del .env ──────────────────────────
val generateAppConfig by tasks.registering {
    group = "build"
    description = "Genera AppConfig.kt a partir del archivo .env"

    val outputDir = layout.buildDirectory.dir("generated/kotlin/commonMain/org/owlcode/edutrack/core/config")
    outputs.dir(outputDir)
    inputs.file(envFile).optional(true)

    // Capturamos los valores aquí (fase de configuración) para ser compatibles
    // con el configuration cache de Gradle
    val apiBaseUrl  = env("API_BASE_URL", "http://localhost:8080")
    val apiVersion  = env("API_VERSION",  "v1")
    val appEnv      = env("APP_ENV",      "development")
    val webHost     = env("WEB_HOST",     "localhost")
    val webPort     = env("WEB_PORT",     "8081").toIntOrNull() ?: 8081
    val logLevel    = env("LOG_LEVEL",    "DEBUG")

    doLast {
        val dir = outputDir.get().asFile.also { it.mkdirs() }
        dir.resolve("AppConfig.kt").writeText(
            """
            // ⚠️ Archivo generado automáticamente — no editar a mano
            // Fuente: .env  |  Generado por Gradle
            package org.owlcode.edutrack.core.config

            object AppConfig {
                /** URL base de la API REST */
                const val API_BASE_URL: String = "$apiBaseUrl"

                /** Versión de la API */
                const val API_VERSION: String = "$apiVersion"

                /** Entorno de ejecución: development | staging | production */
                const val APP_ENV: String = "$appEnv"

                /** Host del servidor web frontend */
                const val WEB_HOST: String = "$webHost"

                /** Puerto del servidor web frontend */
                const val WEB_PORT: Int = $webPort

                /** Nivel de logging */
                const val LOG_LEVEL: String = "$logLevel"

                /** Indica si la app corre en modo desarrollo */
                val isDevelopment: Boolean get() = APP_ENV == "development"

                /** URL completa del endpoint base de la API */
                val apiBaseEndpoint: String get() = "${'$'}API_BASE_URL/api"
            }
            """.trimIndent()
        )
    }
}

// Asegura que el código se genere antes de compilar
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn(generateAppConfig)
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile>().configureEach {
    dependsOn(generateAppConfig)
}
// ─────────────────────────────────────────────────────────────────────────────

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon>().configureEach {
    dependsOn("generateAppConfig")
}