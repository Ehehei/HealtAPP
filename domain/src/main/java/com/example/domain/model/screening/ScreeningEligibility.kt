package com.example.domain.model.screening

import java.time.LocalDate

data class ScreeningEligibility(
    val screening: Screening,
    val status: ScreeningStatus,
    val lastDoneOn: LocalDate? = null,
    val nextDueOn: LocalDate? = null,
)

enum class ScreeningStatus {
    NOT_ELIGIBLE,
    DUE_NOW,
    UPCOMING,
}
