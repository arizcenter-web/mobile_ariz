package com.tecsup.app_ariz.feature.rewards.ui.club

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.app_ariz.core.common.AppResult
import com.tecsup.app_ariz.feature.rewards.domain.model.CodeAccess
import com.tecsup.app_ariz.feature.rewards.domain.usecase.LoadClubUseCase
import com.tecsup.app_ariz.feature.rewards.domain.usecase.RedeemIncentiveUseCase
import com.tecsup.app_ariz.feature.rewards.domain.usecase.ValidateReferrerCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val CodeMaxLength = 16

@HiltViewModel
class ClubViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val validateCode: ValidateReferrerCodeUseCase,
    private val loadClub: LoadClubUseCase,
    private val redeemIncentive: RedeemIncentiveUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ClubUiState())
    val state: StateFlow<ClubUiState> = _state.asStateFlow()

    init {
        // El código puede llegar en la ruta (enlace de invitación): entra directo al club.
        val codeFromRoute = savedStateHandle.get<String>(CODE_ARG)?.trim()?.uppercase().orEmpty()
        if (codeFromRoute.isNotEmpty()) {
            _state.update { it.copy(codeInput = codeFromRoute) }
            validate()
        }
    }

    fun onAction(action: ClubAction) {
        when (action) {
            is ClubAction.CodeChanged ->
                _state.update { it.copy(codeInput = action.value.uppercase().take(CodeMaxLength), codeError = null) }
            ClubAction.ValidateCode -> validate()
            ClubAction.Refresh -> _state.value.referrer?.let { loadClubData(it.code) }
            ClubAction.LeaveClub -> _state.value = ClubUiState()
            is ClubAction.RedeemRequested -> _state.update { it.copy(pendingRedeem = action.incentive) }
            ClubAction.RedeemConfirmed -> redeem()
            ClubAction.RedeemDismissed -> _state.update { it.copy(pendingRedeem = null) }
            ClubAction.OutcomeDismissed -> _state.update { it.copy(redeemOutcome = null) }
            ClubAction.ReferralFormOpened -> _state.update { it.copy(showReferralForm = true) }
            ClubAction.ReferralFormClosed -> _state.update { it.copy(showReferralForm = false) }
        }
    }

    private fun validate() {
        val code = _state.value.codeInput.trim()
        if (code.isEmpty()) {
            _state.update { it.copy(codeError = EMPTY_CODE) }
            return
        }
        if (_state.value.isValidating) return
        _state.update { it.copy(isValidating = true, codeError = null) }
        viewModelScope.launch {
            when (val result = validateCode(code)) {
                is AppResult.Failure -> _state.update { it.copy(isValidating = false, codeError = result.error.message) }
                is AppResult.Success -> when (val access = result.data) {
                    is CodeAccess.Invalid -> _state.update { it.copy(isValidating = false, codeError = access.message ?: INVALID_CODE) }
                    is CodeAccess.Valid -> {
                        _state.update { it.copy(isValidating = false, referrer = access.referrer, points = access.referrer.totalPoints) }
                        loadClubData(access.referrer.code)
                    }
                }
            }
        }
    }

    private fun loadClubData(code: String) {
        _state.update { it.copy(isLoadingClub = true, clubError = null) }
        viewModelScope.launch {
            when (val result = loadClub(code)) {
                is AppResult.Success -> _state.update {
                    it.copy(
                        isLoadingClub = false,
                        incentives = result.data.incentives,
                        redemptions = result.data.redemptions,
                        campaigns = result.data.campaigns,
                    )
                }
                is AppResult.Failure -> _state.update { it.copy(isLoadingClub = false, clubError = result.error) }
            }
        }
    }

    private fun redeem() {
        val current = _state.value
        val incentive = current.pendingRedeem ?: return
        val referrer = current.referrer ?: return
        if (current.isRedeeming) return
        _state.update { it.copy(isRedeeming = true) }
        viewModelScope.launch {
            when (val result = redeemIncentive(referrer.code, incentive, current.points)) {
                is AppResult.Success -> {
                    _state.update {
                        it.copy(
                            isRedeeming = false,
                            pendingRedeem = null,
                            points = (it.points - incentive.pointsRequired).coerceAtLeast(0),
                            redeemOutcome = RedeemOutcome.Success,
                        )
                    }
                    loadClubData(referrer.code) // refresca stock y "Mis canjes"
                }
                is AppResult.Failure -> _state.update {
                    it.copy(isRedeeming = false, pendingRedeem = null, redeemOutcome = RedeemOutcome.Failure(result.error.message))
                }
            }
        }
    }

    companion object {
        const val CODE_ARG = "code"
        const val EMPTY_CODE = "Ingresa tu código de referente"
        const val INVALID_CODE = "Código no válido"
    }
}
