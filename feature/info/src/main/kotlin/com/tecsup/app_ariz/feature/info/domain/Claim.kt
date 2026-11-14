package com.tecsup.app_ariz.feature.info.domain

import com.tecsup.app_ariz.core.common.AppResult

/** Libro de Reclamaciones (`POST /claims`, público): reclamo (sobre el bien) o queja (sobre la atención). */
enum class ClaimKind(val wire: String) { CLAIM("Claim"), COMPLAINT("Complaint") }

data class ClaimDraft(
    val fullName: String = "",
    val address: String = "",
    val dniOrCe: String = "",
    val phone: String = "",
    val email: String = "",
    val parentGuardian: String = "",
    /** `Producto` o `Servicio`. */
    val assetType: String = "Servicio",
    val claimedAmount: String = "",
    val assetDescription: String = "",
    val kind: ClaimKind = ClaimKind.CLAIM,
    val claimDetails: String = "",
    val consumerRequest: String = "",
)

enum class ClaimField { FULL_NAME, ADDRESS, DNI, EMAIL, AMOUNT, ASSET_DESCRIPTION, DETAILS, REQUEST }

enum class ClaimError { REQUIRED, TOO_SHORT_3, TOO_SHORT_5, TOO_SHORT_10, TOO_SHORT_20, DNI_LENGTH, INVALID_EMAIL, INVALID_AMOUNT }

/** Código y resumen que se muestran al consumidor tras registrar el reclamo. */
data class ClaimReceipt(val code: String, val fullName: String, val kind: ClaimKind)

/** Mismas reglas del formulario web (y límites del `CreateClaimDto`). */
object ClaimValidator {
    private val emailRegex = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun validate(draft: ClaimDraft): Map<ClaimField, ClaimError> = buildMap {
        minLength(draft.fullName, 3, ClaimError.TOO_SHORT_3)?.let { put(ClaimField.FULL_NAME, it) }
        minLength(draft.address, 5, ClaimError.TOO_SHORT_5)?.let { put(ClaimField.ADDRESS, it) }
        val dni = draft.dniOrCe.trim()
        when {
            dni.isEmpty() -> put(ClaimField.DNI, ClaimError.REQUIRED)
            dni.length < 8 || dni.length > 12 -> put(ClaimField.DNI, ClaimError.DNI_LENGTH)
        }
        when {
            draft.email.isBlank() -> put(ClaimField.EMAIL, ClaimError.REQUIRED)
            !emailRegex.matches(draft.email.trim()) -> put(ClaimField.EMAIL, ClaimError.INVALID_EMAIL)
        }
        val amount = draft.claimedAmount.trim().replace(',', '.').toDoubleOrNull()
        when {
            draft.claimedAmount.isBlank() -> put(ClaimField.AMOUNT, ClaimError.REQUIRED)
            amount == null || amount < 0 -> put(ClaimField.AMOUNT, ClaimError.INVALID_AMOUNT)
        }
        minLength(draft.assetDescription, 10, ClaimError.TOO_SHORT_10)?.let { put(ClaimField.ASSET_DESCRIPTION, it) }
        minLength(draft.claimDetails, 20, ClaimError.TOO_SHORT_20)?.let { put(ClaimField.DETAILS, it) }
        minLength(draft.consumerRequest, 20, ClaimError.TOO_SHORT_20)?.let { put(ClaimField.REQUEST, it) }
    }

    private fun minLength(value: String, min: Int, tooShort: ClaimError): ClaimError? = when {
        value.isBlank() -> ClaimError.REQUIRED
        value.trim().length < min -> tooShort
        else -> null
    }
}

interface ClaimRepository {
    suspend fun submit(draft: ClaimDraft): AppResult<ClaimReceipt>
}
