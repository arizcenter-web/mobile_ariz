package com.tecsup.app_ariz.feature.professional

import androidx.lifecycle.SavedStateHandle
import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.model.common.Currency
import com.tecsup.app_ariz.feature.professional.data.toBody
import com.tecsup.app_ariz.feature.professional.domain.GetPropertyDraftUseCase
import com.tecsup.app_ariz.feature.professional.domain.NotificationSettings
import com.tecsup.app_ariz.feature.professional.domain.OwnerLead
import com.tecsup.app_ariz.feature.professional.domain.OwnerProject
import com.tecsup.app_ariz.feature.professional.domain.OwnerProperty
import com.tecsup.app_ariz.feature.professional.domain.Plan
import com.tecsup.app_ariz.feature.professional.domain.ProfessionalProfile
import com.tecsup.app_ariz.feature.professional.domain.ProfessionalRepository
import com.tecsup.app_ariz.feature.professional.domain.ProfileUpdate
import com.tecsup.app_ariz.feature.professional.domain.ProjectStatus
import com.tecsup.app_ariz.feature.professional.domain.PropertyDraft
import com.tecsup.app_ariz.feature.professional.domain.PropertyField
import com.tecsup.app_ariz.feature.professional.domain.PropertyFieldError
import com.tecsup.app_ariz.feature.professional.domain.PropertyStatus
import com.tecsup.app_ariz.feature.professional.domain.PropertyValidator
import com.tecsup.app_ariz.feature.professional.domain.PublishPropertyUseCase
import com.tecsup.app_ariz.feature.professional.domain.SavePropertyUseCase
import com.tecsup.app_ariz.feature.professional.domain.StatsCalculator
import com.tecsup.app_ariz.feature.professional.domain.Subscription
import com.tecsup.app_ariz.feature.professional.domain.SubscriptionStatus
import com.tecsup.app_ariz.feature.professional.domain.UploadPhotoUseCase
import com.tecsup.app_ariz.feature.professional.domain.UploadedPhoto
import com.tecsup.app_ariz.feature.professional.ui.PropertyFormAction
import com.tecsup.app_ariz.feature.professional.ui.PropertyFormViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.TimeZone

private const val Day = 86_400_000L
private val Utc: TimeZone = TimeZone.getTimeZone("UTC")

private fun property(id: String, status: PropertyStatus) =
    OwnerProperty(id, id, "Propiedad $id", status, false, 100.0, Currency.PEN, "Tumbes", "Tumbes", null, null)

private fun project(status: ProjectStatus) =
    OwnerProject("j", "j", "Proyecto", status, 1.0, Currency.PEN, 3, "Tumbes", null, null)

private fun lead(id: String, at: Long, source: String = "WHATSAPP", target: String? = null) =
    OwnerLead(id, "N", "e@e.com", "", "", source, at, target, "")

private fun validDraft() = PropertyDraft(
    title = "Casa en Tumbes", description = "Hermosa casa", price = "250000", district = "Tumbes",
    address = "Av. Principal 123", photos = listOf("https://img/1.jpg"),
)

class StatsCalculatorTest {

    // Día 100 a mediodía (UTC): "hoy" es el día 100.
    private val now = 100 * Day + 12 * 3_600_000L

    @Test
    fun `cuenta publicadas, pausadas y otras como el dashboard del web`() {
        val stats = StatsCalculator.calculate(
            properties = listOf(
                property("a", PropertyStatus.PUBLISHED), property("b", PropertyStatus.PAUSED),
                property("c", PropertyStatus.DRAFT), property("d", PropertyStatus.REJECTED),
            ),
            projects = listOf(project(ProjectStatus.PUBLISHED), project(ProjectStatus.DRAFT)),
            leads = emptyList(), subscription = null, nowMillis = now, timeZone = Utc,
        )

        assertEquals(1, stats.publishedCount)
        assertEquals(1, stats.pausedCount)
        assertEquals(2, stats.otherCount)
        assertEquals(1, stats.activeProjects)
        assertEquals(2, stats.totalProjects)
    }

    @Test
    fun `las consultas nuevas son de los ultimos 7 dias y la tendencia tiene 14 barras con hoy al final`() {
        val leads = listOf(lead("1", now - 1 * Day), lead("2", now - 3 * Day), lead("3", now - 20 * Day), lead("4", now - 3 * Day))

        val stats = StatsCalculator.calculate(emptyList(), emptyList(), leads, null, now, Utc)

        assertEquals(4, stats.totalLeads)
        assertEquals(3, stats.newLeads)
        assertEquals(14, stats.leadsLast14Days.size)
        assertEquals(0, stats.leadsLast14Days[13].count) // hoy
        assertEquals(1, stats.leadsLast14Days[12].count) // ayer
        assertEquals(2, stats.leadsLast14Days[10].count) // hace 3 días
        assertEquals(3, stats.leadsInTrend) // la de hace 20 días queda fuera
    }

