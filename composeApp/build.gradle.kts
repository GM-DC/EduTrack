import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import java.util.Properties

// ── Lectura del archivo .env ──────────────────────────────────────────────────
val envFile = rootProject.file(".env")
val envProps = Properties().apply {
    if (envFile.exists()) envFile.inputStream().use { load(it) }
}
// Prioridad: variable de entorno del sistema > .env > default
fun env(key: String, default: String = ""): String =
    (System.getenv(key) ?: envProps.getProperty(key) ?: default)
        .trim()
// ─────────────────────────────────────────────────────────────────────────────

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    // Habilita expect/actual para clases/objetos (actualmente en Beta en KMP)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

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
    // No usar build cache: los valores pueden venir de variables de entorno del sistema
    outputs.cacheIf { false }
    inputs.file(envFile).optional(true)

    // Capturamos los valores aquí (fase de configuración).
    // Prioridad: variable de entorno del sistema (CI/Railway) > .env (local)
    val apiBaseUrl  = env("API_BASE_URL", "http://localhost:8080")
    val apiVersion  = env("API_VERSION",  "v1")
    val appEnv      = env("APP_ENV",      "development")
    val webHost     = env("WEB_HOST",     "localhost")
    val webPort     = env("WEB_PORT",     "8081").toIntOrNull() ?: 8081
    val logLevel      = env("LOG_LEVEL",    "DEBUG")
    // Versión única por build (epoch seconds). Cambia en cada deploy.
    // En Railway siempre es fresco gracias a --no-configuration-cache.
    val buildVersion  = (System.currentTimeMillis() / 1000).toString()

    // Declaramos los valores como inputs para que el configuration cache
    // se invalide si cambia el .env o las variables de entorno
    inputs.property("apiBaseUrl",   apiBaseUrl)
    inputs.property("appEnv",       appEnv)
    inputs.property("apiVersion",   apiVersion)
    inputs.property("logLevel",     logLevel)
    inputs.property("buildVersion", buildVersion)

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

                /** Versión única de compilación (epoch seconds). Usada para invalidar caché. */
                const val BUILD_VERSION: String = "$buildVersion"
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