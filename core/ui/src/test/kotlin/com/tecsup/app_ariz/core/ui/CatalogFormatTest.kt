package com.tecsup.app_ariz.core.ui

import com.tecsup.app_ariz.core.model.common.Currency
import com.tecsup.app_ariz.core.ui.format.formatArea
import com.tecsup.app_ariz.core.ui.format.formatPrice
import com.tecsup.app_ariz.core.ui.format.publishedLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogFormatTest {

    private val day = 86_400_000L
    private val now = 1_700_000_000_000L

    @Test
    fun `precio con simbolo y separador de miles`() {
        assertEquals("S/ 250,000", formatPrice(250_000.0, Currency.PEN))
        assertEquals("$ 1,200", formatPrice(1_200.0, Currency.USD))
    }

    @Test
    fun `area vacia si no hay medida`() {
        assertEquals("", formatArea(0.0))
        assertEquals("120 m²", formatArea(120.0))
    }

    @Test
    fun `hoy y dias recientes se marcan como nuevos`() {
        assertEquals("Publicado hoy", publishedLabel(now - 1_000, now)?.relative)
        assertEquals("hace 1 día", publishedLabel(now - day, now)?.relative)
        assertTrue(publishedLabel(now - 3 * day, now)!!.isNew)
    }

    @Test
    fun `semanas y fechas antiguas no son nuevas`() {
        assertEquals("hace 2 semanas", publishedLabel(now - 14 * day, now)?.relative)
        assertFalse(publishedLabel(now - 14 * day, now)!!.isNew)
        assertFalse(publishedLabel(now - 90 * day, now)!!.isNew)
    }

    @Test
    fun `sin fecha no hay etiqueta`() {
        assertNull(publishedLabel(null, now))
    }
}
