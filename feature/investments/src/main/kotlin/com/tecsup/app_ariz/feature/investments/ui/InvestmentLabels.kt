package com.tecsup.app_ariz.feature.investments.ui

import androidx.annotation.StringRes
import com.tecsup.app_ariz.feature.investments.R
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentGoal
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentRisk
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentStatus

@StringRes
internal fun InvestmentGoal.labelRes(): Int = when (this) {
    InvestmentGoal.PLUSVALIA -> R.string.investments_goal_plusvalia
    InvestmentGoal.CONSTRUCCION -> R.string.investments_goal_construccion
    InvestmentGoal.RENTA -> R.string.investments_goal_renta
}

@StringRes
internal fun InvestmentStatus.labelRes(): Int = when (this) {
    InvestmentStatus.AVAILABLE -> R.string.investments_status_available
    InvestmentStatus.COMING_SOON -> R.string.investments_status_coming_soon
    InvestmentStatus.CLOSED -> R.string.investments_status_closed
}

@StringRes
internal fun InvestmentRisk.labelRes(): Int = when (this) {
    InvestmentRisk.LOW -> R.string.investments_risk_low
    InvestmentRisk.MEDIUM -> R.string.investments_risk_medium
    InvestmentRisk.HIGH -> R.string.investments_risk_high
}
