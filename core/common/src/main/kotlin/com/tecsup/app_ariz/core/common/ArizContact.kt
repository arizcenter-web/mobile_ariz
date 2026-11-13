package com.tecsup.app_ariz.core.common

/**
 * Datos de contacto de la empresa, los mismos del web (`shared/lib/whatsapp.ts`). Los usan las
 * pantallas que ofrecen "Hablar con un asesor" (inversiones, servicios, contacto).
 */
object ArizContact {
    /** Número de ARIZ en formato internacional de `wa.me`. */
    const val WHATSAPP_DIGITS = "51958283952"

    /** Enlace de WhatsApp con un mensaje ya escrito (el texto se codifica para la URL). */
    fun whatsappUrl(message: String): String =
        "https://wa.me/$WHATSAPP_DIGITS?text=" + java.net.URLEncoder.encode(message, "UTF-8").replace("+", "%20")
}