    @Test
    fun `agrupa por origen y ordena las propiedades por consultas`() {
        val leads = listOf(
            lead("1", now, "WHATSAPP", "a"), lead("2", now, "WHATSAPP", "b"), lead("3", now, "PHONE", "b"), lead("4", now, "WHATSAPP", "b"),
        )
        val stats = StatsCalculator.calculate(
            listOf(property("a", PropertyStatus.PUBLISHED), property("b", PropertyStatus.PUBLISHED)), emptyList(), leads, null, now, Utc,
        )

        assertEquals("WHATSAPP", stats.leadsBySource.first().source)
        assertEquals(3, stats.leadsBySource.first().count)
        assertEquals("b", stats.topProperties.first().propertyId)
        assertEquals(3, stats.topProperties.first().leads)
    }

    @Test
    fun `los dias restantes solo existen con un plan activo`() {
        val active = Subscription("s", SubscriptionStatus.ACTIVE, "Pro", null, now + 10 * Day)
        val pending = active.copy(status = SubscriptionStatus.PENDING_PAYMENT)

        assertEquals(10, StatsCalculator.calculate(emptyList(), emptyList(), emptyList(), active, now, Utc).daysRemaining)
        assertNull(StatsCalculator.calculate(emptyList(), emptyList(), emptyList(), pending, now, Utc).daysRemaining)
        assertNull(StatsCalculator.calculate(emptyList(), emptyList(), emptyList(), null, now, Utc).daysRemaining)
    }
}

class PropertyValidatorTest {

    @Test
    fun `un borrador completo no tiene errores`() {
        assertTrue(PropertyValidator.validate(validDraft()).isEmpty())
    }

    @Test
    fun `campos obligatorios, precio minimo y al menos una foto`() {
        val errors = PropertyValidator.validate(PropertyDraft(price = "0"))

        assertEquals(PropertyFieldError.REQUIRED, errors[PropertyField.TITLE])
        assertEquals(PropertyFieldError.REQUIRED, errors[PropertyField.DESCRIPTION])
        assertEquals(PropertyFieldError.INVALID_NUMBER, errors[PropertyField.PRICE])
        assertEquals(PropertyFieldError.REQUIRED, errors[PropertyField.DISTRICT])
        assertEquals(PropertyFieldError.REQUIRED, errors[PropertyField.ADDRESS])
        assertEquals(PropertyFieldError.NEEDS_PHOTO, errors[PropertyField.PHOTOS])
    }

    @Test
    fun `titulo largo, areas menores a 1 y correo invalido`() {
        val errors = PropertyValidator.validate(
            validDraft().copy(title = "x".repeat(101), totalArea = "0.5", constructedArea = "abc", contactEmail = "no-es-correo"),
        )

        assertEquals(PropertyFieldError.TOO_LONG, errors[PropertyField.TITLE])
        assertEquals(PropertyFieldError.INVALID_NUMBER, errors[PropertyField.TOTAL_AREA])
        assertEquals(PropertyFieldError.INVALID_NUMBER, errors[PropertyField.CONSTRUCTED_AREA])
        assertEquals(PropertyFieldError.INVALID_EMAIL, errors[PropertyField.CONTACT_EMAIL])
    }

