package com.tecsup.app_ariz.feature.rewards.ui.club

import com.tecsup.app_ariz.core.common.AppError
import com.tecsup.app_ariz.feature.rewards.domain.model.CampaignPoints
import com.tecsup.app_ariz.feature.rewards.domain.model.Incentive
import com.tecsup.app_ariz.feature.rewards.domain.model.Redemption
import com.tecsup.app_ariz.feature.rewards.domain.model.Referrer

/** Resultado del último canje, para avisar al usuario. */
sealed interface RedeemOutcome {
    data object Success : RedeemOutcome

    data class Failure(val message: String) : RedeemOutcome
}

data class ClubUiState(
    // --- Paso 1: código de referente ---
    val codeInput: String = "",
    val isValidating: Boolean = false,
    val codeError: String? = null,

    // --- Paso 2: el club (hay [referrer] cuando el acceso fue válido) ---
    val referrer: Referrer? = null,
    /** Puntos disponibles; se descuentan al canjear sin volver a pedirlos al backend. */
    val points: Int = 0,
    val incentives: List<Incentive> = emptyList(),
    val redemptions: List<Redemption> = emptyList(),
    val campaigns: List<CampaignPoints> = emptyList(),
    val isLoadingClub: Boolean = false,
    val clubError: AppError? = null,

    // --- Canje ---
    /** Premio que el usuario está por canjear (diálogo de confirmación). */
    val pendingRedeem: Incentive? = null,
    val isRedeeming: Boolean = false,
    val redeemOutcome: RedeemOutcome? = null,

    val showReferralForm: Boolean = false,
) {
    val isInClub: Boolean get() = referrer != null

    val campaignPointsTotal: Int get() = campaigns.sumOf { it.totalPoints }
}

sealed interface ClubAction {
    data class CodeChanged(val value: String) : ClubAction
    data object ValidateCode : ClubAction
    data object Refresh : ClubAction
    data object LeaveClub : ClubAction
    data class RedeemRequested(val incentive: Incentive) : ClubAction
    data object RedeemConfirmed : ClubAction
    data object RedeemDismissed : ClubAction
    data object OutcomeDismissed : ClubAction
    data object ReferralFormOpened : ClubAction
    data object ReferralFormClosed : ClubAction
}
