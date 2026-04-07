package org.owlcode.edutrack.core.result

sealed class AppError(open val message: String) {
    data class Network(override val message: String) : AppError(message)
    data class Local(override val message: String) : AppError(message)
    data class Auth(override val message: String) : AppError(message)
    data class Unknown(override val message: String) : AppError(message)
}

