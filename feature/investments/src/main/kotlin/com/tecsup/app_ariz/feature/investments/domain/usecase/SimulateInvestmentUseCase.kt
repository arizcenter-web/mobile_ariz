package com.tecsup.app_ariz.feature.investments.domain.usecase

import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentGoal
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentOpportunity
import com.tecsup.app_ariz.feature.investments.domain.model.InvestmentSimulation
import javax.inject.Inject
import kotlin.math.roundToLong

/** Capital desde el cual el web deja de exigir que el objetivo coincida. */
private const val FlexibleCapital = 20_000

/** ROI que se asume cuando ninguna oportunidad es compatible. */
private const val DefaultRoiPercent = 14.0

/**
 * Evaluador de inversión del web (`InvestmentsExperience`): una oportunidad es compatible si el
 * capital alcanza para la inversión inicial y su objetivo coincide (o el capital es de 20 000 o más).
 * El retorno es referencial: capital × ROI promedio × plazo en años.
 */
class SimulateInvestmentUseCase @Inject constructor() {

    operator fun invoke(
        opportunities: List<InvestmentOpportunity>,
        capital: Int,
        termMonths: Int,
        goal: InvestmentGoal,
    ): InvestmentSimulation {
        val compatible = opportunities.filter {
            capital >= it.initialInvestment && (it.goal == goal || capital >= FlexibleCapital)
        }
        val averageRoi = if (compatible.isEmpty()) DefaultRoiPercent else compatible.map { it.roiPercent }.average()
        val estimatedReturn = (capital * (averageRoi / 100) * (termMonths / 12.0)).roundToLong()
        return InvestmentSimulation(compatible = compatible, estimatedReturn = estimatedReturn)
    }
}
