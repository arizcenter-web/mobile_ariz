package com.tecsup.app_ariz.core.common

/**
 * Errores de dominio que la UI puede mostrar. Mapean los codigos que devuelve el backend NestJS:
 * 401 (sesion), 403 (rol), 404, 422/400 (validacion), 429 (throttler) y 503 (BD inalcanzable).
 */
sealed class AppError(open val message: String) {
    data class Unauthorized(override val message: String = "Sesión requerida") : AppError(message)
    data class Forbidden(override val message: String = "No tienes permisos") : AppError(message)
    data class NotFound(override val message: String = "No encontrado") : AppError(message)
    data class Validation(override val message: String) : AppError(message)
    data class TooManyRequests(override val message: String = "Demasiados intentos, espera un momento") : AppError(message)
    data class ServiceUnavailable(override val message: String = "Servicio no disponible, intenta de nuevo") : AppError(message)
    data class Network(override val message: String = "Sin conexión") : AppError(message)
    /** El usuario canceló una acción interactiva (p. ej. el selector de cuentas de Google). No se muestra. */
    data object Cancelled : AppError("Cancelado")
    data class Unknown(override val message: String = "Ocurrió un error inesperado") : AppError(message)
}
