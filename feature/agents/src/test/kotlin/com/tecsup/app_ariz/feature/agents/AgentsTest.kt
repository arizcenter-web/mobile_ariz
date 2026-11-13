package com.tecsup.app_ariz.feature.agents

import androidx.lifecycle.SavedStateHandle
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.model.common.Currency
import com.tecsup.app_ariz.core.model.property.ListingOperation
import com.tecsup.app_ariz.feature.agents.data.mapper.toDomain
import com.tecsup.app_ariz.feature.agents.data.remote.dto.AgentProfileDto
import com.tecsup.app_ariz.feature.agents.data.remote.dto.AgentPropertyDto
import com.tecsup.app_ariz.feature.agents.data.remote.dto.LocationDto
import com.tecsup.app_ariz.feature.agents.data.remote.dto.PriceDto
import com.tecsup.app_ariz.feature.agents.domain.model.AgentProfile
import com.tecsup.app_ariz.feature.agents.domain.repository.AgentsRepository
import com.tecsup.app_ariz.feature.agents.domain.usecase.GetAgentProfileUseCase
import com.tecsup.app_ariz.feature.agents.ui.profile.AgentProfileAction
import com.tecsup.app_ariz.feature.agents.ui.profile.AgentProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private fun dto(
    name: String = "Rodrigo Salva",
    status: String = "APPROVED",
    whatsapp: String? = "51999888777",
    phone: String? = "987654321",
    districts: String? = "Zarumilla",
    website: String? = null,
    properties: List<AgentPropertyDto> = emptyList(),
) = AgentProfileDto(
    id = "a1", name = name, status = status, whatsapp = whatsapp, phone = phone, email = "a@correo.com",
    city = "Tumbes", districts = districts, website = website, properties = properties,
)

class AgentProfileTest {

    @Test
    fun `whatsapp se formatea sin el prefijo 51 y agrupado de a 3`() {
        val profile = dto().toDomain()

        assertEquals("51999888777", profile.whatsappDigits)
        assertEquals("+51 999 888 777", profile.whatsappDisplay)
    }

    @Test
    fun `sin whatsapp se usa el telefono, y sin ninguno no hay contacto`() {
        assertEquals("987654321", dto(whatsapp = null).toDomain().whatsappDigits)
        assertNull(dto(whatsapp = null, phone = null).toDomain().whatsappDigits)
        assertNull(dto(whatsapp = "  ", phone = "").toDomain().whatsappDisplay)
    }

    @Test
    fun `nombre iniciales zona y ubicacion`() {
        val profile = dto(name = "  rodrigo   salva soto ").toDomain()

        assertEquals("rodrigo", profile.firstName)
        assertEquals("RS", profile.initials)
        assertEquals("Zarumilla, Tumbes", profile.locationLine)
        assertEquals("Zarumilla", profile.serviceZone)
        assertEquals("Tumbes", dto(districts = null).toDomain().serviceZone)
    }

    @Test
    fun `el sitio web recibe https cuando no trae esquema y el estado aprobado es verificado`() {
        assertEquals("https://ariz.pe", dto(website = "ariz.pe").toDomain().websiteUrl)
        assertEquals("http://ariz.pe", dto(website = "http://ariz.pe").toDomain().websiteUrl)
        assertNull(dto(website = " ").toDomain().websiteUrl)
        assertTrue(dto().toDomain().isVerified)
        assertFalse(dto(status = "PENDING").toDomain().isVerified)
    }

    @Test
    fun `las propiedades del portafolio se convierten a resumenes`() {
        val profile = dto(
            properties = listOf(
                AgentPropertyDto(
                    id = "p1", slug = "casa", title = "Casa", operation = "rent",
                    price = PriceDto(900.0, "USD"), location = LocationDto("Tumbes", "Zarumilla"),
                    images = listOf("a.jpg", "b.jpg"), rooms = 3, publishedAt = "2023-11-14T22:13:20.000Z",
                ),
            ),
        ).toDomain()

        val property = profile.properties.single()
        assertEquals(ListingOperation.RENT, property.operation)
        assertEquals(Currency.USD, property.currency)
        assertEquals("a.jpg", property.coverImageUrl)
        assertEquals(2, property.photoCount)
        assertEquals(1_700_000_000_000L, property.publishedAtMillis)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AgentProfileViewModelTest {

    private class FakeRepo(var result: AppResult<AgentProfile>) : AgentsRepository {
        var requested: String? = null
        override suspend fun getProfile(slugOrId: String): AppResult<AgentProfile> {
            requested = slugOrId
            return result
        }
    }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `pide el id de la ruta y reintentar recupera un error`() {
        val repo = FakeRepo(AppResult.Failure(AppError.NotFound()))
        val viewModel = AgentProfileViewModel(
            SavedStateHandle(mapOf(AgentProfileViewModel.ID_ARG to "a1")),
            GetAgentProfileUseCase(repo),
        )
        assertEquals("a1", repo.requested)
        assertNotNull(viewModel.state.value.error)
        assertNull(viewModel.state.value.profile)

        repo.result = AppResult.Success(dto().toDomain())
        viewModel.onAction(AgentProfileAction.Retry)

        assertNull(viewModel.state.value.error)
        assertEquals("Rodrigo Salva", viewModel.state.value.profile?.name)
        assertFalse(viewModel.state.value.isLoading)
    }
}
