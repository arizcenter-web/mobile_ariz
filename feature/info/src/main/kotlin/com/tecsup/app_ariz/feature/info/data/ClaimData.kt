package com.tecsup.app_ariz.feature.info.data

import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.core.common.map
import com.tecsup.app_ariz.core.network.safeApiCall
import com.tecsup.app_ariz.feature.info.domain.ClaimDraft
import com.tecsup.app_ariz.feature.info.domain.ClaimKind
import com.tecsup.app_ariz.feature.info.domain.ClaimReceipt
import com.tecsup.app_ariz.feature.info.domain.ClaimRepository
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Inject

/** Cuerpo de `POST /claims`: teléfono y tutor vacíos no se envían. */
@Serializable
internal data class ClaimRequest(
    val fullName: String,
    val address: String,
    val dniOrCe: String,
    val phone: String? = null,
    val email: String,
    val parentGuardian: String? = null,
    val assetType: String,
    val claimedAmount: Double,
    val assetDescription: String,
    val type: String,
    val claimDetails: String,
    val consumerRequest: String,
)

@Serializable
internal data class ClaimResponse(val claimCode: String = "", val fullName: String = "", val type: String = "Claim")

internal interface ClaimApi {
    @POST("claims")
    suspend fun create(@Body body: ClaimRequest): ClaimResponse
}

internal class ClaimRepositoryImpl @Inject constructor(private val api: ClaimApi) : ClaimRepository {
    override suspend fun submit(draft: ClaimDraft): AppResult<ClaimReceipt> =
        safeApiCall { api.create(draft.toBody()) }.map {
            ClaimReceipt(it.claimCode, it.fullName.ifBlank { draft.fullName.trim() }, ClaimKind.entries.firstOrNull { k -> k.wire == it.type } ?: draft.kind)
        }
}

internal fun ClaimDraft.toBody() = ClaimRequest(
    fullName = fullName.trim(),
    address = address.trim(),
    dniOrCe = dniOrCe.trim(),
    phone = phone.trim().ifEmpty { null },
    email = email.trim(),
    parentGuardian = parentGuardian.trim().ifEmpty { null },
    assetType = assetType,
    claimedAmount = claimedAmount.trim().replace(',', '.').toDouble(),
    assetDescription = assetDescription.trim(),
    type = kind.wire,
    claimDetails = claimDetails.trim(),
    consumerRequest = consumerRequest.trim(),
)
