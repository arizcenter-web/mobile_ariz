package com.tecsup.app_ariz.core.catalog

import com.tecsup.app_ariz.core.catalog.data.mapper.toQuery
import com.tecsup.app_ariz.core.model.property.ListingOperation
import com.tecsup.app_ariz.core.model.property.PropertyFilters
import com.tecsup.app_ariz.core.model.property.PropertySortOrder
import com.tecsup.app_ariz.core.model.property.PropertyTypeFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FilterMapperTest {

    @Test
    fun `sin filtros pide ventas recientes`() {
        val query = PropertyFilters().toQuery()

        assertEquals("SALE", query.operationType)
        assertEquals("recent", query.sort)
        assertNull(query.propertyType)
        assertNull(query.district)
    }

    @Test
    fun `filtros completos usan el vocabulario del backend`() {
        val query = PropertyFilters(
            operation = ListingOperation.RENT,
            type = PropertyTypeFilter.APARTMENT,
            district = "  Tumbes ",
            priceMin = 500,
            priceMax = 2_000,
            roomsMin = 2,
            areaMin = 60,
            sort = PropertySortOrder.PRICE_DESC,
        ).toQuery()

        assertEquals("RENT", query.operationType)
        assertEquals("APARTMENT", query.propertyType)
        assertEquals("Tumbes", query.district)
        assertEquals(500, query.priceMin)
        assertEquals(2, query.bedroomsMin)
        assertEquals("price-desc", query.sort)
    }

    @Test
    fun `valores en cero o distrito vacio no se envian porque el backend exige minimo 1`() {
        val query = PropertyFilters(district = "   ", priceMin = 0, roomsMin = 0, areaMin = 0).toQuery()

        assertNull(query.district)
        assertNull(query.priceMin)
        assertNull(query.bedroomsMin)
        assertNull(query.areaMin)
    }
}