    @Test
    fun `el cuerpo usa la primera foto como portada, convierte numeros y omite lo vacio`() {
        val body = validDraft().copy(
            photos = listOf("https://img/1.jpg", "https://img/2.jpg"), totalArea = "120.5", bedrooms = "3",
            maintenanceFee = "", contactName = "  ", amenities = setOf("PISCINA"),
        ).toBody()

        assertEquals(250_000.0, body.price, 0.0)
        assertEquals(120.5, body.details.totalArea!!, 0.0)
        assertEquals(3, body.details.bedrooms)
        assertNull(body.maintenanceFee)
        assertNull(body.contactName)
        assertEquals(listOf(true, false), body.media.map { it.isCover })
        assertEquals(listOf(0, 1), body.media.map { it.position })
        assertEquals(listOf("PISCINA"), body.amenityCodes)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PropertyFormViewModelTest {

    private class FakeRepo : ProfessionalRepository {
        val saves = mutableListOf<String?>()
        var publishResult: AppResult<OwnerProperty> = AppResult.Success(property("p-nueva", PropertyStatus.PUBLISHED))
        var uploads = 0
        var uploadFails = false

        override suspend fun saveProperty(id: String?, draft: PropertyDraft, asAdmin: Boolean): AppResult<OwnerProperty> {
            saves += id
            return AppResult.Success(property("p-nueva", PropertyStatus.DRAFT))
        }
        override suspend fun publishProperty(id: String) = publishResult
        override suspend fun uploadPhoto(uri: String, folder: String): AppResult<UploadedPhoto> {
            uploads++
            return if (uploadFails) AppResult.Failure(AppError.Network()) else AppResult.Success(UploadedPhoto("https://img/$uri.jpg", "id"))
        }
        override suspend fun getPropertyDraft(id: String): AppResult<PropertyDraft> = AppResult.Success(validDraft())

        override suspend fun getProfile(): AppResult<ProfessionalProfile> = error("no se usa")
        override suspend fun updateProfile(update: ProfileUpdate): AppResult<ProfessionalProfile> = error("no se usa")
        override suspend fun changePassword(current: String, new: String): AppResult<Unit> = error("no se usa")
        override suspend fun getSettings(): AppResult<NotificationSettings> = error("no se usa")
        override suspend fun updateSettings(settings: NotificationSettings): AppResult<NotificationSettings> = error("no se usa")
        override suspend fun getProperties(): AppResult<List<OwnerProperty>> = error("no se usa")
        override suspend fun deleteProperty(id: String): AppResult<Unit> = error("no se usa")
        override suspend fun getProjects(): AppResult<List<OwnerProject>> = error("no se usa")
        override suspend fun createProject(draft: com.tecsup.app_ariz.feature.professional.domain.ProjectDraft, asAdmin: Boolean): AppResult<String> = error("no se usa")
        override suspend fun getProjectDraft(id: String, asAdmin: Boolean): AppResult<com.tecsup.app_ariz.feature.professional.domain.ProjectDraft> = error("no se usa")
        override suspend fun updateProject(id: String, draft: com.tecsup.app_ariz.feature.professional.domain.ProjectDraft): AppResult<Unit> = error("no se usa")
        override suspend fun publishProject(id: String, asAdmin: Boolean): AppResult<Unit> = error("no se usa")
        override suspend fun deleteProject(id: String): AppResult<Unit> = error("no se usa")
        override suspend fun getLeads(): AppResult<List<OwnerLead>> = error("no se usa")
        override suspend fun getPlans(): AppResult<List<Plan>> = error("no se usa")
        override suspend fun getSubscription(): AppResult<Subscription?> = error("no se usa")
        override suspend fun requestPlan(planId: String, receiptUrl: String?): AppResult<Subscription> = error("no se usa")
    }

    private val repo = FakeRepo()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(id: String? = null) = PropertyFormViewModel(
        SavedStateHandle(if (id == null) emptyMap() else mapOf(PropertyFormViewModel.ID_ARG to id)),
        GetPropertyDraftUseCase(repo), SavePropertyUseCase(repo), PublishPropertyUseCase(repo), UploadPhotoUseCase(repo),
    )

    @Test
    fun `guardar un formulario incompleto marca los errores y no llama al backend`() {
        val viewModel = viewModel()

        viewModel.onAction(PropertyFormAction.Save(publish = false))

        assertTrue(viewModel.state.value.errors.isNotEmpty())
        assertTrue(repo.saves.isEmpty())
    }

    @Test
    fun `las fotos elegidas se suben y se agregan, y quitar o poner portada reordena`() {
        val viewModel = viewModel()

        viewModel.onAction(PropertyFormAction.PhotosPicked(listOf("a", "b")))
        assertEquals(listOf("https://img/a.jpg", "https://img/b.jpg"), viewModel.state.value.draft.photos)
        assertEquals(0, viewModel.state.value.uploading)

        viewModel.onAction(PropertyFormAction.MakeCover("https://img/b.jpg"))
        assertEquals("https://img/b.jpg", viewModel.state.value.draft.photos.first())

        viewModel.onAction(PropertyFormAction.RemovePhoto("https://img/b.jpg"))
        assertEquals(listOf("https://img/a.jpg"), viewModel.state.value.draft.photos)
    }

    @Test
    fun `una foto que falla muestra el error y no se agrega`() {
        repo.uploadFails = true
        val viewModel = viewModel()

        viewModel.onAction(PropertyFormAction.PhotosPicked(listOf("a")))

        assertTrue(viewModel.state.value.draft.photos.isEmpty())
        assertEquals(0, viewModel.state.value.uploading)
        assertFalse(viewModel.state.value.photoError.isNullOrBlank())
    }

    @Test
    fun `si crea y la publicacion falla, el segundo intento edita la misma y no crea un duplicado`() {
        repo.publishResult = AppResult.Failure(AppError.Forbidden("Tu perfil debe estar aprobado"))
        val viewModel = viewModel()
        viewModel.onAction(PropertyFormAction.Edit { validDraft() })

        viewModel.onAction(PropertyFormAction.Save(publish = true))
        assertEquals(listOf<String?>(null), repo.saves)
        assertEquals("Tu perfil debe estar aprobado", viewModel.state.value.error?.message)

        viewModel.onAction(PropertyFormAction.Save(publish = true))
        assertEquals(listOf<String?>(null, "p-nueva"), repo.saves)
    }

    @Test
    fun `al editar carga la propiedad existente`() {
        val viewModel = viewModel(id = "abc")

        assertTrue(viewModel.state.value.isEditing)
        assertEquals("Casa en Tumbes", viewModel.state.value.draft.title)
        assertFalse(viewModel.state.value.isLoading)
    }
}
