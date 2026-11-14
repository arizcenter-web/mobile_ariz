package com.tecsup.app_ariz.feature.admin

import com.tecsup.app_ariz.feature.admin.domain.AnalyticsProject
import com.tecsup.app_ariz.feature.admin.domain.AnalyticsRaw
import com.tecsup.app_ariz.feature.admin.domain.AnalyticsRow
import com.tecsup.app_ariz.feature.admin.domain.ProfileFilter
import com.tecsup.app_ariz.feature.admin.domain.RangeFilter
import com.tecsup.app_ariz.feature.admin.domain.buildAnalytics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

class AnalyticsTest {
    private val today = YearMonth.of(2026, 11)

    private val raw = AnalyticsRaw(
        professionals = listOf(
            AnalyticsRow("2026-11-02", "REAL_ESTATE_AGENCY"),
            AnalyticsRow("2026-10-15", "INDEPENDENT_AGENT"),
            AnalyticsRow("2025-01-01", "DEVELOPER"), // fuera de la ventana
        ),
        properties = listOf(AnalyticsRow("2026-11-05", "INDEPENDENT_AGENT"), AnalyticsRow("2026-11-06", "DEVELOPER")),
        projects = listOf(
            AnalyticsProject(AnalyticsRow("2026-11-01", "DEVELOPER"), "Lomas", 40),
            AnalyticsProject(AnalyticsRow("2026-09-01", "REAL_ESTATE_AGENCY"), "Cerro", 90),
            AnalyticsProject(AnalyticsRow("2026-09-01", "REAL_ESTATE_AGENCY"), "Sin lotes", 0),
        ),
        subscriptions = listOf(AnalyticsRow("2026-11-09", "REAL_ESTATE_AGENCY")),
    )

    @Test
    fun `agrupa por mes con los meses de la ventana y etiquetas en espanol`() {
        val view = buildAnalytics(raw, ProfileFilter.ALL, RangeFilter.SIX, today)
        assertEquals(listOf("jun", "jul", "ago", "sep", "oct", "nov"), view.accounts.map { it.label })
        assertEquals(listOf(0, 0, 0, 0, 1, 1), view.accounts.map { it.value })
        assertEquals(listOf(0, 0, 0, 2, 0, 1), view.projects.map { it.value })
        // Publicaciones = propiedades + proyectos
        assertEquals(listOf(0, 0, 0, 2, 0, 3), view.publications.map { it.value })
    }

    @Test
    fun `inmobiliarias incluye desarrolladoras en publicaciones pero no en cuentas`() {
        val view = buildAnalytics(raw, ProfileFilter.AGENCIES, RangeFilter.SIX, today)
        assertEquals(1, view.accounts.last().value) // solo la inmobiliaria de noviembre
        assertEquals(1, view.properties.last().value) // la propiedad de la desarrolladora
        assertEquals(1, view.projects.last().value)
    }

    @Test
    fun `usuarios no tiene cuentas profesionales`() {
        val view = buildAnalytics(raw, ProfileFilter.USERS, RangeFilter.SIX, today)
        assertTrue(view.isEmpty)
    }

    @Test
    fun `sin fechas usables se muestra el total en el mes actual`() {
        val noDates = AnalyticsRaw(properties = listOf(AnalyticsRow(null, null), AnalyticsRow(null, null)))
        val view = buildAnalytics(noDates, ProfileFilter.ALL, RangeFilter.ONE, today)
        assertEquals(listOf(2), view.properties.map { it.value })
    }

    @Test
    fun `el top ordena por lotes, ignora los vacios y limita a 10`() {
        val many = AnalyticsRaw(projects = (1..12).map { AnalyticsProject(AnalyticsRow("2026-11-01", null), "P$it", it) } + AnalyticsProject(AnalyticsRow(null, null), "Vacio", 0))
        val top = buildAnalytics(many, ProfileFilter.ALL, RangeFilter.SIX, today).topProjects
        assertEquals(10, top.size)
        assertEquals("P12", top.first().name)
        assertTrue(top.none { it.lots == 0 })
    }
}
