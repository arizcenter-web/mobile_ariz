package com.tecsup.app_ariz.feature.rewards.domain.usecase

import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.rewards.domain.model.CampaignPoints
import com.tecsup.app_ariz.feature.rewards.domain.model.CodeAccess
import com.tecsup.app_ariz.feature.rewards.domain.model.Incentive
import com.tecsup.app_ariz.feature.rewards.domain.model.Redemption
import com.tecsup.app_ariz.feature.rewards.domain.model.ReferralRequest
import com.tecsup.app_ariz.feature.rewards.domain.repository.RewardsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/** Código de referente que se escribe en pantalla: se normaliza y se valida contra el backend. */
class ValidateReferrerCodeUseCase @Inject constructor(
    private val repository: RewardsRepository,
) {
    suspend operator fun invoke(code: String): AppResult<CodeAccess> = repository.validateCode(code)
}

/** Lo que se muestra al entrar al club: catálogo, canjes del referente y puntos por campaña. */
data class ClubData(
    val incentives: List<Incentive>,
    val redemptions: List<Redemption>,
    val campaigns: List<CampaignPoints>,
)

/**
 * Carga el club en paralelo. El catálogo es imprescindible; los canjes y los puntos de campaña son
 * un extra: si fallan, se muestran vacíos (como hace el web con sus `catch`).
 */
class LoadClubUseCase @Inject constructor(
    private val repository: RewardsRepository,
) {
    suspend operator fun invoke(code: String): AppResult<ClubData> = coroutineScope {
        val catalog = async { repository.getCatalog() }
        val redemptions = async { repository.getMyRedemptions(code) }
        val campaigns = async { repository.getCampaignPoints(code) }

        when (val catalogResult = catalog.await()) {
            is AppResult.Failure -> catalogResult
            is AppResult.Success -> AppResult.Success(
                ClubData(
                    incentives = catalogResult.data,
                    redemptions = (redemptions.await() as? AppResult.Success)?.data.orEmpty(),
                    campaigns = (campaigns.await() as? AppResult.Success)?.data.orEmpty(),
                ),
            )
        }
    }
}

/** Canjea un premio. Antes de molestar al backend comprueba que alcancen los puntos y haya stock. */
class RedeemIncentiveUseCase @Inject constructor(
    private val repository: RewardsRepository,
) {
    suspend operator fun invoke(code: String, incentive: Incentive, availablePoints: Int): AppResult<Unit> = when {
        incentive.stock <= 0 -> AppResult.Failure(AppError.Validation("Este premio ya no tiene stock"))
        availablePoints < incentive.pointsRequired ->
            AppResult.Failure(AppError.Validation("No tienes puntos suficientes para este premio"))
        else -> repository.redeem(code, incentive.id)
    }
}

class SubmitReferralUseCase @Inject constructor(
    private val repository: RewardsRepository,
) {
    suspend operator fun invoke(request: ReferralRequest): AppResult<Unit> = repository.submitReferral(request)
}
