package com.tecsup.app_ariz.core.catalog

import com.tecsup.app_ariz.core.catalog.data.mapper.toDomain
import com.tecsup.app_ariz.core.catalog.data.remote.AreaRangeDto
import com.tecsup.app_ariz.core.catalog.data.remote.LocationDto
import com.tecsup.app_ariz.core.catalog.data.remote.PriceDto
import com.tecsup.app_ariz.core.catalog.data.remote.ProjectDto
import com.tecsup.app_ariz.core.catalog.data.remote.PropertyDto
import com.tecsup.app_ariz.core.common.isoToEpochMillis
import com.tecsup.app_ariz.core.model.common.Currency
import com.tecsup.app_ariz.core.model.project.ProjectCategory
import com.tecsup.app_ariz.core.model.project.ProjectStage
import com.tecsup.app_ariz.core.model.property.ListingOperation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CatalogMappersTest {

    @Test
    fun `propiedad en alquiler en dolares usa la portada y el distrito`() {
        val dto = PropertyDto(
            id = "1", slug = "depa", title = "Depa", operation = "rent",
            price = PriceDto(amount = 800.0, currency = "USD"),
            location = LocationDto(department = "Tumbes", district = "Zarumilla"),
            images = listOf("a.jpg", "b.jpg"), rooms = 2, bathrooms = 1, areaM2 = 70.0,
        )

        val property = dto.toDomain()

        assertEquals(ListingOperation.RENT, property.operation)
        assertEquals(Currency.USD, property.currency)
        assertEquals("a.jpg", property.coverImageUrl)
        assertEquals("Zarumilla", property.district)
        assertEquals(2, property.bedrooms)
    }

    @Test
    fun `operacion desconocida cae en OTHER y moneda desconocida en soles`() {
        val property = PropertyDto(
            id = "1", slug = "x", title = "X", operation = "invest", price = PriceDto(1.0, "EUR"),
        ).toDomain()

        assertEquals(ListingOperation.OTHER, property.operation)
        assertEquals(Currency.PEN, property.currency)
    }

    @Test
    fun `proyecto usa totalLots y si no lotsCount`() {
        val withTotal = ProjectDto(id = "1", slug = "p", name = "P", totalLots = 40, lotsCount = 3).toDomain()
        val withoutTotal = ProjectDto(
            id = "2", slug = "q", name = "Q", totalLots = null, lotsCount = 3,
            stage = "DELIVERED", category = "DEPARTAMENTOS", areaRange = AreaRangeDto(min = 55.0),
        ).toDomain()

        assertEquals(40, withTotal.totalUnits)
        assertEquals(3, withoutTotal.totalUnits)
        assertEquals(ProjectStage.DELIVERED, withoutTotal.stage)
        assertEquals(ProjectCategory.DEPARTAMENTOS, withoutTotal.category)
        assertEquals(55.0, withoutTotal.minAreaM2, 0.0)
    }

    @Test
    fun `fecha ISO del backend se convierte a milisegundos`() {
        assertEquals(1_700_000_000_000L, "2023-11-14T22:13:20.000Z".isoToEpochMillis())
        assertNull("no es fecha".isoToEpochMillis())
        assertNull(null.isoToEpochMillis())
    }
}
