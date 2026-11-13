package com.tecsup.app_ariz.core.network

import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import retrofit2.HttpException
import java.io.IOException

/** Envuelve una llamada Retrofit y traduce excepciones HTTP/IO a [AppError]. */
suspend inline fun <T> safeApiCall(crossinline call: suspend () -> T): AppResult<T> =
    try {
        AppResult.Success(call())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        AppResult.Failure(e.toAppError())
    } catch (e: IOException) {
        AppResult.Failure(AppError.Network())
    } catch (e: Exception) {
        AppResult.Failure(AppError.Unknown())
    }

@PublishedApi
internal fun HttpException.toAppError(): AppError {
    // NestJS responde { statusCode, message: string | string[], error }.
    val serverMessage = runCatching {
        val body = response()?.errorBody()?.string().orEmpty()
        val obj: JsonObject = Json.parseToJsonElement(body).jsonObject
        when (val m = obj["message"]) {
            is JsonPrimitive -> m.content
            null -> null
            else -> m.toString().trim('[', ']').replace("\"", "")
        }
    }.getOrNull()

    return when (code()) {
        400, 422 -> AppError.Validation(serverMessage ?: "Datos inválidos")
        401 -> AppError.Unauthorized(serverMessage ?: "Sesión requerida")
        403 -> AppError.Forbidden(serverMessage ?: "No tienes permisos")
        404 -> AppError.NotFound(serverMessage ?: "No encontrado")
        429 -> AppError.TooManyRequests()
        503 -> AppError.ServiceUnavailable(serverMessage ?: "Servicio no disponible, intenta de nuevo")
        else -> AppError.Unknown(serverMessage ?: "Error ${code()}")
    }
}
