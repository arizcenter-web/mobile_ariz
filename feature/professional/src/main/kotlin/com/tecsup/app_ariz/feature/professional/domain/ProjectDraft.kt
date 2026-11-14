package com.tecsup.app_ariz.feature.professional.domain

/** Un lote del inventario (campos en texto: así se editan en el formulario). */
data class LotDraft(
    val number: String = "",
    val block: String = "",
    val area: String = "",
    val price: String = "",
    /** Estado del lote al editar (los vendidos/reservados se conservan). */
    val status: String = "AVAILABLE",
    /** Datos que el formulario no edita pero el backend reemplaza al guardar. */
    val frontM: String = "",
    val planLocation: String = "",
)

/**
 * Proyecto de lotización que se crea desde el móvil. El web ofrece además plano interactivo y proyectos de
 * departamentos; esos se arman en el panel web.
 */
data class ProjectDraft(
    /** `LOTIZACION` o `DEPARTAMENTOS` (este último solo lo crea el administrador: se arma en el plano). */
    val category: String = "LOTIZACION",
    /** Los lotes/departamentos los define el plano del proyecto: aquí no se editan ni se validan. */
    val managedByPlan: Boolean = false,
    val name: String = "",
    val description: String = "",
    val stage: String = "PRE_SALE",
    val currency: String = "PEN",
    val priceFrom: String = "",
    val priceTo: String = "",
    val department: String = "",
    val province: String = "",
    val district: String = "",
    val zone: String = "",
    val street: String = "",
    val latitude: String = "",
    val longitude: String = "",
    /** Fecha estimada de entrega, `AAAA-MM-DD`. */
    val estimatedDelivery: String = "",
    val photos: List<String> = emptyList(),
    val lots: List<LotDraft> = listOf(LotDraft()),
    val contactName: String = "",
    val contactEmail: String = "",
    val contactPhone: String = "",
    val contactWhatsapp: String = "",
    /**
     * Al editar: el resto del proyecto (financiamiento, servicios, hitos, videos...) en JSON. El backend reemplaza
     * todo el proyecto en cada guardado, así que se devuelve tal cual para no perder lo que la app no edita.
     */
    val extras: String? = null,
)

enum class ProjectField {
    NAME, DESCRIPTION, PRICE_FROM, PRICE_TO, DEPARTMENT, PROVINCE, DISTRICT, LATITUDE, LONGITUDE, DELIVERY, PHOTOS, LOTS, CONTACT_EMAIL,
}

enum class ProjectFieldError { REQUIRED, INVALID_NUMBER, TOO_LONG, INVALID_EMAIL, INVALID_DATE, NEEDS_PHOTO, PRICE_RANGE, NEEDS_LOT, LOT_INVALID, LOT_DUPLICATE }

/** Reglas del `CreateProjectDto` del backend (el backend sigue siendo la autoridad). */
object ProjectValidator {
    const val NAME_MAX = 120
    const val DESCRIPTION_MAX = 8000

    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    private val dateRegex = Regex("^\\d{4}-\\d{2}-\\d{2}$")

    /** Para publicar el backend exige además el punto en el mapa (`requireCoordinates`). */
    fun validate(draft: ProjectDraft, requireCoordinates: Boolean): Map<ProjectField, ProjectFieldError> = buildMap {
        when {
            draft.name.isBlank() -> put(ProjectField.NAME, ProjectFieldError.REQUIRED)
            draft.name.trim().length > NAME_MAX -> put(ProjectField.NAME, ProjectFieldError.TOO_LONG)
        }
        when {
            draft.description.isBlank() -> put(ProjectField.DESCRIPTION, ProjectFieldError.REQUIRED)
            draft.description.trim().length > DESCRIPTION_MAX -> put(ProjectField.DESCRIPTION, ProjectFieldError.TOO_LONG)
        }
        val from = draft.priceFrom.toDoubleOrNull()
        val to = draft.priceTo.toDoubleOrNull()
        if (draft.priceFrom.isBlank()) put(ProjectField.PRICE_FROM, ProjectFieldError.REQUIRED)
        else if (from == null || from < 1) put(ProjectField.PRICE_FROM, ProjectFieldError.INVALID_NUMBER)
        if (draft.priceTo.isBlank()) put(ProjectField.PRICE_TO, ProjectFieldError.REQUIRED)
        else if (to == null || to < 1) put(ProjectField.PRICE_TO, ProjectFieldError.INVALID_NUMBER)
        else if (from != null && to < from) put(ProjectField.PRICE_TO, ProjectFieldError.PRICE_RANGE)

        if (draft.department.isBlank()) put(ProjectField.DEPARTMENT, ProjectFieldError.REQUIRED)
        if (draft.province.isBlank()) put(ProjectField.PROVINCE, ProjectFieldError.REQUIRED)
        if (draft.district.isBlank()) put(ProjectField.DISTRICT, ProjectFieldError.REQUIRED)

        coordinate(draft.latitude, 90.0, requireCoordinates)?.let { put(ProjectField.LATITUDE, it) }
        coordinate(draft.longitude, 180.0, requireCoordinates)?.let { put(ProjectField.LONGITUDE, it) }

        when {
            draft.estimatedDelivery.isBlank() -> put(ProjectField.DELIVERY, ProjectFieldError.REQUIRED)
            !dateRegex.matches(draft.estimatedDelivery.trim()) -> put(ProjectField.DELIVERY, ProjectFieldError.INVALID_DATE)
        }
        if (draft.photos.isEmpty()) put(ProjectField.PHOTOS, ProjectFieldError.NEEDS_PHOTO)
        if (draft.contactEmail.isNotBlank() && !emailRegex.matches(draft.contactEmail.trim())) {
            put(ProjectField.CONTACT_EMAIL, ProjectFieldError.INVALID_EMAIL)
        }

        val lots = draft.lots.filter { it != LotDraft() }
        when {
            draft.managedByPlan || draft.category == "DEPARTAMENTOS" -> Unit
            lots.isEmpty() -> put(ProjectField.LOTS, ProjectFieldError.NEEDS_LOT)
            lots.any { it.number.isBlank() || (it.area.toDoubleOrNull() ?: 0.0) < 1 || (it.price.toDoubleOrNull() ?: 0.0) < 1 } ->
                put(ProjectField.LOTS, ProjectFieldError.LOT_INVALID)
            lots.map { it.number.trim().lowercase() }.toSet().size != lots.size -> put(ProjectField.LOTS, ProjectFieldError.LOT_DUPLICATE)
        }
    }

    private fun coordinate(value: String, limit: Double, required: Boolean): ProjectFieldError? {
        if (value.isBlank()) return if (required) ProjectFieldError.REQUIRED else null
        val number = value.toDoubleOrNull() ?: return ProjectFieldError.INVALID_NUMBER
        return if (number < -limit || number > limit) ProjectFieldError.INVALID_NUMBER else null
    }
}

val ProfessionalType.canManageProjects: Boolean
    get() = this == ProfessionalType.REAL_ESTATE_AGENCY || this == ProfessionalType.DEVELOPER

/** Las inmobiliarias no eligen plan: coordinan la habilitación con el gerente de ARIZ. */
val ProfessionalType.isAgency: Boolean
    get() = this == ProfessionalType.REAL_ESTATE_AGENCY
