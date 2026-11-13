package com.tecsup.app_ariz.core.ui.format

import com.tecsup.app_ariz.core.model.common.Currency
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

private val PeruLocale = Locale("es", "PE")
private const val DayMillis = 86_400_000L

/** `formatPrice` del web (`shared/lib/format.ts`): símbolo + entero con separador de miles. Ej. `S/ 250,000`. */
fun formatPrice(amount: Double, currency: Currency): String =
    "${currency.symbol} ${NumberFormat.getIntegerInstance(PeruLocale).format(amount)}"

/** `formatArea` del web: vacío si no hay área. */
fun formatArea(areaM2: Double): String =
    if (areaM2 > 0) "${NumberFormat.getIntegerInstance(PeruLocale).format(areaM2)} m²" else ""

/** Fecha de publicación lista para mostrar, como `formatPublishedDate` del web. */
data class PublishedLabel(val relative: String, val isNew: Boolean)

fun publishedLabel(publishedAtMillis: Long?, nowMillis: Long = System.currentTimeMillis()): PublishedLabel? {
    if (publishedAtMillis == null) return null
    val days = maxOf(0L, (nowMillis - publishedAtMillis) / DayMillis)
    val relative = when {
        days == 0L -> "Publicado hoy"
        days < 7 -> "hace $days ${if (days == 1L) "día" else "días"}"
        days <= 30 -> {
            val weeks = maxOf(1L, days / 7)
            "hace $weeks ${if (weeks == 1L) "semana" else "semanas"}"
        }
        else -> SimpleDateFormat("d MMM yyyy", PeruLocale).format(publishedAtMillis).replace(".", "")
    }
    return PublishedLabel(relative = relative, isNew = days < 7)
}

/** "Publicado el 12 de noviembre de 2026", como `fullWithPrefix` del web. */
fun publishedFullLabel(publishedAtMillis: Long?): String? {
    if (publishedAtMillis == null) return null
    return "Publicado el " + SimpleDateFormat("d 'de' MMMM 'de' yyyy", PeruLocale).format(publishedAtMillis)
}

/** "noviembre de 2026" (mes y año), como `formatMonthYear` del detalle de proyecto del web. */
fun formatMonthYear(millis: Long?): String? {
    if (millis == null) return null
    return SimpleDateFormat("MMMM 'de' yyyy", PeruLocale).format(millis)
}
