package com.tecsup.app_ariz.core.model.common

/** Resultado paginado genérico de los listados públicos. */
data class Page<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val hasMore: Boolean,
)
