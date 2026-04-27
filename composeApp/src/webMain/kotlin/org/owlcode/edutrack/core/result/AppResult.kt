package org.owlcode.edutrack.core.result

import io.ktor.client.plugins.*
import io.ktor.http.*

sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val error: AppError) : AppResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? = (this as? Success)?.data
    fun errorOrNull(): AppError? = (this as? Error)?.error
}

inline fun <T> AppResult<T>.onSuccess(block: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) block(data)
    return this
}

inline fun <T> AppResult<T>.onError(block: (AppError) -> Unit): AppResult<T> {
    if (this is AppResult.Error) block(error)
    return this
}

suspend fun <T> safeApiCall(block: suspend () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (e: kotlinx.coroutines.CancellationException) {
    // Nunca atrapar CancellationException — es cómo Kotlin cancela corrutinas
    throw e
} catch (e: ResponseException) {
    // Errores HTTP con código de estado: distinguir 401 de otros errores de red
    if (e.response.status == HttpStatusCode.Unauthorized) {
        AppResult.Error(AppError.Auth("Sesión expirada. Por favor, inicia sesión nuevamente."))
    } else {
        AppResult.Error(AppError.Network("Error del servidor: ${e.response.status.value}"))
    }
} catch (e: Throwable) {
    // Throwable (no solo Exception) para capturar también errores JS nativos
    // como DOMException de IndexedDB, que no son instancias de kotlin.Exception
    AppResult.Error(AppError.Network(e.message ?: "Error de red desconocido"))
}

