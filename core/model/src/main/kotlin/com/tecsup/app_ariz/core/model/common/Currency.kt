package com.tecsup.app_ariz.core.model.common

/** Moneda de los precios del backend (`PEN` soles, `USD` dólares). */
enum class Currency(val symbol: String) {
    PEN("S/"),
    USD("$"),
}
