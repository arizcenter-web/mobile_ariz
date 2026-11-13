package com.tecsup.app_ariz.core.common

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Convierte la fecha ISO que envía el backend (`toISOString()`: UTC con milisegundos) a milisegundos
 * desde epoch. `java.time` no existe en API 24, por eso se usa `SimpleDateFormat`.
 */
fun String?.isoToEpochMillis(): Long? {
    if (isNullOrBlank()) return null
    return runCatching {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .parse(this)
            ?.time
    }.getOrNull()
}
