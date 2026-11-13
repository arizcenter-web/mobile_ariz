package com.tecsup.app_ariz.core.catalog

import com.tecsup.app_ariz.core.catalog.data.mapper.toProjectQuery
import com.tecsup.app_ariz.core.model.common.Currency
import com.tecsup.app_ariz.core.model.project.ProjectFilters
import com.tecsup.app_ariz.core.model.project.ProjectOriginFilter
import com.tecsup.app_ariz.core.model.project.ProjectSortOrder
import com.tecsup.app_ariz.core.model.project.ProjectStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProjectFilterMapperTest {

    @Test
    fun `sin filtros solo envia el orden y no la moneda`() {
        val query = ProjectFilters().toProjectQuery()

        assertEquals("recent", query.sort)
        assertNull(query.origin)
        assertNull(query.stage)
        assertNull(query.currency)
        assertNull(query.priceMax)
    }

    @Test
    fun `filtros completos usan el vocabulario del backend`() {
        val query = ProjectFilters(
            stage = ProjectStage.UNDER_CONSTRUCTION,
            district = "  Tumbes ",
            origin = ProjectOriginFilter.OTHERS,
            currency = Currency.USD,
            priceMax = 200_000,
            sort = ProjectSortOrder.PRICE_DESC,
        ).toProjectQuery()

        assertEquals("UNDER_CONSTRUCTION", query.stage)
        assertEquals("Tumbes", query.district)
        assertEquals("AGENT", query.origin)
        assertEquals("USD", query.currency)
        assertEquals(200_000, query.priceMax)
        assertEquals("price-desc", query.sort)
    }

    @Test
    fun `presupuesto en cero no se envia y arrastra a la moneda`() {
        val query = ProjectFilters(currency = Currency.USD, priceMax = 0, origin = ProjectOriginFilter.ARIZ).toProjectQuery()

        assertNull(query.priceMax)
        assertNull(query.currency)
        assertEquals("ADMIN", query.origin)
    }
}
